package com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates;

import com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates.interfaces.BusinessDayCalculator;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// This class has a single responsibility: to calculate dates

@ApplicationScoped
public class VacationDaysBtCalculator implements BusinessDayCalculator {

    @Override
    public int calculateBusinessDays(LocalDate start, LocalDate end) {
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }
}
