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
 *
 * La classe NON comunica con la Connection.
 *
 * La classe rappresenta esclusivamente i parametri necessari
 * alla configurazione del transport adapter.
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
     * Modalità di addressing.
     */
    @NonNull
    private final String addressingMode;

    /**
     * Dimensione CAN ID.
     *
     * Valori supportati:
     *
     * 11
     * 29
     */
    private final int canIdBits;

    /**
     * Costruttore.
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
    }

    /**
     * Costruttore completo.
     *
     * @param protocol protocollo.
     * @param requestId request CAN ID.
     * @param responseId response CAN ID.
     * @param addressingMode addressing mode.
     * @param canIdBits dimensione CAN ID.
     */
    public Elm327Configuration(
            @NonNull String protocol,
            @NonNull String requestId,
            @NonNull String responseId,
            @NonNull String addressingMode,
            int canIdBits) {

        this(
                new DiagnosticTargetDefinition(
                        protocol,
                        requestId,
                        responseId,
                        addressingMode,
                        canIdBits
                )
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
     * Restituisce l'addressing mode.
     *
     * @return addressing.
     */
    @NonNull
    public String getAddressingMode() {

        return addressingMode;
    }

    /**
     * Restituisce la dimensione CAN ID.
     *
     * @return 11 oppure 29.
     */
    public int getCanIdBits() {

        return canIdBits;
    }

    /**
     * Indica se CAN standard.
     *
     * @return true se 11 bit.
     */
    public boolean isStandardCan() {

        return canIdBits == 11;
    }

    /**
     * Indica se CAN extended.
     *
     * @return true se 29 bit.
     */
    public boolean isExtendedCan() {

        return canIdBits == 29;
    }

    /**
     * Indica se l'addressing è fisico.
     *
     * @return true se physical.
     */
    public boolean isPhysicalAddressing() {

        return "PHYSICAL".equals(
                addressingMode
        );
    }

    /**
     * Indica se l'addressing è funzionale.
     *
     * @return true se functional.
     */
    public boolean isFunctionalAddressing() {

        return "FUNCTIONAL".equals(
                addressingMode
        );
    }

    /**
     * Converte la configurazione in un DiagnosticTargetDefinition.
     *
     * Il metodo permette di mantenere un singolo modello
     * di riferimento per il target diagnostico.
     *
     * @return target diagnostico.
     */
    @NonNull
    public DiagnosticTargetDefinition toDiagnosticTarget() {

        return new DiagnosticTargetDefinition(
                protocol,
                requestId,
                responseId,
                addressingMode,
                canIdBits
        );
    }

    /**
     * Verifica se la configurazione equivale a un target.
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
                        target.getCanIdBits();
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
                        other.canIdBits;
    }

    /**
     * Rappresentazione testuale.
     *
     * @return configurazione.
     */
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
                '}';
    }
}