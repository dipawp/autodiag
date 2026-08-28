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
 * Adapter tra DiagnosticTransport e l'attuale Connection utilizzata
 * dall'ELM327.
 *
 * Questa versione mantiene volutamente separato il concetto di target
 * dalla configurazione concreta dell'ELM327.
 *
 * Il target viene validato e memorizzato dal transport, mentre l'attuale
 * Connection continua a occuparsi del canale fisico.
 *
 * L'applicazione non usa ancora il target per inviare comandi AT specifici:
 * questa parte verrà introdotta insieme al transport/addressing reale.
 *
 * In questo modo non alteriamo il comportamento ELM327 già testato.
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
     * Ultimo target utilizzato.
     */
    private DiagnosticTargetDefinition lastTarget;

    /**
     * Ultima request inviata.
     */
    @NonNull
    private String lastRequest =
            "";

    /**
     * Costruttore.
     *
     * @param connection connessione fisica.
     */
    public Elm327DiagnosticTransport(
            @NonNull Connection connection) {

        this.connection =
                connection;
    }

    /**
     * Invia la richiesta attraverso Connection.
     *
     * Il target viene conservato per il livello transport, ma non
     * tradotto ancora in comandi AT: l'attuale Connection non espone
     * un'API per CAN ID.
     *
     * @param target target.
     * @param request request diagnostica.
     *
     * @throws IOException errore comunicazione.
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
         * Conserviamo il target usato per questa transazione.
         */
        lastTarget =
                target;

        lastRequest =
                normalizedRequest;

        /*
         * Manteniamo esattamente il comportamento attuale
         * della Connection.
         */
        connection.send(
                normalizedRequest + "\r"
        );
    }

    /**
     * Riceve la risposta dalla Connection.
     *
     * @param target target.
     *
     * @return risposta raw.
     *
     * @throws IOException errore comunicazione.
     */
    @Override
    @NonNull
    public String receive(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        /*
         * Verifichiamo che il receive corrisponda alla transazione
         * iniziata con send().
         */
        if (lastTarget == null) {

            throw new IOException(
                    "Nessuna richiesta diagnostica pendente."
            );
        }

        String response =
                connection.receive();

        if (response == null) {

            return "";
        }

        return response;
    }

    /**
     * Restituisce l'ultimo target utilizzato.
     *
     * Getter utile per test e diagnostica.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition getLastTarget() {

        return lastTarget;
    }

    /**
     * Restituisce l'ultima request.
     *
     * Getter utile per test.
     *
     * @return request.
     */
    @NonNull
    public String getLastRequest() {

        return lastRequest;
    }
}