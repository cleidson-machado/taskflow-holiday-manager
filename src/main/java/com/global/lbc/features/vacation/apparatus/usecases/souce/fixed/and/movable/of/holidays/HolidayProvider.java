package com.global.lbc.features.vacation.apparatus.usecases.souce.fixed.and.movable.of.holidays;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class HolidayProvider {


    public Set<LocalDate> getBusinessDayHolidaysForPeriod(int startYear, int endYear) {
        return IntStream.rangeClosed(startYear, endYear)
                .mapToObj(this::getHolidaysForYear)
                .flatMap(Set::stream)
                .filter(this::isBusinessDay)
                .collect(Collectors.toSet());
    }

    private Set<LocalDate> getHolidaysForYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // 1. Feriados Fixos Nacionais Brasileiros (Exemplo)
        holidays.add(LocalDate.of(year, Month.JANUARY, 1));     // Confraternização Universal
        holidays.add(LocalDate.of(year, Month.APRIL, 21));      // Tiradentes
        holidays.add(LocalDate.of(year, Month.MAY, 1));         // Dia do Trabalho
        holidays.add(LocalDate.of(year, Month.SEPTEMBER, 7));   // Independência do Brasil
        holidays.add(LocalDate.of(year, Month.OCTOBER, 12));    // Nossa Senhora Aparecida
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 2));    // Finados
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 15));   // Proclamação da República
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25));   // Natal

        // 2. Feriados Móveis (baseados no cálculo da Páscoa)
        LocalDate easter = calculateEaster(year);
        holidays.add(easter.minusDays(47)); // Carnaval (Terça-feira de Carnaval)
        holidays.add(easter.minusDays(2));  // Sexta-feira Santa
        holidays.add(easter.plusDays(60));  // Corpus Christi

        return holidays;
    }

    private LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(year, month, day);
    }

    private boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}