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
 * Descrizione:
 *
 * Transport diagnostico ELM327.
 *
 * Gestisce:
 *
 * - stato della transazione;
 * - target corrente;
 * - configurazione dell'adapter;
 * - invio request diagnostica;
 * - ricezione response.
 *
 * Il transport NON decide se una richiesta diagnostica è consentita.
 * La DiagnosticOperationPolicy viene applicata dal DiagnosticPidExecutor.
 *
 * La configurazione dell'ELM327 è separata dal traffico diagnostico.
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
     * Esecutore della configurazione.
     *
     * Può essere null nel percorso legacy nel quale
     * la configurazione dinamica non è ancora collegata.
     */
    private final Elm327ConfigurationExecutor
            configurationExecutor;

    /**
     * Target attualmente configurato.
     */
    private DiagnosticTargetDefinition configuredTarget;

    /**
     * Costruttore legacy.
     *
     * Mantiene il comportamento precedente:
     *
     * Connection
     *   ↓
     * request diagnostica
     *
     * senza esecuzione automatica dei comandi AT dinamici.
     *
     * @param connection connessione fisica.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection) {

        this(
                connection,
                new Elm327AdapterConfigurator(),
                null
        );
    }

    /**
     * Costruttore con configuratore.
     *
     * Il configuratore produce il piano, ma non lo esegue
     * perché manca un executor esplicito.
     *
     * @param connection connessione.
     * @param adapterConfigurator configuratore.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection,
            @NonNull DiagnosticAdapterConfigurator adapterConfigurator) {

        this(
                connection,
                adapterConfigurator,
                null
        );
    }

    /**
     * Costruttore completo.
     *
     * Questo è il nuovo percorso utilizzabile quando si vuole
     * applicare realmente la configurazione del target prima
     * della richiesta diagnostica.
     *
     * @param connection connessione fisica.
     * @param adapterConfigurator configuratore.
     * @param configurationExecutor esecutore configurazione.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection,
            @NonNull DiagnosticAdapterConfigurator adapterConfigurator,
            Elm327ConfigurationExecutor configurationExecutor) {

        this.connection =
                connection;

        this.state =
                new DiagnosticTransportState();

        this.adapterConfigurator =
                adapterConfigurator;

        this.configurationExecutor =
                configurationExecutor;

        this.configuredTarget =
                null;
    }

    /**
     * Invia una richiesta diagnostica.
     *
     * Se il target cambia e un ConfigurationExecutor è disponibile,
     * viene applicata la configurazione adapter prima dell'invio.
     *
     * Se il target cambia ma manca il ConfigurationExecutor,
     * il transport opera in modalità legacy e non invia comandi AT.
     *
     * @param target target diagnostico.
     * @param request richiesta.
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

            configureTarget(
                    target
            );
        }

        /*
         * ---------------------------------------------------------
         * TRANSAZIONE
         * ---------------------------------------------------------
         */

        state.reset();

        state.begin(
                target,
                normalizedRequest
        );

        /*
         * ---------------------------------------------------------
         * REQUEST DIAGNOSTICA
         * ---------------------------------------------------------
         *
         * Arriviamo qui solo dopo la configurazione del target.
         *
         * La policy read-only è stata già verificata
         * dal DiagnosticPidExecutor prima di questo punto.
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
                            + "della richiesta diagnostica.",
                    exception
            );
        }
    }

    /**
     * Configura il target adapter.
     *
     * @param target target.
     *
     * @throws IOException errore configurazione.
     */
    private void configureTarget(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        /*
         * Costruiamo sempre il piano attraverso il configuratore.
         *
         * Questo permette di verificare che il target sia supportato
         * anche quando l'esecuzione reale non è ancora abilitata.
         */
        adapterConfigurator.configure(
                target
        );

        /*
         * In modalità legacy non eseguiamo AT.
         *
         * La configurazione reale richiede esplicitamente
         * un Elm327ConfigurationExecutor.
         */
        if (configurationExecutor == null) {

            configuredTarget =
                    target;

            return;
        }

        if (!(adapterConfigurator
                instanceof Elm327AdapterConfigurator)) {

            throw new IOException(
                    "ConfigurationExecutor disponibile, "
                            + "ma il configuratore non è "
                            + "Elm327AdapterConfigurator."
            );
        }

        Elm327AdapterConfigurator configurator =
                (Elm327AdapterConfigurator)
                        adapterConfigurator;

        /*
         * Il piano deve esistere.
         */
        if (configurator.getLastPlan() == null) {

            throw new IOException(
                    "Piano ELM327 non disponibile."
            );
        }

        /*
         * Esecuzione completa.
         *
         * Se un singolo comando fallisce,
         * configurationExecutor lancia IOException.
         *
         * In quel caso configuredTarget NON viene aggiornato.
         */
        configurationExecutor.execute(
                configurator.getLastPlan()
        );

        configuredTarget =
                target;
    }

    /**
     * Riceve la risposta.
     *
     * Il target deve corrispondere alla transazione pendente
     * e al target configurato.
     *
     * @param target target.
     *
     * @return risposta raw.
     *
     * @throws IOException errore/stato non valido.
     */
    @Override
    @NonNull
    public String receive(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        /*
         * Nessuna richiesta.
         */
        if (state.isIdle()) {

            throw new IOException(
                    "Nessuna richiesta diagnostica pendente."
            );
        }

        /*
         * Richiesta già completata.
         */
        if (!state.isWaitingResponse()) {

            throw new IOException(
                    "La transazione diagnostica "
                            + "non è più in attesa di risposta."
            );
        }

        /*
         * Target della transazione.
         */
        if (!state.matchesTarget(
                target
        )) {

            throw new IOException(
                    "Target diagnostico non corrispondente "
                            + "alla richiesta pendente."
            );
        }

        /*
         * Target adapter configurato.
         */
        if (!isSameTarget(
                configuredTarget,
                target
        )) {

            throw new IOException(
                    "Target configurato non corrisponde "
                            + "al target della transazione."
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
     * Reimposta la configurazione.
     *
     * Nota:
     *
     * reset() del configuratore non invia automaticamente
     * comandi all'ELM327.
     *
     * @throws IOException errore.
     */
    public void resetAdapterConfiguration()
            throws IOException {

        adapterConfigurator.reset();

        configuredTarget =
                null;

        state.reset();
    }

    /**
     * Restituisce lo stato.
     *
     * @return stato.
     */
    @NonNull
    public DiagnosticTransportState getState() {

        return state;
    }

    /**
     * Restituisce il configuratore.
     *
     * @return configuratore.
     */
    @NonNull
    public DiagnosticAdapterConfigurator
    getAdapterConfigurator() {

        return adapterConfigurator;
    }

    /**
     * Restituisce l'esecutore configurazione.
     *
     * @return executor oppure null.
     */
    public Elm327ConfigurationExecutor
    getConfigurationExecutor() {

        return configurationExecutor;
    }

    /**
     * Restituisce il target configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition
    getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Restituisce ultimo target della transazione.
     *
     * @return target.
     */
    public DiagnosticTargetDefinition getLastTarget() {

        return state.getTarget();
    }

    /**
     * Restituisce ultima request.
     *
     * @return request.
     */
    @NonNull
    public String getLastRequest() {

        return state.getRequest();
    }

    /**
     * Indica se è in attesa di response.
     *
     * @return true se pending.
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
                        second.getCanIdBits()
                &&
                first.getCanBitrateKbps()
                        ==
                        second.getCanBitrateKbps();
    }
}