package com.global.lbc.features.human.resources.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa um Número de Identificação Fiscal (NIF) português.
 * Garante a imutabilidade e validação do NIF.
 */
public class FiscalNumber {

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{9}$");
    private static final int NIF_LENGTH = 9;

    private final String value;

    /**
     * Construtor privado para garantir validação através do método factory.
     */
    private FiscalNumber(String nifNumber) {
        this.value = nifNumber;
    }

    /**
     * Cria uma instância de FiscalNumber com validação.
     *
     * @param nifNumber o número do NIF como String
     * @return uma instância válida de FiscalNumber
     * @throws IllegalArgumentException se o NIF for inválido
     */
    public static FiscalNumber of(String nifNumber) {
        if (nifNumber == null || nifNumber.isBlank()) {
            throw new IllegalArgumentException("NIF não pode ser nulo ou vazio");
        }

        String cleanNif = nifNumber.trim().replaceAll("\\s+", "");

        if (!NIF_PATTERN.matcher(cleanNif).matches()) {
            throw new IllegalArgumentException("NIF deve conter exatamente 9 dígitos numéricos");
        }

        if (!isValidNif(cleanNif)) {
            throw new IllegalArgumentException("NIF inválido: falha na validação do dígito de controle");
        }

        return new FiscalNumber(cleanNif);
    }

    /**
     * Valida o NIF usando o algoritmo de validação português.
     * O último dígito é um dígito de controle calculado com base nos 8 primeiros.
     *
     * @param nif o número do NIF a validar
     * @return true se o NIF for válido, false caso contrário
     */
    private static boolean isValidNif(String nif) {
        int checkDigit = Character.getNumericValue(nif.charAt(8));
        int sum = 0;

        // Multiplica cada dígito por sua posição (9, 8, 7, 6, 5, 4, 3, 2)
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(nif.charAt(i)) * (9 - i);
        }

        int remainder = sum % 11;
        int expectedCheckDigit = (remainder == 0 || remainder == 1) ? 0 : 11 - remainder;

        return checkDigit == expectedCheckDigit;
    }

    /**
     * Retorna o valor do NIF como String.
     *
     * @return o número do NIF
     */
    public String getValue() {
        return value;
    }

    /**
     * Retorna o NIF formatado (XXX XXX XXX).
     *
     * @return o NIF formatado
     */
    public String getFormatted() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " " +
                value.substring(6, 9);
    }

    /**
     * Retorna o NIF mascarado para exibição segura (XXX XXX ***).
     *
     * @return o NIF mascarado
     */
    public String getMasked() {
        return value.substring(0, 3) + " " +
                value.substring(3, 6) + " ***";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiscalNumber that = (FiscalNumber) o;
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