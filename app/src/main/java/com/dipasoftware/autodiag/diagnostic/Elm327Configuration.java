package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327Configuration
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta la configurazione richiesta all'adapter ELM327
 * per comunicare con uno specifico target diagnostico.
 *
 * La classe NON invia comandi.
 * La classe NON comunica con la Connection.
 *
 * ****************************************************************************
 */
public class Elm327Configuration {

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
     * Dimensione CAN ID.
     */
    private final int canIdBits;

    /**
     * Bitrate CAN in kbit/s.
     *
     * 0 = non specificato.
     */
    private final int canBitrateKbps;

    /**
     * Costruttore da target.
     *
     * @param target target diagnostico.
     */
    public Elm327Configuration(
            @NonNull DiagnosticTargetDefinition target) {

        this.protocol =
                target.getProtocol();

        this.requestId =
                target.getRequestId();

        this.responseId =
                target.getResponseId();

        this.addressingMode =
                target.getAddressingMode();

        this.canIdBits =
                target.getCanIdBits();

        this.canBitrateKbps =
                target.getCanBitrateKbps();
    }

    /**
     * Costruttore completo.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing.
     * @param canIdBits dimensione CAN ID.
     * @param canBitrateKbps bitrate.
     */
    public Elm327Configuration(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode,
            int canIdBits,
            int canBitrateKbps) {

        this(
                new DiagnosticTargetDefinition(
                        protocol,
                        requestId,
                        responseId,
                        addressingMode,
                        canIdBits,
                        canBitrateKbps
                )
        );
    }

    /**
     * Costruttore compatibile.
     */
    public Elm327Configuration(
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

    public int getCanBitrateKbps() {

        return canBitrateKbps;
    }

    public boolean hasCanBitrate() {

        return canBitrateKbps > 0;
    }

    public boolean isStandardCan() {

        return canIdBits == 11;
    }

    public boolean isExtendedCan() {

        return canIdBits == 29;
    }

    public boolean isPhysicalAddressing() {

        return "PHYSICAL".equals(
                addressingMode
        );
    }

    public boolean isFunctionalAddressing() {

        return "FUNCTIONAL".equals(
                addressingMode
        );
    }

    /**
     * Converte nuovamente in DiagnosticTargetDefinition.
     *
     * @return target.
     */
    @NonNull
    public DiagnosticTargetDefinition toDiagnosticTarget() {

        return new DiagnosticTargetDefinition(
                protocol,
                requestId,
                responseId,
                addressingMode,
                canIdBits,
                canBitrateKbps
        );
    }

    /**
     * Verifica corrispondenza con un target.
     *
     * @param target target.
     *
     * @return true se equivalente.
     */
    public boolean matchesTarget(
            @NonNull DiagnosticTargetDefinition target) {

        return protocol.equalsIgnoreCase(
                target.getProtocol()
        )
                &&
                requestId.equalsIgnoreCase(
                        target.getRequestId()
                )
                &&
                responseId.equalsIgnoreCase(
                        target.getResponseId()
                )
                &&
                addressingMode.equalsIgnoreCase(
                        target.getAddressingMode()
                )
                &&
                canIdBits ==
                        target.getCanIdBits()
                &&
                canBitrateKbps ==
                        target.getCanBitrateKbps();
    }

    /**
     * Verifica equivalenza con un'altra configurazione.
     *
     * @param other altra configurazione.
     *
     * @return true se equivalente.
     */
    public boolean equalsConfiguration(
            @NonNull Elm327Configuration other) {

        return protocol.equalsIgnoreCase(
                other.protocol
        )
                &&
                requestId.equalsIgnoreCase(
                        other.requestId
                )
                &&
                responseId.equalsIgnoreCase(
                        other.responseId
                )
                &&
                addressingMode.equalsIgnoreCase(
                        other.addressingMode
                )
                &&
                canIdBits ==
                        other.canIdBits
                &&
                canBitrateKbps ==
                        other.canBitrateKbps;
    }

    @NonNull
    @Override
    public String toString() {

        return "Elm327Configuration{" +
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
}