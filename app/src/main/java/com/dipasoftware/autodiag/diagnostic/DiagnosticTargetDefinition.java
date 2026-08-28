package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticTargetDefinition
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Definisce il target diagnostico necessario per comunicare
 * con una specifica ECU.
 *
 * La classe contiene esclusivamente informazioni di indirizzamento.
 *
 * NON invia comandi.
 * NON comunica con la Connection.
 *
 * Supporta:
 *
 * - CAN 11 bit;
 * - CAN 29 bit;
 * - addressing fisico;
 * - addressing funzionale.
 *
 * Esempi:
 *
 * 11 bit:
 *
 * request  = 7E0
 * response = 7E8
 *
 * 29 bit:
 *
 * request  = 18DAF110
 * response = 18DA10F1
 *
 * ****************************************************************************
 */
public class DiagnosticTargetDefinition {

    /**
     * Protocollo diagnostico.
     *
     * Esempi:
     *
     * CAN
     * ISO_15765_4_CAN
     * UDS
     * KLINE
     * DOIP
     */
    @NonNull
    private final String protocol;

    /**
     * CAN request ID.
     */
    @NonNull
    private final String requestId;

    /**
     * CAN response ID.
     */
    @NonNull
    private final String responseId;

    /**
     * Modalità addressing.
     *
     * Esempi:
     *
     * PHYSICAL
     * FUNCTIONAL
     */
    @NonNull
    private final String addressingMode;

    /**
     * Lunghezza identificatore CAN.
     *
     * 11 oppure 29.
     */
    private final int canIdBits;

    /**
     * Costruttore.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing mode.
     * @param canIdBits lunghezza CAN ID: 11 oppure 29.
     */
    public DiagnosticTargetDefinition(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode,
            int canIdBits) {

        this.protocol =
                protocol.trim()
                        .toUpperCase();

        if (this.protocol.isEmpty()) {

            throw new IllegalArgumentException(
                    "Protocollo diagnostico vuoto."
            );
        }

        if (canIdBits != 11 &&
                canIdBits != 29) {

            throw new IllegalArgumentException(
                    "canIdBits deve essere 11 oppure 29."
            );
        }

        this.canIdBits =
                canIdBits;

        this.requestId =
                normalizeCanId(
                        requestId,
                        "requestId"
                );

        this.responseId =
                normalizeCanId(
                        responseId,
                        "responseId"
                );

        this.addressingMode =
                addressingMode.trim()
                        .toUpperCase();

        if (this.addressingMode.isEmpty()) {

            throw new IllegalArgumentException(
                    "Addressing mode vuoto."
            );
        }
    }

    /**
     * Costruttore compatibile con la versione precedente.
     *
     * Assume CAN 11 bit.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing mode.
     */
    public DiagnosticTargetDefinition(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode) {

        this(
                protocol,
                requestId,
                responseId,
                addressingMode,
                11
        );
    }

    /**
     * Restituisce il protocollo.
     *
     * @return protocollo.
     */
    @NonNull
    public String getProtocol() {

        return protocol;
    }

    /**
     * Restituisce il request ID.
     *
     * @return request ID.
     */
    @NonNull
    public String getRequestId() {

        return requestId;
    }

    /**
     * Restituisce il response ID.
     *
     * @return response ID.
     */
    @NonNull
    public String getResponseId() {

        return responseId;
    }

    /**
     * Restituisce la modalità addressing.
     *
     * @return addressing mode.
     */
    @NonNull
    public String getAddressingMode() {

        return addressingMode;
    }

    /**
     * Restituisce il numero di bit dell'identificatore CAN.
     *
     * @return 11 oppure 29.
     */
    public int getCanIdBits() {

        return canIdBits;
    }

    /**
     * Indica se l'ID CAN è standard 11 bit.
     *
     * @return true se 11 bit.
     */
    public boolean isStandardCanId() {

        return canIdBits == 11;
    }

    /**
     * Indica se l'ID CAN è extended 29 bit.
     *
     * @return true se 29 bit.
     */
    public boolean isExtendedCanId() {

        return canIdBits == 29;
    }

    /**
     * Indica se il protocollo utilizza CAN.
     *
     * @return true se CAN.
     */
    public boolean isCan() {

        return "CAN".equals(protocol)
                ||
                "ISO_15765_4_CAN".equals(protocol)
                ||
                "UDS".equals(protocol);
    }

    /**
     * Indica se il target è fisicamente indirizzato.
     *
     * @return true se physical.
     */
    public boolean isPhysical() {

        return "PHYSICAL".equals(
                addressingMode
        );
    }

    /**
     * Indica se il target è funzionalmente indirizzato.
     *
     * @return true se functional.
     */
    public boolean isFunctional() {

        return "FUNCTIONAL".equals(
                addressingMode
        );
    }

    /**
     * Normalizza e valida un CAN ID.
     *
     * 11 bit:
     *
     *     7E0
     *     07E0
     *     0x7E0
     *
     * 29 bit:
     *
     *     18DAF110
     *     0x18DAF110
     *
     * Il valore restituito mantiene gli zeri eventualmente
     * presenti nel JSON, ad eccezione del prefisso 0x.
     *
     * @param value valore.
     * @param name nome campo.
     *
     * @return ID normalizzato.
     */
    @NonNull
    private String normalizeCanId(
            @NonNull String value,
            @NonNull String name) {

        String normalized =
                value.trim()
                        .toUpperCase()
                        .replace(
                                "0X",
                                ""
                        );

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    name + " vuoto."
            );
        }

        for (
                int index = 0;
                index < normalized.length();
                index++
        ) {

            char character =
                    normalized.charAt(
                            index
                    );

            boolean valid =
                    (character >= '0'
                            && character <= '9')
                            ||
                            (character >= 'A'
                                    && character <= 'F');

            if (!valid) {

                throw new IllegalArgumentException(
                        name
                                + " non HEX: "
                                + value
                );
            }
        }

        int numericValue;

        try {

            numericValue =
                    Integer.parseInt(
                            normalized,
                            16
                    );

        } catch (
                NumberFormatException exception) {

            throw new IllegalArgumentException(
                    name
                            + " non valido: "
                            + value,
                    exception
            );
        }

        int maxValue =
                canIdBits == 11
                        ? 0x7FF
                        : 0x1FFFFFFF;

        if (numericValue < 0 ||
                numericValue > maxValue) {

            throw new IllegalArgumentException(
                    name
                            + " fuori dal range CAN "
                            + canIdBits
                            + "-bit: "
                            + value
            );
        }

        return normalized;
    }

    /**
     * Rappresentazione testuale.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "DiagnosticTargetDefinition{" +
                "protocol='" +
                protocol +
                '\'' +
                ", requestId='" +
                requestId +
                '\'' +
                ", responseId='" +
                responseId +
                '\'' +
                ", addressingMode='" +
                addressingMode +
                '\'' +
                ", canIdBits=" +
                canIdBits +
                '}';
    }
}