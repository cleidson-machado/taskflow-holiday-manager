package com.global.lbc.features.human.resources.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa um Número de Identificação de Segurança Social (NISS) português.
 * O NISS é composto por 11 dígitos numéricos com validação por dígitos de controle.
 * Garante a imutabilidade e validação do NISS.
 */
public class SocialNumber {

    private static final Pattern NISS_PATTERN = Pattern.compile("^[0-9]{11}$");
    private static final int NISS_LENGTH = 11;

    private final String value;

    /**
     * Construtor privado para garantir validação através do método factory.
     */
    private SocialNumber(String nissNumber) {
        this.value = nissNumber;
    }

    /**
     * Cria uma instância de SocialNumber com validação.
     *
     * @param nissNumber o número do NISS como String
     * @return uma instância válida de SocialNumber
     * @throws IllegalArgumentException se o NISS for inválido
     */
    public static SocialNumber of(String nissNumber) {
        if (nissNumber == null || nissNumber.isBlank()) {
            throw new IllegalArgumentException("NISS não pode ser nulo ou vazio");
        }

        String cleanNiss = nissNumber.trim().replaceAll("\\s+", "");

        if (!NISS_PATTERN.matcher(cleanNiss).matches()) {
            throw new IllegalArgumentException("NISS deve conter exatamente 11 dígitos numéricos");
        }

        if (!isValidNiss(cleanNiss)) {
            throw new IllegalArgumentException("NISS inválido: falha na validação dos dígitos de controle");
        }

        return new SocialNumber(cleanNiss);
    }

    /**
     * Valida o NISS usando o algoritmo de validação português.
     * O NISS possui 11 dígitos, sendo os dois últimos dígitos de controle.
     *
     * Algoritmo de validação:
     * 1. Os primeiros 9 dígitos são multiplicados por 9, 8, 7, 6, 5, 4, 3, 2, 1 respectivamente
     * 2. A soma é dividida por 10 e o resto é comparado com o 10º dígito
     * 3. Os primeiros 10 dígitos são multiplicados por 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 respectivamente
     * 4. A soma é dividida por 10 e o resto é comparado com o 11º dígito
     *
     * @param niss o número do NISS a validar
     * @return true se o NISS for válido, false caso contrário
     */
    private static boolean isValidNiss(String niss) {
        // Validação do primeiro dígito de controle (10º dígito)
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += Character.getNumericValue(niss.charAt(i)) * (9 - i);
        }
        int checkDigit1 = sum1 % 10;
        int expectedCheckDigit1 = Character.getNumericValue(niss.charAt(9));

        if (checkDigit1 != expectedCheckDigit1) {
            return false;
        }

        // Validação do segundo dígito de controle (11º dígito)
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += Character.getNumericValue(niss.charAt(i)) * (10 - i);
        }
        int checkDigit2 = sum2 % 10;
        int expectedCheckDigit2 = Character.getNumericValue(niss.charAt(10));

        return checkDigit2 == expectedCheckDigit2;
    }

    /**
     * Retorna o valor do NISS como String.
     *
     * @return o número do NISS
     */
    public String getValue() {
        return value;
    }

    /**
     * Retorna o NISS formatado (XXX XXX XXX XX).
     *
     * @return o NISS formatado
     */
    public String getFormatted() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " " +
                value.substring(6, 9) + " " +
                value.substring(9, 11);
    }

    /**
     * Retorna o NISS mascarado para exibição segura (XXX XXX XXX **).
     *
     * @return o NISS mascarado
     */
    public String getMasked() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " " +
                value.substring(6, 9) + " **";
    }

    /**
     * Retorna o NISS parcialmente mascarado (*** *** XXX XX).
     * Útil para exibir apenas os últimos dígitos.
     *
     * @return o NISS parcialmente mascarado
     */
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