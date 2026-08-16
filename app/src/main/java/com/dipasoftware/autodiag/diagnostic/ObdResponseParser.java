package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: ObdResponseParser
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Analizza le risposte OBD-II provenienti dall'ELM327.
 *
 * Esempio:
 *
 *     Risposta:
 *
 *     41 0C 1A F8
 *
 *     Service:
 *
 *     41
 *
 *     PID:
 *
 *     0C
 *
 *     Dati:
 *
 *     1A F8
 *
 * La classe NON contiene formule PID.
 *
 * La conversione dei dati viene effettuata successivamente
 * da PidFormulaEvaluator utilizzando la formula presente
 * nel JSON.
 *
 * ****************************************************************************
 */
public class ObdResponseParser {

    /**
     * Service OBD-II positivo per Mode 01.
     *
     * Mode 01 -> risposta 41.
     */
    private static final int POSITIVE_SERVICE_01 = 0x41;

    /**
     * Analizza una risposta OBD-II.
     *
     * @param response risposta testuale ELM327.
     * @param expectedPid PID richiesto, ad esempio "010C".
     *
     * @return risultato del parsing.
     *
     * @throws IllegalArgumentException risposta non valida.
     */
    @NonNull
    public ObdResponse parse(
            @NonNull String response,
            @NonNull String expectedPid) {

        List<Integer> bytes =
                parseHexBytes(response);

        if (bytes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Risposta OBD-II vuota."
            );
        }

        /*
         * Una risposta Mode 01 positiva deve
         * iniziare con 41.
         */
        if (bytes.get(0) !=
                POSITIVE_SERVICE_01) {

            throw new IllegalArgumentException(
                    "Risposta OBD-II non positiva: "
                            + response
            );
        }

        /*
         * Normalizza il PID richiesto.
         *
         * Esempio:
         *
         * 010C -> 0C
         */
        int pid =
                parsePid(
                        expectedPid
                );

        /*
         * Il secondo byte deve essere
         * il PID richiesto.
         */
        if (bytes.size() < 2 ||
                bytes.get(1) != pid) {

            throw new IllegalArgumentException(
                    "PID inatteso. "
                            + "Atteso: "
                            + formatHex(pid)
                            + ", ricevuto: "
                            + (
                            bytes.size() > 1
                                    ? formatHex(bytes.get(1))
                                    : "(nessuno)"
                    )
            );
        }

        /*
         * Tutti i byte successivi al
         * service e al PID sono dati.
         */
        List<Integer> data =
                new ArrayList<>();

        for (int index = 2;
             index < bytes.size();
             index++) {

            data.add(
                    bytes.get(index)
            );
        }

        if (data.isEmpty()) {

            throw new IllegalArgumentException(
                    "Nessun dato presente nella risposta "
                            + "del PID "
                            + expectedPid
            );
        }

        /*
         * Conversione Integer -> byte.
         */
        byte[] dataBytes =
                new byte[data.size()];

        for (int index = 0;
             index < data.size();
             index++) {

            dataBytes[index] = (byte) data.get(index).intValue();
        }

        return new ObdResponse(
                POSITIVE_SERVICE_01,
                pid,
                dataBytes
        );
    }

    /**
     * Converte la risposta ELM327 in byte esadecimali.
     *
     * Sono tollerati:
     *
     * - spazi;
     * - CR;
     * - LF;
     * - prompt >;
     * - testo SEARCHING non valido;
     *
     * Il metodo considera solamente token
     * esadecimali validi.
     *
     * @param response risposta ELM327.
     *
     * @return lista di byte.
     */
    @NonNull
    private List<Integer> parseHexBytes(
            @NonNull String response) {

        List<Integer> bytes =
                new ArrayList<>();

        String normalized =
                response
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(">", " ")
                        .trim();

        if (normalized.isEmpty()) {

            return bytes;
        }

        String[] tokens =
                normalized.split(
                        "\\s+"
                );

        for (String token :
                tokens) {

            if (token == null ||
                    token.trim().isEmpty()) {

                continue;
            }

            String cleanToken =
                    token.trim();

            /*
             * Un byte OBD deve essere
             * rappresentato da esattamente
             * due caratteri esadecimali.
             */
            if (cleanToken.length() != 2) {

                continue;
            }

            if (!isHexByte(cleanToken)) {

                continue;
            }

            try {

                bytes.add(
                        Integer.parseInt(
                                cleanToken,
                                16
                        )
                );

            } catch (NumberFormatException ignored) {

                /*
                 * Token non interpretabile.
                 */
            }
        }

        return bytes;
    }

    /**
     * Converte il PID completo nel relativo
     * byte PID.
     *
     * Esempio:
     *
     * 010C -> 0C
     *
     * @param pidCode PID.
     *
     * @return valore PID.
     */
    private int parsePid(
            @NonNull String pidCode) {

        String normalized =
                pidCode
                        .trim()
                        .toUpperCase();

        /*
         * Per Mode 01 ci aspettiamo
         * normalmente:
         *
         * 01XX
         */
        if (normalized.length() != 4) {

            throw new IllegalArgumentException(
                    "PID non valido: "
                            + pidCode
            );
        }

        if (!normalized.startsWith("01")) {

            throw new IllegalArgumentException(
                    "PID non appartenente al Mode 01: "
                            + pidCode
            );
        }

        String pidPart =
                normalized.substring(
                        2
                );

        try {

            return Integer.parseInt(
                    pidPart,
                    16
            );

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "PID non valido: "
                            + pidCode,
                    exception
            );
        }
    }

    /**
     * Verifica se una stringa rappresenta
     * un byte esadecimale.
     *
     * @param value valore.
     *
     * @return true se valido.
     */
    private boolean isHexByte(
            @NonNull String value) {

        if (value.length() != 2) {

            return false;
        }

        for (int index = 0;
             index < value.length();
             index++) {

            char character =
                    Character.toUpperCase(
                            value.charAt(index)
                    );

            boolean valid =
                    (
                            character >= '0'
                                    && character <= '9'
                    )
                            ||
                            (
                                    character >= 'A'
                                            && character <= 'F'
                            );

            if (!valid) {

                return false;
            }
        }

        return true;
    }

    /**
     * Formatta un byte in esadecimale.
     *
     * @param value valore.
     *
     * @return valore formattato.
     */
    @NonNull
    private String formatHex(
            int value) {

        return String.format(
                java.util.Locale.US,
                "%02X",
                value & 0xFF
        );
    }

    /**
     * Risultato del parsing OBD.
     */
    public static class ObdResponse {

        /**
         * Service positivo.
         */
        private final int service;

        /**
         * PID.
         */
        private final int pid;

        /**
         * Dati PID.
         */
        @NonNull
        private final byte[] data;

        /**
         * Costruttore.
         *
         * @param service service OBD.
         * @param pid PID.
         * @param data dati.
         */
        public ObdResponse(
                int service,
                int pid,
                @NonNull byte[] data) {

            this.service = service;
            this.pid = pid;
            this.data = data;
        }

        /**
         * Restituisce il service.
         *
         * @return service.
         */
        public int getService() {

            return service;
        }

        /**
         * Restituisce il PID.
         *
         * @return PID.
         */
        public int getPid() {

            return pid;
        }

        /**
         * Restituisce i dati grezzi.
         *
         * @return dati.
         */
        @NonNull
        public byte[] getData() {

            return data.clone();
        }

        /**
         * Restituisce i dati in formato HEX.
         *
         * @return dati HEX.
         */
        @NonNull
        public String getDataHex() {

            StringBuilder result =
                    new StringBuilder();

            for (int index = 0;
                 index < data.length;
                 index++) {

                if (index > 0) {

                    result.append(" ");
                }

                result.append(
                        String.format(
                                java.util.Locale.US,
                                "%02X",
                                data[index] & 0xFF
                        )
                );
            }

            return result.toString();
        }
    }
}
