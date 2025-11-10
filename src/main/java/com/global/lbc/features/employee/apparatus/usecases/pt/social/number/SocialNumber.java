package com.global.lbc.features.employee.apparatus.usecases.pt.social.number;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;
import java.util.regex.Pattern;

// DO A TEST BECAUSE THE ERROR ON VALIDADE MAYBE IS BECAUSE WUE NOW USER SERIALIZER AND DESERIALIZER
// Keep this code until we confirm everything is working fine!

@JsonSerialize(using = SocialNumberSerializer.class)
@JsonDeserialize(using = SocialNumberDeserializer.class)
public class SocialNumber {

    private static final Pattern NISS_PATTERN = Pattern.compile("^[12][0-9]{10}$");
    private static final int NISS_LENGTH = 11;

    private final String value;

    private SocialNumber(String nissNumber) {
        this.value = nissNumber;
    }

    public static SocialNumber of(String nissNumber) {
        if (nissNumber == null || nissNumber.isBlank()) {
            throw new IllegalArgumentException("NISS cannot be null or empty.");
        }

        String clean = nissNumber.trim().replaceAll("\\s+", "");

        if (clean.length() != NISS_LENGTH) {
            throw new IllegalArgumentException("NISS must contain exactly 11 digits.");
        }

        if (!NISS_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException("NISS must start with 1 or 2 and contain only digits.");
        }

        return new SocialNumber(clean);
    }

    public String getValue() {
        return value;
    }

    public String getFormatted() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " " +
                value.substring(6, 9) + " " +
                value.substring(9, 11);
    }

    public String getMasked() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " " +
                value.substring(6, 9) + " **";
    }

    public String getPartiallyMasked() {
        return "*** *** " +
                value.substring(6, 9) + " " +
                value.substring(9, 11);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialNumber that = (SocialNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}