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
 * La classe contiene esclusivamente informazioni di indirizzamento
 * e configurazione del bus diagnostico.
 *
 * NON invia comandi.
 * NON comunica con la Connection.
 *
 * Supporta:
 *
 * - CAN 11 bit;
 * - CAN 29 bit;
 * - addressing fisico;
 * - addressing funzionale;
 * - bitrate CAN.
 *
 * ****************************************************************************
 */
public class DiagnosticTargetDefinition {

    /**
     * Protocollo diagnostico.
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
     */
    @NonNull
    private final String addressingMode;

    /**
     * Lunghezza identificatore CAN.
     *
     * Valori:
     *
     * 11
     * 29
     */
    private final int canIdBits;

    /**
     * Bitrate CAN in kbit/s.
     *
     * Valore 0 = non specificato.
     */
    private final int canBitrateKbps;

    /**
     * Costruttore completo.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing mode.
     * @param canIdBits lunghezza CAN ID.
     * @param canBitrateKbps bitrate CAN in kbit/s.
     */
    public DiagnosticTargetDefinition(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode,
            int canIdBits,
            int canBitrateKbps) {

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

        if (canBitrateKbps < 0) {

            throw new IllegalArgumentException(
                    "canBitrateKbps non può essere negativo."
            );
        }

        this.canIdBits =
                canIdBits;

        this.canBitrateKbps =
                canBitrateKbps;

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
     * Il bitrate non viene specificato.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing mode.
     * @param canIdBits lunghezza CAN ID.
     */
    public DiagnosticTargetDefinition(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode,
            int canIdBits) {

        this(
                protocol,
                requestId,
                responseId,
                addressingMode,
                canIdBits,
                0
        );
    }

    /**
     * Costruttore compatibile con la prima versione.
     *
     * Assume CAN 11 bit e bitrate non specificato.
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
                11,
                0
        );
    }

    @NonNull
    public String getProtocol() {

        return protocol;
    }

    @NonNull
    public String getRequestId() {

        return requestId;
    }

    @NonNull
    public String getResponseId() {

        return responseId;
    }

    @NonNull
    public String getAddressingMode() {

        return addressingMode;
    }

    public int getCanIdBits() {

        return canIdBits;
    }

    /**
     * Restituisce il bitrate CAN.
     *
     * @return bitrate in kbit/s, oppure 0 se non specificato.
     */
    public int getCanBitrateKbps() {

        return canBitrateKbps;
    }

    /**
     * Indica se il bitrate è stato esplicitamente specificato.
     *
     * @return true se presente.
     */
    public boolean hasCanBitrate() {

        return canBitrateKbps > 0;
    }

    public boolean isStandardCanId() {

        return canIdBits == 11;
    }

    public boolean isExtendedCanId() {

        return canIdBits == 29;
    }

    public boolean isCan() {

        return "CAN".equals(protocol)
                ||
                "ISO_15765_4_CAN".equals(protocol)
                ||
                "UDS".equals(protocol);
    }

    public boolean isPhysical() {

        return "PHYSICAL".equals(
                addressingMode
        );
    }

    public boolean isFunctional() {

        return "FUNCTIONAL".equals(
                addressingMode
        );
    }

    /**
     * Normalizza e valida un CAN ID.
     *
     * @param value valore.
     * @param name nome.
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

        if (numericValue > maxValue) {

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
                ", canBitrateKbps=" +
                canBitrateKbps +
                '}';
    }


    /**
     * Indica se il target usa la selezione automatica
     * del protocollo ELM327.
     *
     * @return true se AUTO.
     */
    public boolean isAutomaticProtocol() {

        return "AUTO".equalsIgnoreCase(
                protocol
        );
    }

    /**
     * Crea un target OBD funzionale con protocollo automatico.
     *
     * Il target non forza CAN e non imposta header CAN.
     *
     * @return target automatico.
     */
    @NonNull
    public static DiagnosticTargetDefinition
    createAutomaticObdTarget() {

        return new DiagnosticTargetDefinition(
                "AUTO",
                "000",
                "000",
                "FUNCTIONAL",
                11,
                0
        );
    }
}