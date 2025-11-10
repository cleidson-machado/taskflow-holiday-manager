package com.global.lbc.features.vacation.apparatus.usecases.days.between.two.dates.interfaces;

import java.time.LocalDate;

public interface BusinessDayCalculator {
    int calculateBusinessDays(LocalDate start, LocalDate end);
}
