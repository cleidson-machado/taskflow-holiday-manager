package com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates;

import com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates.interfaces.BusinessDayCalculator;
import com.global.lbc.features.vacation.apparatus.usecases.souce.fixed.and.movable.of.holidays.HolidayProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Stream;


@ApplicationScoped
public class VacationDaysBtCalculator implements BusinessDayCalculator {

    @Inject
    HolidayProvider holidayProvider;

    @Override
    public int calculateBusinessDays(LocalDate start, LocalDate end) {
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }

    public long calculateActualBusinessDays(LocalDate start, LocalDate end) {
        int startYear = start.getYear();
        int endYear = end.getYear();

        Set<LocalDate> holidays = holidayProvider.getBusinessDayHolidaysForPeriod(startYear, endYear);

        return Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                .filter(date -> {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();

                    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                        return false;
                    }
                    if (holidays.contains(date)) {
                        return false;
                    }
                    return true;
                })
                .count();
    }

    public boolean isHoliday(LocalDate date) {
        Set<LocalDate> holidays = holidayProvider.getBusinessDayHolidaysForPeriod(date.getYear(), date.getYear());
        return holidays.contains(date);
    }

    /**
     * Calcula a data final de férias baseada na data de início e no número de dias úteis solicitados.
     * Considera finais de semana e feriados no cálculo.
     *
     * @param startDate Data de início das férias
     * @param daysRequested Número de dias úteis de férias solicitados
     * @return Data final das férias (última data efetiva de férias)
     */
    public LocalDate calculateEndDate(LocalDate startDate, int daysRequested) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (daysRequested <= 0) {
            throw new IllegalArgumentException("Days requested must be greater than zero");
        }

        LocalDate currentDate = startDate;
        int businessDaysCount = 0;

        // Busca feriados para o período estimado (adiciona margem de segurança)
        int startYear = startDate.getYear();
        int estimatedEndYear = startDate.plusDays(daysRequested * 2L).getYear(); // margem para feriados e fins de semana
        Set<LocalDate> holidays = holidayProvider.getBusinessDayHolidaysForPeriod(startYear, estimatedEndYear);

        // Itera até encontrar o número de dias úteis solicitados
        while (businessDaysCount < daysRequested) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Verifica se é dia útil (não é fim de semana e não é feriado)
            boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
            boolean isHoliday = holidays.contains(currentDate);

            if (!isWeekend && !isHoliday) {
                businessDaysCount++;

                // Se já atingiu o número de dias solicitados, retorna a data atual
                if (businessDaysCount == daysRequested) {
                    return currentDate;
                }
            }

            // Avança para o próximo dia
            currentDate = currentDate.plusDays(1);
        }

        return currentDate;
    }
}