package com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates;

import com.global.lbc.features.vacation.apparatus.usecases.souce.fixed.and.movable.of.holidays.HolidayProvider;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class VacationDaysBtCalculatorTest {

    @Inject
    VacationDaysBtCalculator calculator;

    @Inject
    HolidayProvider holidayProvider;

    @Test
    void testCalculateEndDate_5DaysFromNov13_2025() {
        // 13/11/2025 é uma quinta-feira
        LocalDate startDate = LocalDate.of(2025, 11, 13);
        int daysRequested = 5;

        LocalDate endDate = calculator.calculateEndDate(startDate, daysRequested);

        // Esperado:
        // 13/11 (quinta) - Dia 1
        // 14/11 (sexta) - Dia 2
        // 15/11 (sábado) - FIM DE SEMANA (pula)
        // 16/11 (domingo) - FIM DE SEMANA (pula)
        // 17/11 (segunda) - Dia 3
        // 18/11 (terça) - Dia 4
        // 19/11 (quarta) - Dia 5
        LocalDate expectedEndDate = LocalDate.of(2025, 11, 19);

        assertEquals(expectedEndDate, endDate,
            "5 dias úteis a partir de 13/11/2025 deve terminar em 19/11/2025");
        assertEquals(DayOfWeek.WEDNESDAY, endDate.getDayOfWeek());
    }

    @Test
    void testCalculateEndDate_10DaysFromJan2_2025() {
        // 02/01/2025 é uma quinta-feira
        // 01/01/2025 é feriado (Confraternização Universal)
        LocalDate startDate = LocalDate.of(2025, 1, 2);
        int daysRequested = 10;

        LocalDate endDate = calculator.calculateEndDate(startDate, daysRequested);

        // Deve contar apenas dias úteis, pulando fins de semana
        assertNotNull(endDate);
        assertTrue(endDate.isAfter(startDate));

        // Verifica que a data final não é fim de semana
        assertNotEquals(DayOfWeek.SATURDAY, endDate.getDayOfWeek());
        assertNotEquals(DayOfWeek.SUNDAY, endDate.getDayOfWeek());

        // Verifica que calculou corretamente os dias úteis
        long actualBusinessDays = calculator.calculateActualBusinessDays(startDate, endDate);
        assertEquals(daysRequested, actualBusinessDays);
    }

    @Test
    void testCalculateEndDate_1Day() {
        LocalDate startDate = LocalDate.of(2025, 11, 13); // quinta-feira
        int daysRequested = 1;

        LocalDate endDate = calculator.calculateEndDate(startDate, daysRequested);

        assertEquals(startDate, endDate, "1 dia útil deve retornar a própria data de início");
    }

    @Test
    void testCalculateEndDate_StartingOnMonday() {
        LocalDate startDate = LocalDate.of(2025, 11, 17); // segunda-feira
        int daysRequested = 5;

        LocalDate endDate = calculator.calculateEndDate(startDate, daysRequested);

        // Segunda a sexta = 5 dias úteis
        LocalDate expectedEndDate = LocalDate.of(2025, 11, 21); // sexta-feira
        assertEquals(expectedEndDate, endDate);
        assertEquals(DayOfWeek.FRIDAY, endDate.getDayOfWeek());
    }

    @Test
    void testCalculateEndDate_CrossingHoliday() {
        // Teste com um período que inclui feriado
        // 15/11 é Proclamação da República
        LocalDate startDate = LocalDate.of(2025, 11, 13); // quinta-feira
        int daysRequested = 3;

        LocalDate endDate = calculator.calculateEndDate(startDate, daysRequested);

        // Deve pular o fim de semana e chegar na segunda
        // 13/11 (quinta) - Dia 1
        // 14/11 (sexta) - Dia 2
        // 15/11 (sábado) - FIM DE SEMANA
        // 16/11 (domingo) - FIM DE SEMANA
        // 17/11 (segunda) - Dia 3
        LocalDate expectedEndDate = LocalDate.of(2025, 11, 17);
        assertEquals(expectedEndDate, endDate);
    }

    @Test
    void testCalculateEndDate_NullStartDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateEndDate(null, 5);
        });
    }

    @Test
    void testCalculateEndDate_ZeroDays() {
        LocalDate startDate = LocalDate.of(2025, 11, 13);
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateEndDate(startDate, 0);
        });
    }

    @Test
    void testCalculateEndDate_NegativeDays() {
        LocalDate startDate = LocalDate.of(2025, 11, 13);
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateEndDate(startDate, -5);
        });
    }

    @Test
    void testCalculateActualBusinessDays_WithWeekends() {
        LocalDate start = LocalDate.of(2025, 11, 13); // quinta
        LocalDate end = LocalDate.of(2025, 11, 19);   // quarta

        long businessDays = calculator.calculateActualBusinessDays(start, end);

        // 13, 14, 17, 18, 19 = 5 dias úteis
        assertEquals(5, businessDays);
    }

    @Test
    void testIsHoliday_NewYear() {
        LocalDate newYear = LocalDate.of(2025, 1, 1);
        assertTrue(calculator.isHoliday(newYear), "01/01 deve ser feriado");
    }

    @Test
    void testIsHoliday_Christmas() {
        LocalDate christmas = LocalDate.of(2025, 12, 25);
        assertTrue(calculator.isHoliday(christmas), "25/12 deve ser feriado");
    }

    @Test
    void testIsHoliday_RegularDay() {
        LocalDate regularDay = LocalDate.of(2025, 11, 13);
        assertFalse(calculator.isHoliday(regularDay), "13/11/2025 não é feriado");
    }
}

