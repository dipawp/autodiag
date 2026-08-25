package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: UdsResponseParser
 *
 * Tipo.......: Parser
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Interpreta risposte UDS relative alla lettura di un Data Identifier
 * tramite il servizio ReadDataByIdentifier (0x22).
 *
 * Richiesta:
 *
 *     22 F1 90
 *
 * Risposta positiva:
 *
 *     62 F1 90 DATA...
 *
 * Risposta negativa:
 *
 *     7F 22 NRC
 *
 * Il parser non comunica con l'ECU e non utilizza la Connection.
 *
 * La responsabilità è esclusivamente:
 *
 * - normalizzare la risposta;
 * - verificare il service positivo;
 * - verificare il DID richiesto;
 * - distinguere risposta positiva e negativa;
 * - estrarre i byte dati.
 *
 * ****************************************************************************
 */
public class UdsResponseParser {

    /**
     * Service UDS ReadDataByIdentifier.
     */
    private static final int SERVICE_READ_DATA_BY_IDENTIFIER =
            0x22;

    /**
     * Service UDS positivo corrispondente a 0x22.
     */
    private static final int POSITIVE_READ_DATA_BY_IDENTIFIER =
            0x62;

    /**
     * Negative Response Service.
     */
    private static final int NEGATIVE_RESPONSE =
            0x7F;

    /**
     * Numero di byte necessari per rappresentare
     * un Data Identifier UDS.
     */
    private static final int DID_LENGTH =
            2;

    /**
     * Risultato della risposta UDS.
     */
    public static class UdsResponse {

        /**
         * Service ricevuto.
         */
        private final int service;

        /**
         * DID ricevuto.
         */
        private final int did;

        /**
         * Byte dati.
         */
        @NonNull
        private final byte[] data;

        /**
         * Indica se la risposta è positiva.
         */
        private final boolean positive;

        /**
         * Negative Response Code.
         *
         * Vale -1 per una risposta positiva.
         */
        private final int negativeResponseCode;

        /**
         * Risposta originale normalizzata.
         */
        @NonNull
        private final String rawResponse;

        /**
         * Costruttore.
         *
         * @param service service ricevuto.
         * @param did DID.
         * @param data dati.
         * @param positive risposta positiva.
         * @param negativeResponseCode NRC.
         * @param rawResponse risposta normalizzata.
         */
        UdsResponse(
                int service,
                int did,
                @NonNull byte[] data,
                boolean positive,
                int negativeResponseCode,
                @NonNull String rawResponse) {

            this.service =
                    service;

            this.did =
                    did;

            this.data =
                    data.clone();

            this.positive =
                    positive;

            this.negativeResponseCode =
                    negativeResponseCode;

            this.rawResponse =
                    rawResponse;
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
         * Restituisce il DID.
         *
         * @return DID.
         */
        public int getDid() {

            return did;
        }

        /**
         * Restituisce una copia dei dati.
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

            return bytesToHex(
                    data
            );
        }

        /**
         * Indica se la risposta è positiva.
         *
         * @return true se positiva.
         */
        public boolean isPositive() {

            return positive;
        }

        /**
         * Indica se la risposta è negativa.
         *
         * @return true se negativa.
         */
        public boolean isNegative() {

            return !positive;
        }

        /**
         * Restituisce il Negative Response Code.
         *
         * @return NRC oppure -1.
         */
        public int getNegativeResponseCode() {

            return negativeResponseCode;
        }

        /**
         * Restituisce la risposta normalizzata.
         *
         * @return risposta.
         */
        @NonNull
        public String getRawResponse() {

            return rawResponse;
        }
    }

    /**
     * Analizza una risposta UDS relativa a una richiesta 0x22.
     *
     * @param response risposta ricevuta dall'ELM327.
     * @param request richiesta inviata.
     *
     * @return risposta UDS interpretata.
     *
     * @throws IllegalArgumentException risposta non valida.
     */
    @NonNull
    public UdsResponse parse(
            @NonNull String response,
            @NonNull String request) {

        String normalized =
                normalizeResponse(
                        response
                );

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "Risposta UDS vuota."
            );
        }

        byte[] bytes =
                parseHexBytes(
                        normalized
                );

        if (bytes.length < 1) {

            throw new IllegalArgumentException(
                    "Risposta UDS senza byte."
            );
        }

        int service =
                bytes[0] & 0xFF;

        int requestedDid =
                extractRequestedDid(
                        request
                );

        /*
         * ---------------------------------------------------------
         * RISPOSTA NEGATIVA
         * ---------------------------------------------------------
         *
         * 7F 22 NRC
         */
        if (service ==
                NEGATIVE_RESPONSE) {

            if (bytes.length < 3) {

                throw new IllegalArgumentException(
                        "Risposta negativa UDS incompleta."
                );
            }

            int requestedService =
                    bytes[1] & 0xFF;

            int negativeResponseCode =
                    bytes[2] & 0xFF;

            if (requestedService !=
                    SERVICE_READ_DATA_BY_IDENTIFIER) {

                throw new IllegalArgumentException(
                        String.format(
                                Locale.US,
                                "Service negativo inatteso: %02X",
                                requestedService
                        )
                );
            }

            return new UdsResponse(
                    service,
                    requestedDid,
                    new byte[0],
                    false,
                    negativeResponseCode,
                    normalized
            );
        }

        /*
         * ---------------------------------------------------------
         * RISPOSTA POSITIVA
         * ---------------------------------------------------------
         *
         * 62 DID_HIGH DID_LOW DATA...
         */
        if (service !=
                POSITIVE_READ_DATA_BY_IDENTIFIER) {

            throw new IllegalArgumentException(
                    String.format(
                            Locale.US,
                            "Service UDS inatteso: %02X",
                            service
                    )
            );
        }

        if (bytes.length <
                1 + DID_LENGTH) {

            throw new IllegalArgumentException(
                    "Risposta positiva UDS "
                            + "incompleta."
            );
        }

        int receivedDid =
                ((bytes[1] & 0xFF) << 8)
                        |
                        (bytes[2] & 0xFF);

        if (requestedDid >= 0 &&
                receivedDid != requestedDid) {

            throw new IllegalArgumentException(
                    String.format(
                            Locale.US,
                            "DID inatteso. "
                                    + "Richiesto: %04X, "
                                    + "ricevuto: %04X",
                            requestedDid,
                            receivedDid
                    )
            );
        }

        byte[] data =
                new byte[
                        bytes.length
                                - 3
                        ];

        System.arraycopy(
                bytes,
                3,
                data,
                0,
                data.length
        );

        return new UdsResponse(
                service,
                receivedDid,
                data,
                true,
                -1,
                normalized
        );
    }

    /**
     * Estrae il DID dalla richiesta UDS.
     *
     * La richiesta attesa è:
     *
     * 22 F1 90
     *
     * oppure:
     *
     * 22F190
     *
     * @param request richiesta.
     *
     * @return DID oppure -1 se non determinabile.
     */
    private int extractRequestedDid(
            @NonNull String request) {

        String normalized =
                normalizeResponse(
                        request
                );

        byte[] bytes =
                parseHexBytes(
                        normalized
                );

        if (bytes.length < 3) {

            throw new IllegalArgumentException(
                    "Richiesta UDS 0x22 incompleta."
            );
        }

        int service =
                bytes[0] & 0xFF;

        if (service !=
                SERVICE_READ_DATA_BY_IDENTIFIER) {

            throw new IllegalArgumentException(
                    String.format(
                            Locale.US,
                            "Service richiesta UDS inatteso: %02X",
                            service
                    )
            );
        }

        return ((bytes[1] & 0xFF) << 8)
                |
                (bytes[2] & 0xFF);
    }

    /**
     * Normalizza una stringa HEX.
     *
     * @param response stringa originale.
     *
     * @return stringa normalizzata.
     */
    @NonNull
    private String normalizeResponse(
            @NonNull String response) {

        return response
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
                .trim()
                .toUpperCase(
                        Locale.US
                )
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    /**
     * Converte una stringa HEX in array di byte.
     *
     * @param hex stringa HEX.
     *
     * @return byte.
     */
    @NonNull
    private byte[] parseHexBytes(
            @NonNull String hex) {

        String normalized =
                hex.replace(
                        " ",
                        ""
                );

        if (normalized.isEmpty()) {

            return new byte[0];
        }

        if ((normalized.length() % 2) != 0) {

            throw new IllegalArgumentException(
                    "Numero dispari di caratteri HEX: "
                            + normalized
            );
        }

        byte[] result =
                new byte[
                        normalized.length() / 2
                        ];

        for (int index = 0;
             index < result.length;
             index++) {

            int start =
                    index * 2;

            String token =
                    normalized.substring(
                            start,
                            start + 2
                    );

            try {

                result[index] =
                        (byte) Integer.parseInt(
                                token,
                                16
                        );

            } catch (
                    NumberFormatException exception) {

                throw new IllegalArgumentException(
                        "Byte HEX non valido: "
                                + token,
                        exception
                );
            }
        }

        return result;
    }

    /**
     * Converte byte in stringa HEX separata da spazi.
     *
     * @param data byte.
     *
     * @return HEX.
     */
    @NonNull
    private static String bytesToHex(
            @NonNull byte[] data) {

        if (data.length == 0) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (int index = 0;
             index < data.length;
             index++) {

            if (index > 0) {

                result.append(
                        ' '
                );
            }

            result.append(
                    String.format(
                            Locale.US,
                            "%02X",
                            data[index] & 0xFF
                    )
            );
        }

        return result.toString();
    }
}