package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import com.dipasoftware.autodiag.connection.Connection;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticPidExecutor
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Esegue una singola richiesta diagnostica utilizzando una Connection.
 *
 * Responsabilità:
 *
 * - costruire la request;
 * - inviare la request;
 * - ricevere la risposta raw;
 * - delegare il parsing al DiagnosticResponseParser.
 *
 * La classe NON interpreta gli errori specifici ELM327.
 *
 * Questo mantiene separata:
 *
 *     comunicazione
 *         ↓
 *     parsing diagnostico
 *         ↓
 *     gestione errori ELM327
 *
 * ****************************************************************************
 */
public class DiagnosticPidExecutor {

    /**
     * Connessione fisica verso l'interfaccia diagnostica.
     */
    @NonNull
    private final Connection connection;

    /**
     * Builder delle richieste diagnostiche.
     */
    @NonNull
    private final DiagnosticRequestBuilder requestBuilder;

    /**
     * Parser diagnostico generico.
     */
    @NonNull
    private final DiagnosticResponseParser responseParser;

    /**
     * Costruttore.
     *
     * @param connection connessione diagnostica.
     */
    public DiagnosticPidExecutor(
            @NonNull Connection connection) {

        this.connection =
                connection;

        this.requestBuilder =
                new DiagnosticRequestBuilder();

        this.responseParser =
                new DiagnosticResponseParser();
    }

    /**
     * Esegue una richiesta diagnostica.
     *
     * @param definition definizione PID/DID.
     *
     * @return risultato completo dell'esecuzione.
     *
     * @throws IOException errore di comunicazione.
     */
    @NonNull
    public DiagnosticPidExecution execute(
            @NonNull PidDefinition definition)
            throws IOException {

        if (!connection.isConnected()) {

            throw new IOException(
                    "Connection non connessa."
            );
        }

        /*
         * Costruzione della request.
         */
        String request =
                requestBuilder.build(
                        definition
                );

        /*
         * Invio della request all'ELM327.
         */
        connection.send(
                request + "\r"
        );

        /*
         * Lettura della risposta raw.
         */
        String response =
                connection.receive();

        if (response == null) {

            response = "";
        }

        /*
         * Il parser diagnostico viene eseguito solamente
         * quando esiste una risposta.
         *
         * Una risposta vuota rimane comunque disponibile
         * nel risultato, così il chiamante può decidere
         * come gestirla.
         */
        DiagnosticResponseResult parsedResponse =
                null;

        if (!response.trim().isEmpty()) {

            try {

                parsedResponse =
                        responseParser.parse(
                                definition,
                                response,
                                request
                        );

            } catch (IllegalArgumentException exception) {

                /*
                 * La risposta raw viene comunque conservata.
                 *
                 * La classificazione definitiva dell'errore
                 * rimane responsabilità del chiamante.
                 */
            }
        }

        return new DiagnosticPidExecution(
                request,
                response,
                parsedResponse
        );
    }

    /**
     * Risultato della singola esecuzione diagnostica.
     *
     * Contiene:
     *
     * - request effettivamente inviata;
     * - risposta raw;
     * - risposta già interpretata, quando possibile.
     */
    public static class DiagnosticPidExecution {

        /**
         * Request inviata.
         */
        @NonNull
        private final String request;

        /**
         * Risposta raw ricevuta.
         */
        @NonNull
        private final String rawResponse;

        /**
         * Risposta diagnostica interpretata.
         *
         * Può essere null se la risposta non è interpretabile.
         */
        private final DiagnosticResponseResult parsedResponse;

        /**
         * Costruttore.
         *
         * @param request request.
         * @param rawResponse risposta raw.
         * @param parsedResponse risposta interpretata.
         */
        DiagnosticPidExecution(
                @NonNull String request,
                @NonNull String rawResponse,
                DiagnosticResponseResult parsedResponse) {

            this.request =
                    request;

            this.rawResponse =
                    rawResponse;

            this.parsedResponse =
                    parsedResponse;
        }

        /**
         * Restituisce la request.
         *
         * @return request.
         */
        @NonNull
        public String getRequest() {

            return request;
        }

        /**
         * Restituisce la risposta raw.
         *
         * @return risposta.
         */
        @NonNull
        public String getRawResponse() {

            return rawResponse;
        }

        /**
         * Restituisce il risultato interpretato.
         *
         * @return risultato oppure null.
         */
        public DiagnosticResponseResult getParsedResponse() {

            return parsedResponse;
        }

        /**
         * Indica se la risposta è stata interpretata.
         *
         * @return true se disponibile.
         */
        public boolean hasParsedResponse() {

            return parsedResponse != null;
        }
    }
}