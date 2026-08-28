package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: ObdVehicleInformationParser
 *
 * Tipo.......: Parser
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Analizza le risposte OBD-II Mode 09 utilizzate per ottenere
 * informazioni del veicolo.
 *
 * Attualmente supporta:
 *
 *     09 02 = VIN
 *
 * Risposta positiva:
 *
 *     49 02 ...
 *
 * Il parser è volutamente separato da ObdResponseParser perché
 * Mode 09 può utilizzare una risposta multi-frame e non ha la
 * stessa struttura del normale PID Mode 01.
 *
 * ****************************************************************************
 */
public class ObdVehicleInformationParser {

    /**
     * Service positivo Mode 09.
     */
    private static final int POSITIVE_SERVICE =
            0x49;

    /**
     * PID VIN.
     */
    private static final int VIN_PID =
            0x02;

    /**
     * Analizza una risposta OBD-II Mode 09 PID 02
     * contenente il VIN.
     *
     * Struttura della risposta positiva:
     *
     *     49 02 01 [17 byte VIN]
     *
     * dove:
     *
     *     49 = risposta positiva al Mode 09
     *     02 = PID VIN
     *     01 = numero di record/messaggi di dati
     *     VIN = 17 caratteri
     *
     * Il byte 01 NON appartiene al VIN.
     *
     * @param response risposta ELM327.
     * @param expectedRequest richiesta attesa.
     *
     * @return risultato VIN.
     *
     * @throws IllegalArgumentException risposta non valida.
     */
    @NonNull
    public VehicleInformationResponse parseVin(
            @NonNull String response,
            @NonNull String expectedRequest) {

        List<Integer> bytes =
                parseHexBytes(
                        response
                );

        if (bytes.isEmpty()) {

            throw new IllegalArgumentException(
                    "Risposta Mode 09 vuota."
            );
        }

        String request =
                normalizeHex(
                        expectedRequest
                );

        if (!"0902".equals(request)) {

            throw new IllegalArgumentException(
                    "Richiesta VIN non valida: "
                            + expectedRequest
            );
        }

        /*
         * -------------------------------------------------------------
         * RICERCA RISPOSTA POSITIVA
         * -------------------------------------------------------------
         *
         * Cerchiamo:
         *
         *     49 02
         *
         * La struttura attesa è:
         *
         *     49 02 01 [VIN]
         *
         * Il byte 01 rappresenta il numero di record
         * e non deve essere incluso nel VIN.
         */
        int responseStart =
                -1;

        for (
                int index = 0;
                index < bytes.size() - 1;
                index++
        ) {

            if (bytes.get(index) ==
                    POSITIVE_SERVICE
                    &&
                    bytes.get(index + 1) ==
                            VIN_PID) {

                responseStart =
                        index;

                break;
            }
        }

        if (responseStart < 0) {

            throw new IllegalArgumentException(
                    "Risposta VIN non riconosciuta: "
                            + response
            );
        }

        /*
         * -------------------------------------------------------------
         * INIZIO DATI VIN
         * -------------------------------------------------------------
         */

        int dataStart =
                responseStart + 2;

        /*
         * Dopo 49 02 ci si aspetta il byte del numero
         * di record/messaggi.
         *
         * Se presente, viene saltato.
         */
        if (dataStart < bytes.size()) {

            int recordCount =
                    bytes.get(
                            dataStart
                    );

            /*
             * Per PID 02 il valore atteso normalmente è 01.
             *
             * Non imponiamo però rigidamente il valore per
             * mantenere compatibilità con eventuali risposte
             * implementate in modo differente.
             *
             * Il byte viene comunque escluso dai dati VIN.
             */
            if (recordCount >= 0) {

                dataStart++;
            }
        }

        /*
         * -------------------------------------------------------------
         * ESTRAZIONE VIN
         * -------------------------------------------------------------
         *
         * Il VIN contiene 17 caratteri ASCII alfanumerici.
         *
         * Ignoriamo:
         *
         * - byte di padding;
         * - CR/LF;
         * - eventuali byte non VIN.
         *
         * Ci fermiamo esattamente a 17 caratteri.
         */
        StringBuilder vin =
                new StringBuilder(
                        17
                );

        for (
                int index = dataStart;
                index < bytes.size()
                        &&
                        vin.length() < 17;
                index++
        ) {

            int current =
                    bytes.get(index);

            if (isVinCharacter(
                    current
            )) {

                vin.append(
                        (char) current
                );
            }
        }

        String result =
                vin.toString().trim();

        /*
         * -------------------------------------------------------------
         * VALIDAZIONE
         * -------------------------------------------------------------
         */

        if (result.length() != 17) {

            throw new IllegalArgumentException(
                    "VIN non valido o incompleto. "
                            + "Lunghezza: "
                            + result.length()
                            + ". Risposta: "
                            + response
            );
        }

        return new VehicleInformationResponse(
                POSITIVE_SERVICE,
                VIN_PID,
                result
        );
    }

    /**
     * Converte la risposta ELM327 in byte.
     *
     * Vengono considerati solamente token HEX a due caratteri.
     *
     * @param response risposta.
     *
     * @return byte.
     */
    @NonNull
    private List<Integer> parseHexBytes(
            @NonNull String response) {

        List<Integer> bytes =
                new ArrayList<>();

        String normalized =
                response
                        .replace(
                                "\r",
                                " "
                        )
                        .replace(
                                "\n",
                                " "
                        )
                        .replace(
                                ">",
                                " "
                        )
                        .trim();

        if (normalized.isEmpty()) {

            return bytes;
        }

        String[] tokens =
                normalized.split(
                        "\\s+"
                );

        for (
                String token :
                tokens
        ) {

            if (token == null) {

                continue;
            }

            String clean =
                    token.trim();

            if (clean.length() != 2) {

                continue;
            }

            if (!isHexByte(
                    clean
            )) {

                continue;
            }

            try {

                bytes.add(
                        Integer.parseInt(
                                clean,
                                16
                        )
                );

            } catch (
                    NumberFormatException ignored) {

                // Token ignorato.
            }
        }

        return bytes;
    }

    /**
     * Verifica se un byte può appartenere al VIN.
     *
     * Sono ammessi caratteri alfanumerici VIN standard.
     *
     * @param value byte.
     *
     * @return true se valido.
     */
    private boolean isVinCharacter(
            int value) {

        return value >= '0'
                && value <= '9'
                ||
                value >= 'A'
                        && value <= 'Z';
    }

    /**
     * Verifica un byte HEX.
     */
    private boolean isHexByte(
            @NonNull String value) {

        if (value.length() != 2) {

            return false;
        }

        for (
                int index = 0;
                index < value.length();
                index++
        ) {

            char character =
                    Character.toUpperCase(
                            value.charAt(index)
                    );

            boolean valid =
                    character >= '0'
                            && character <= '9'
                            ||
                            character >= 'A'
                                    && character <= 'F';

            if (!valid) {

                return false;
            }
        }

        return true;
    }

    /**
     * Normalizza una richiesta HEX.
     */
    @NonNull
    private String normalizeHex(
            @NonNull String value) {

        return value
                .replace(
                        " ",
                        ""
                )
                .replace(
                        "\r",
                        ""
                )
                .replace(
                        "\n",
                        ""
                )
                .replace(
                        ">",
                        ""
                )
                .trim()
                .toUpperCase();
    }

    /**
     * Risultato del parsing Mode 09.
     */
    public static class VehicleInformationResponse {

        /**
         * Service positivo.
         */
        private final int service;

        /**
         * PID.
         */
        private final int pid;

        /**
         * VIN.
         */
        @NonNull
        private final String vin;

        /**
         * Costruttore.
         */
        public VehicleInformationResponse(
                int service,
                int pid,
                @NonNull String vin) {

            this.service =
                    service;

            this.pid =
                    pid;

            this.vin =
                    vin;
        }

        public int getService() {

            return service;
        }

        public int getPid() {

            return pid;
        }

        @NonNull
        public String getVin() {

            return vin;
        }
    }
}