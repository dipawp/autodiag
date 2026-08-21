package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * ****************************************************************************
 *
 * Classe.....: PidSupportChecker
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Interpreta le bitmap OBD-II dei PID supportati.
 *
 * Una richiesta come:
 *
 * 0100
 *
 * restituisce una bitmap di 4 byte che indica quali PID:
 *
 * 0101 - 0120
 *
 * sono supportati dalla ECU.
 *
 * Una richiesta:
 *
 * 0120
 *
 * indica invece il supporto dei PID:
 *
 * 0121 - 0140
 *
 * e così via.
 *
 * IMPORTANTE:
 *
 * Questa classe NON mantiene lo stato complessivo dei PID supportati
 * dalla ECU.
 *
 * Questa classe si occupa esclusivamente di interpretare una singola
 * bitmap.
 *
 * La gestione dell'insieme complessivo dei PID supportati sarà
 * responsabilità del componente che esegue la scansione delle bitmap.
 *
 * ****************************************************************************
 */
public class PidSupportChecker {

    /**
     * Numero di byte previsti da una bitmap OBD-II
     * dei PID supportati.
     *
     * Ogni bitmap contiene 32 bit e quindi descrive
     * 32 PID consecutivi.
     */
    private static final int BITFIELD_BYTE_COUNT = 4;

    /**
     * Numero di bit contenuti nella bitmap.
     *
     * Quattro byte × otto bit = 32 PID.
     */
    private static final int BITFIELD_BIT_COUNT = 32;

    /**
     * Primo valore PID utilizzabile come bitmap base.
     *
     * Il valore 00 rappresenta la bitmap che descrive
     * i PID 01-20.
     */
    private static final int MIN_BASE_PID = 0x00;

    /**
     * Massimo valore PID utilizzabile come base per
     * le bitmap standard fino alla fascia E0.
     */
    private static final int MAX_BASE_PID = 0xE0;

    /**
     * Verifica quali PID sono supportati dalla bitmap ricevuta.
     *
     * Esempio:
     *
     * basePid = 0x00
     *
     * La bitmap descriverà:
     *
     * 01 - 20
     *
     * basePid = 0x20
     *
     * La bitmap descriverà:
     *
     * 21 - 40
     *
     * @param basePid valore numerico del PID base.
     * @param data quattro byte della bitmap.
     *
     * @return insieme dei PID supportati.
     *
     * @throws IllegalArgumentException se la bitmap non contiene
     *                                  almeno quattro byte oppure
     *                                  il PID base non è valido.
     */
    @NonNull
    public Set<String> getSupportedPids(
            int basePid,
            @NonNull byte[] data) {

        validateBasePid(basePid);

        if (data.length < BITFIELD_BYTE_COUNT) {

            throw new IllegalArgumentException(
                    "BITFIELD incompleto. " +
                            "Byte ricevuti: " +
                            data.length +
                            ", richiesti: " +
                            BITFIELD_BYTE_COUNT
            );
        }

        Set<String> supported =
                new HashSet<>();

        /*
         * La bitmap contiene 32 bit.
         *
         * Il bit più significativo del primo byte
         * rappresenta il primo PID della fascia.
         *
         * Esempio per 0100:
         *
         * bit 7 -> 0101
         * bit 6 -> 0102
         * ...
         * bit 0 -> 0108
         *
         * Il secondo byte continua con:
         *
         * 0109 ... 0110
         *
         * e così via.
         */
        for (int bitIndex = 0;
             bitIndex < BITFIELD_BIT_COUNT;
             bitIndex++) {

            int byteIndex =
                    bitIndex / 8;

            int bitInByte =
                    7 - (bitIndex % 8);

            int value =
                    data[byteIndex] & 0xFF;

            boolean supportedBit =
                    (value &
                            (1 << bitInByte))
                            != 0;

            if (!supportedBit) {
                continue;
            }

            int pidValue =
                    basePid +
                            bitIndex +
                            1;

            /*
             * La fascia E0 arriva fino a FF.
             *
             * I valori oltre FF non sono PID validi.
             */
            if (pidValue > 0xFF) {
                continue;
            }

            supported.add(
                    formatPid(pidValue)
            );
        }

        return supported;
    }

    /**
     * Converte un PID completo, ad esempio 0100,
     * nel valore numerico della parte PID.
     *
     * Esempio:
     *
     * 0100 -> 0x00
     *
     * 0120 -> 0x20
     *
     * 0140 -> 0x40
     *
     * @param pid PID completo.
     *
     * @return valore numerico del PID.
     *
     * @throws IllegalArgumentException se il PID non è valido.
     */
    public int parsePid(
            @NonNull String pid) {

        String normalized =
                pid.trim()
                        .toUpperCase(Locale.US);

        if (normalized.length() != 4) {

            throw new IllegalArgumentException(
                    "PID non valido: " +
                            pid
            );
        }

        if (!normalized.startsWith("01")) {

            throw new IllegalArgumentException(
                    "Modalità OBD-II non supportata " +
                            "dal PidSupportChecker: " +
                            pid
            );
        }

        try {

            return Integer.parseInt(
                    normalized.substring(2),
                    16
            );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "PID non valido: " +
                            pid,
                    exception
            );
        }
    }

    /**
     * Verifica se un valore PID può essere utilizzato
     * come base di una bitmap standard.
     *
     * Le bitmap standard utilizzano gli intervalli:
     *
     * 00
     * 20
     * 40
     * 60
     * 80
     * A0
     * C0
     * E0
     *
     * @param basePid valore numerico del PID base.
     *
     * @return true se il valore rappresenta una base bitmap valida.
     */
    public boolean isSupportedBasePid(
            int basePid) {

        return basePid >= MIN_BASE_PID
                && basePid <= MAX_BASE_PID
                && (basePid % 0x20) == 0;
    }

    /**
     * Converte una stringa PID completa nel valore base
     * necessario per una bitmap.
     *
     * Esempio:
     *
     * 0100 -> 0x00
     *
     * 0120 -> 0x20
     *
     * 0140 -> 0x40
     *
     * @param pid PID bitmap completo.
     *
     * @return valore numerico base della bitmap.
     *
     * @throws IllegalArgumentException se il PID non rappresenta
     *                                  una bitmap standard.
     */
    public int parseBitmapBasePid(
            @NonNull String pid) {

        int basePid =
                parsePid(pid);

        if (!isSupportedBasePid(basePid)) {

            throw new IllegalArgumentException(
                    "PID non valido come base BITFIELD: " +
                            pid
            );
        }

        return basePid;
    }

    /**
     * Restituisce una rappresentazione leggibile
     * dei PID supportati dalla bitmap.
     *
     * I PID vengono ordinati numericamente.
     *
     * @param basePid valore numerico del PID base.
     * @param data bitmap di quattro byte.
     *
     * @return elenco dei PID supportati.
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

        List<String> sorted =
                new ArrayList<>(
                        supported
                );

        Collections.sort(
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

    /**
     * Formatta un valore numerico PID nel formato
     * completo utilizzato dall'applicazione.
     *
     * Esempio:
     *
     * 0x04 -> 0104
     *
     * 0x0C -> 010C
     *
     * 0x42 -> 0142
     *
     * @param pidValue valore numerico del PID.
     *
     * @return PID completo in formato esadecimale.
     */
    @NonNull
    private String formatPid(
            int pidValue) {

        return String.format(
                Locale.US,
                "01%02X",
                pidValue
        );
    }

    /**
     * Verifica che il valore base della bitmap
     * appartenga a uno degli intervalli standard.
     *
     * @param basePid valore numerico base.
     *
     * @throws IllegalArgumentException se il valore non è valido.
     */
    private void validateBasePid(
            int basePid) {

        if (!isSupportedBasePid(basePid)) {

            throw new IllegalArgumentException(
                    "Base PID BITFIELD non valida: " +
                            String.format(
                                    Locale.US,
                                    "%02X",
                                    basePid
                            )
            );
        }
    }
}