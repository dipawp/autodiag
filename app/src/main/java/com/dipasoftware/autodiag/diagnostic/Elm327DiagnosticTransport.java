package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import com.dipasoftware.autodiag.connection.Connection;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327DiagnosticTransport
 *
 * Tipo.......: Adapter
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Adapter tra DiagnosticTransport e Connection.
 *
 * Gestisce:
 *
 * - stato della transazione;
 * - target diagnostico;
 * - configurazione dell'adapter;
 * - invio della request;
 * - ricezione della response.
 *
 * Il configuratore dell'ELM327 è separato dal transport.
 *
 * ATTENZIONE:
 *
 * Questa versione NON invia ancora comandi AT.
 *
 * Elm327AdapterConfigurator memorizza solamente il target configurato.
 *
 * La ReadOnlyDiagnosticPolicy rimane a monte del transport.
 *
 * ****************************************************************************
 */
public class Elm327DiagnosticTransport
        implements DiagnosticTransport {

    /**
     * Connection fisica.
     */
    @NonNull
    private final Connection connection;

    /**
     * Stato della transazione.
     */
    @NonNull
    private final DiagnosticTransportState state;

    /**
     * Configuratore adapter.
     */
    @NonNull
    private final DiagnosticAdapterConfigurator adapterConfigurator;

    /**
     * Target configurato dall'adapter.
     */
    private DiagnosticTargetDefinition configuredTarget;

    /**
     * Costruttore standard.
     *
     * Utilizza il configuratore ELM327 predefinito.
     *
     * @param connection connessione fisica.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection) {

        this(
                connection,
                new Elm327AdapterConfigurator()
        );
    }

    /**
     * Costruttore configurabile.
     *
     * Utile per test e per future implementazioni.
     *
     * @param connection connessione fisica.
     * @param adapterConfigurator configuratore adapter.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection,
            @NonNull DiagnosticAdapterConfigurator adapterConfigurator) {

        this.connection =
                connection;

        this.state =
                new DiagnosticTransportState();

        this.adapterConfigurator =
                adapterConfigurator;

        this.configuredTarget =
                null;
    }

    /**
     * Invia una request verso il target specificato.
     *
     * Se il target è differente da quello attualmente configurato,
     * viene prima richiesto il cambio di configurazione.
     *
     * In questa versione la configurazione è solamente logica:
     * non vengono inviati comandi AT.
     *
     * @param target target diagnostico.
     * @param request request diagnostica.
     *
     * @throws IOException errore di comunicazione/configurazione.
     */
    @Override
    public void send(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull String request)
            throws IOException {

        if (!connection.isConnected()) {

            throw new IOException(
                    "Connection non connessa."
            );
        }

        String normalizedRequest =
                request.trim();

        if (normalizedRequest.isEmpty()) {

            throw new IOException(
                    "Request diagnostica vuota."
            );
        }

        /*
         * ---------------------------------------------------------
         * CONFIGURAZIONE TARGET
         * ---------------------------------------------------------
         */

        if (!isSameTarget(
                configuredTarget,
                target
        )) {

            try {

                adapterConfigurator.configure(
                        target
                );

            } catch (
                    RuntimeException exception) {

                throw new IOException(
                        "Impossibile configurare "
                                + "il target diagnostico.",
                        exception
                );
            }

            configuredTarget =
                    target;
        }

        /*
         * ---------------------------------------------------------
         * NUOVA TRANSAZIONE
         * ---------------------------------------------------------
         */

        state.reset();

        state.begin(
                target,
                normalizedRequest
        );

        /*
         * ---------------------------------------------------------
         * INVIO
         * ---------------------------------------------------------
         *
         * Il transport non modifica ancora la request
         * con CAN ID o altri comandi adapter.
         */
        try {

            connection.send(
                    normalizedRequest + "\r"
            );

        } catch (
                RuntimeException exception) {

            state.reset();

            throw new IOException(
                    "Errore durante l'invio "
                            + "della request diagnostica.",
                    exception
            );
        }
    }

    /**
     * Riceve la risposta relativa al target corrente.
     *
     * @param target target atteso.
     *
     * @return risposta raw.
     *
     * @throws IOException stato non valido oppure errore.
     */
    @Override
    @NonNull
    public String receive(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        /*
         * Nessuna request pendente.
         */
        if (state.isIdle()) {

            throw new IOException(
                    "Nessuna request diagnostica pendente."
            );
        }

        /*
         * La transazione è già completata.
         */
        if (!state.isWaitingResponse()) {

            throw new IOException(
                    "La transazione diagnostica "
                            + "non è più in attesa di risposta."
            );
        }

        /*
         * Target differente dalla request pendente.
         */
        if (!state.matchesTarget(
                target
        )) {

            throw new IOException(
                    "Target diagnostico non corrispondente "
                            + "alla request pendente."
            );
        }

        /*
         * Il target della transazione deve essere anche
         * quello attualmente configurato.
         */
        if (!isSameTarget(
                configuredTarget,
                target
        )) {

            throw new IOException(
                    "Il target richiesto non corrisponde "
                            + "al target configurato."
            );
        }

        String response;

        try {

            response =
                    connection.receive();

        } catch (
                RuntimeException exception) {

            state.reset();

            throw new IOException(
                    "Errore durante la ricezione "
                            + "della risposta diagnostica.",
                    exception
            );
        }

        if (response == null) {

            response =
                    "";
        }

        state.markResponseReceived();

        return response;
    }

    /**
     * Reimposta il configuratore adapter.
     *
     * Non viene eseguito automaticamente dopo receive(),
     * perché il target può essere riutilizzato per la request
     * successiva.
     *
     * @throws IOException errore di reset.
     */
    public void resetAdapterConfiguration()
            throws IOException {

        adapterConfigurator.reset();

        configuredTarget =
                null;
    }

    /**
     * Restituisce lo stato della transazione.
     *
     * @return stato.
     */
    @NonNull
    public DiagnosticTransportState getState() {

        return state;
    }

    /**
     * Restituisce il configuratore adapter.
     *
     * @return configuratore.
     */
    @NonNull
    public DiagnosticAdapterConfigurator
    getAdapterConfigurator() {

        return adapterConfigurator;
    }

    /**
     * Restituisce il target attualmente configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Restituisce l'ultimo target della transazione.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition getLastTarget() {

        return state.getTarget();
    }

    /**
     * Restituisce l'ultima request.
     *
     * @return request.
     */
    @NonNull
    public String getLastRequest() {

        return state.getRequest();
    }

    /**
     * Indica se è presente una risposta pendente.
     *
     * @return true se in attesa.
     */
    public boolean isWaitingResponse() {

        return state.isWaitingResponse();
    }

    /**
     * Confronta due target.
     *
     * @param first primo target.
     * @param second secondo target.
     *
     * @return true se equivalenti.
     */
    private boolean isSameTarget(
            DiagnosticTargetDefinition first,
            DiagnosticTargetDefinition second) {

        if (first == null ||
                second == null) {

            return false;
        }

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
}