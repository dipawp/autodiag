package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * ****************************************************************************
 *
 * Classe.....: PidSupportChecker
 *
 * Tipo.......: Classe di servizio
 *
 * Descrizione:
 *
 * Interpreta i PID bitmap OBD-II.
 *
 * Esempio:
 *
 * 0100 -> indica quali PID 0101-0120 sono supportati.
 *
 * I quattro byte della risposta rappresentano
 * 32 bit di disponibilità.
 *
 * ****************************************************************************
 */
public class PidSupportChecker {

    /**
     * Verifica quali PID sono supportati
     * dalla bitmap ricevuta.
     *
     * @param basePid PID base della richiesta BITFIELD.
     * @param data quattro byte bitmap.
     *
     * @return insieme dei PID supportati.
     */
    @NonNull
    public Set<String> getSupportedPids(
            int basePid,
            @NonNull byte[] data) {

        if (data.length < 4) {

            throw new IllegalArgumentException(
                    "BITFIELD incompleto. "
                            + "Byte ricevuti: "
                            + data.length
            );
        }

        Set<String> supported =
                new HashSet<>();

        /*
         * La bitmap è composta da 32 bit.
         *
         * Il primo bit rappresenta:
         *
         * basePid + 1
         *
         * l'ultimo:
         *
         * basePid + 32
         */
        for (int bitIndex = 0;
             bitIndex < 32;
             bitIndex++) {

            int byteIndex =
                    bitIndex / 8;

            int bitInByte =
                    7 - (bitIndex % 8);

            int value =
                    data[byteIndex] & 0xFF;

            boolean available =
                    (value &
                            (1 << bitInByte))
                            != 0;

            if (available) {

                int pidValue =
                        basePid
                                + bitIndex
                                + 1;

                supported.add(
                        String.format(
                                java.util.Locale.US,
                                "01%02X",
                                pidValue
                        )
                );
            }
        }

        return supported;
    }

    /**
     * Converte un PID completo, ad esempio 0100,
     * nel valore numerico base.
     *
     * @param pid PID completo.
     *
     * @return valore numerico.
     */
    public int parsePid(
            @NonNull String pid) {

        String normalized =
                pid.trim()
                        .toUpperCase();

        if (normalized.length() != 4 ||
                !normalized.startsWith("01")) {

            throw new IllegalArgumentException(
                    "PID non valido: "
                            + pid
            );
        }

        try {

            return Integer.parseInt(
                    normalized.substring(2),
                    16
            );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "PID non valido: "
                            + pid,
                    exception
            );
        }
    }

    /**
     * Restituisce tutti i PID supportati
     * dalla bitmap.
     *
     * @param basePid PID base.
     * @param data bitmap.
     *
     * @return stringa leggibile.
     */
    @NonNull
    public String formatSupportedPids(
            int basePid,
            @NonNull byte[] data) {

        Set<String> supported =
                getSupportedPids(
                        basePid,
                        data
                );

        if (supported.isEmpty()) {

            return "(nessun PID supportato)";
        }

        java.util.List<String> sorted =
                new java.util.ArrayList<>(
                        supported
                );

        java.util.Collections.sort(
                sorted
        );

        StringBuilder result =
                new StringBuilder();

        for (int index = 0;
             index < sorted.size();
             index++) {

            if (index > 0) {

                result.append(", ");
            }

            result.append(
                    sorted.get(index)
            );
        }

        return result.toString();
    }
}