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
}