package com.global.lbc.features.booking.apparatus.application.service;

import com.global.lbc.features.booking.apparatus.application.BookingMapper;
import com.global.lbc.features.booking.apparatus.application.dto.BookingRequest;
import com.global.lbc.features.booking.apparatus.application.dto.BookingResponse;
import com.global.lbc.features.booking.apparatus.model.Booking;
import com.global.lbc.features.booking.apparatus.model.util.BookingStatus;
import com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates.VacationDaysBtCalculator;
import com.global.lbc.shared.PaginatedResponse;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookingService {

    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    VacationDaysBtCalculator calculator;

    @Inject
    BookingMapper mapper;

    // --- MÉTODOS DE BUSCA (READ) ---

    public Optional<BookingResponse> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Booking booking = Booking.findById(id);

        // Mapeia a Entidade para DTO
        return Optional.ofNullable(booking).map(mapper::toResponse);
    }

    public PaginatedResponse<BookingResponse> getPaginatedBookings(int page, int size, String sortField, String sortOrder) {
        validatePagination(page, size);

        Sort sortBy = buildSort(sortField, sortOrder);

        var query = Booking.<Booking>findAll(sortBy).page(page, size);
        long totalItems = Booking.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<BookingResponse> bookings = query.list().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                bookings,
                totalItems,
                totalPages,
                page
        );
    }

    /**
     * Busca bookings ativos de um colaborador específico
     */
    public List<BookingResponse> findActiveBookingsByEmployee(UUID employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }

        List<Booking> bookings = Booking.list("employee.id = ?1 and isActive = true", employeeId);
        return bookings.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se há conflito de datas com outros bookings ativos do mesmo colaborador
     */
    public boolean hasDateConflict(UUID employeeId, LocalDate startDate, LocalDate endDate, UUID excludeBookingId) {
        if (employeeId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Employee ID and dates cannot be null");
        }

        String query = "employee.id = ?1 and isActive = true and bookingStatus = ?2 " +
                "and ((startDate <= ?3 and endDate >= ?3) or (startDate <= ?4 and endDate >= ?4) " +
                "or (startDate >= ?3 and endDate <= ?4))";

        List<Booking> conflictingBookings;
        if (excludeBookingId != null) {
            conflictingBookings = Booking.list(query + " and id != ?5",
                    employeeId, BookingStatus.RESERVED, startDate, endDate, excludeBookingId);
        } else {
            conflictingBookings = Booking.list(query,
                    employeeId, BookingStatus.RESERVED, startDate, endDate);
        }

        return !conflictingBookings.isEmpty();
    }

    // --- MÉTODOS DE ESCRITA (CREATE & UPDATE) ---

    @Transactional
    public BookingResponse create(BookingRequest dto) {
        // Validações de negócio do DTO
        validateBookingData(dto);

        // 1. Mapeia DTO de Request para Entidade
        Booking booking = mapper.toEntity(dto);

        // 2. Lógica de Negócio: Calcula a data final com base nos dias úteis solicitados
        if (booking.startDate != null && booking.daysReserved != null) {
            booking.endDate = calculator.calculateEndDate(booking.startDate, booking.daysReserved);
        }

        // Verifica conflito de datas após calcular endDate
        if (hasDateConflict(dto.employeeId, booking.startDate, booking.endDate, null)) {
            throw new IllegalStateException("There is already an active booking for this period.");
        }

        // 3. Define status inicial
        booking.bookingStatus = BookingStatus.RESERVED;
        booking.isActive = true;

        // 4. Persistência
        booking.persist();

        // 5. Mapeia Entidade de volta para DTO de Response
        return mapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse update(UUID id, BookingRequest dto) {
        Booking booking = Booking.findById(id);
        if (booking == null) {
            throw new NotFoundException("Booking not found: " + id);
        }

        // Não permite atualizar booking cancelado
        if (booking.bookingStatus == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled booking.");
        }

        // Validações de negócio
        validateBookingData(dto);

        // 1. Mapeia DTO de Request para atualizar a Entidade
        mapper.updateEntity(booking, dto);

        // 2. Lógica de Negócio: Recalcula a data final com base nos dias úteis solicitados
        if (booking.startDate != null && booking.daysReserved != null) {
            booking.endDate = calculator.calculateEndDate(booking.startDate, booking.daysReserved);
        }

        // Verifica conflito de datas após recalcular endDate (excluindo o próprio booking)
        if (hasDateConflict(dto.employeeId, booking.startDate, booking.endDate, id)) {
            throw new IllegalStateException("There is already an active booking for this period.");
        }

        // Persistência (Panache faz o update no fim do @Transactional)
        return mapper.toResponse(booking);
    }

    // --- MÉTODOS DE TRANSIÇÃO DE ESTADO ---

    @Transactional
    public BookingResponse cancel(UUID bookingId, String cancelledBy) {
        Booking booking = Booking.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found.");
        }

        if (booking.bookingStatus == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        // Se já foi convertido em vacation, não permite cancelar
        if (booking.vacationId != null) {
            throw new IllegalStateException("Cannot cancel a booking that has been converted to a vacation request.");
        }

        // Lógica de Transição de Estado
        booking.cancel();

        // Retorna DTO
        return mapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse linkToVacation(UUID bookingId, UUID vacationId) {
        Booking booking = Booking.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found.");
        }

        if (booking.bookingStatus == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot link a cancelled booking to a vacation.");
        }

        if (booking.vacationId != null) {
            throw new IllegalStateException("Booking is already linked to a vacation: " + booking.vacationId);
        }

        // Vincula o booking à vacation
        booking.vacationId = vacationId;

        return mapper.toResponse(booking);
    }

    @Transactional
    public void softDelete(UUID bookingId, String deletedBy) {
        Booking booking = Booking.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found: " + bookingId);
        }

        if (deletedBy == null || deletedBy.isBlank()) {
            throw new IllegalArgumentException("deletedBy cannot be null or empty");
        }

        // Usa o método de soft delete da entidade
        booking.softDelete(deletedBy);
    }

    // --- MÉTODOS AUXILIARES ---

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void validateBookingData(BookingRequest dto) {
        if (dto.employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (dto.startDate == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (dto.daysReserved == null || dto.daysReserved <= 0) {
            throw new IllegalArgumentException("Days reserved must be greater than zero.");
        }
        if (dto.startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create booking for past dates.");
        }
    }

    private Sort buildSort(String sortField, String sortOrder) {
        String field = (sortField == null || sortField.isBlank()) ? "startDate" : sortField;

        return "desc".equalsIgnoreCase(sortOrder)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}
