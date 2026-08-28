package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticTransportState
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta lo stato dell'ultima transazione gestita dal
 * DiagnosticTransport.
 *
 * Contiene:
 *
 * - target corrente;
 * - request corrente;
 * - stato della transazione.
 *
 * La classe NON esegue comunicazioni.
 *
 * La classe NON modifica la Connection.
 *
 * Serve esclusivamente a mantenere coerente l'associazione:
 *
 *     target <-> request <-> response
 *
 * ****************************************************************************
 */
public class DiagnosticTransportState {

    /**
     * Stato della transazione.
     */
    public enum Status {

        /**
         * Nessuna richiesta attiva.
         */
        IDLE,

        /**
         * Request inviata e in attesa della risposta.
         */
        WAITING_RESPONSE,

        /**
         * Risposta ricevuta.
         */
        RESPONSE_RECEIVED
    }

    /**
     * Target dell'ultima transazione.
     */
    @Nullable
    private DiagnosticTargetDefinition target;

    /**
     * Request dell'ultima transazione.
     */
    @NonNull
    private String request;

    /**
     * Stato corrente.
     */
    @NonNull
    private Status status;

    /**
     * Costruttore.
     */
    public DiagnosticTransportState() {

        this.target =
                null;

        this.request =
                "";

        this.status =
                Status.IDLE;
    }

    /**
     * Apre una nuova transazione.
     *
     * @param target target diagnostico.
     * @param request request diagnostica.
     */
    public void begin(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull String request) {

        if (request.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Request diagnostica vuota."
            );
        }

        this.target =
                target;

        this.request =
                request.trim();

        this.status =
                Status.WAITING_RESPONSE;
    }

    /**
     * Verifica che la risposta appartenga al target
     * della transazione corrente.
     *
     * @param target target da verificare.
     *
     * @return true se coincide.
     */
    public boolean matchesTarget(
            @NonNull DiagnosticTargetDefinition target) {

        if (this.target == null) {

            return false;
        }

        return sameTarget(
                this.target,
                target
        );
    }

    /**
     * Segna la transazione come completata.
     */
    public void markResponseReceived() {

        if (status ==
                Status.WAITING_RESPONSE) {

            status =
                    Status.RESPONSE_RECEIVED;
        }
    }

    /**
     * Cancella la transazione corrente.
     */
    public void reset() {

        target =
                null;

        request =
                "";

        status =
                Status.IDLE;
    }

    /**
     * Restituisce il target corrente.
     *
     * @return target oppure null.
     */
    @Nullable
    public DiagnosticTargetDefinition getTarget() {

        return target;
    }

    /**
     * Restituisce la request corrente.
     *
     * @return request.
     */
    @NonNull
    public String getRequest() {

        return request;
    }

    /**
     * Restituisce lo stato.
     *
     * @return stato.
     */
    @NonNull
    public Status getStatus() {

        return status;
    }

    /**
     * Indica se esiste una transazione in attesa.
     *
     * @return true se in attesa.
     */
    public boolean isWaitingResponse() {

        return status ==
                Status.WAITING_RESPONSE;
    }

    /**
     * Indica se è stata ricevuta la risposta.
     *
     * @return true se ricevuta.
     */
    public boolean hasResponse() {

        return status ==
                Status.RESPONSE_RECEIVED;
    }

    /**
     * Indica se non esiste una transazione attiva.
     *
     * @return true se idle.
     */
    public boolean isIdle() {

        return status ==
                Status.IDLE;
    }

    /**
     * Confronta due target.
     *
     * Il confronto considera:
     *
     * - protocol;
     * - request ID;
     * - response ID;
     * - addressing mode;
     * - CAN ID width.
     *
     * @param first primo target.
     * @param second secondo target.
     *
     * @return true se equivalenti.
     */
    private boolean sameTarget(
            @NonNull DiagnosticTargetDefinition first,
            @NonNull DiagnosticTargetDefinition second) {

        return first.getProtocol()
                .equalsIgnoreCase(
                        second.getProtocol()
                )
                &&
                first.getRequestId()
                        .equalsIgnoreCase(
                                second.getRequestId()
                        )
                &&
                first.getResponseId()
                        .equalsIgnoreCase(
                                second.getResponseId()
                        )
                &&
                first.getAddressingMode()
                        .equalsIgnoreCase(
                                second.getAddressingMode()
                        )
                &&
                first.getCanIdBits()
                        ==
                        second.getCanIdBits();
    }

    /**
     * Rappresentazione testuale.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "DiagnosticTransportState{" +
                "target=" +
                target +
                ", request='" +
                request +
                '\'' +
                ", status=" +
                status +
                '}';
    }
}