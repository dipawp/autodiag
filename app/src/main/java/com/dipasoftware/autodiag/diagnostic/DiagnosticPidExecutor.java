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
 * Esegue una singola richiesta diagnostica.
 *
 * Responsabilità:
 *
 * - costruire la request;
 * - verificare la policy di sicurezza;
 * - inviare la request tramite DiagnosticTransport;
 * - ricevere la risposta raw;
 * - delegare il parsing al DiagnosticResponseParser.
 *
 * La Connection rimane il livello fisico di comunicazione.
 *
 * DiagnosticTransport rappresenta invece il livello di trasporto
 * diagnostico e permette in futuro di gestire il target ECU senza
 * modificare tutta l'architettura.
 *
 * ****************************************************************************
 */
public class DiagnosticPidExecutor {

    /**
     * Transport diagnostico.
     */
    @NonNull
    private final DiagnosticTransport transport;

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
     * Policy di sicurezza.
     *
     * Nella V1 deve essere ReadOnlyDiagnosticPolicy.
     */
    @NonNull
    private final DiagnosticOperationPolicy operationPolicy;

    /**
     * Costruttore compatibile con il codice esistente.
     *
     * Utilizza:
     *
     * - Elm327DiagnosticTransport;
     * - ReadOnlyDiagnosticPolicy.
     *
     * @param connection connessione fisica.
     */
    public DiagnosticPidExecutor(
            @NonNull Connection connection) {

        this(
                new Elm327DiagnosticTransport(
                        connection
                ),
                new ReadOnlyDiagnosticPolicy()
        );
    }

    /**
     * Costruttore compatibile con il codice esistente
     * che permette di specificare la policy.
     *
     * @param connection connessione fisica.
     * @param operationPolicy policy diagnostica.
     */
    public DiagnosticPidExecutor(
            @NonNull Connection connection,
            @NonNull DiagnosticOperationPolicy operationPolicy) {

        this(
                new Elm327DiagnosticTransport(
                        connection
                ),
                operationPolicy
        );
    }

    /**
     * Costruttore basato direttamente sul transport.
     *
     * Utile per test e per future implementazioni del
     * livello di comunicazione diagnostica.
     *
     * @param transport transport diagnostico.
     * @param operationPolicy policy diagnostica.
     */
    public DiagnosticPidExecutor(
            @NonNull DiagnosticTransport transport,
            @NonNull DiagnosticOperationPolicy operationPolicy) {

        this.transport =
                transport;

        this.requestBuilder =
                new DiagnosticRequestBuilder();

        this.responseParser =
                new DiagnosticResponseParser();

        this.operationPolicy =
                operationPolicy;
    }

    /**
     * Costruttore basato sul transport e policy read-only.
     *
     * @param transport transport diagnostico.
     */
    public DiagnosticPidExecutor(
            @NonNull DiagnosticTransport transport) {

        this(
                transport,
                new ReadOnlyDiagnosticPolicy()
        );
    }

    /**
     * Esegue una richiesta diagnostica utilizzando il target
     * di compatibilità predefinito.
     *
     * Questo metodo mantiene la compatibilità con tutto il codice
     * esistente che utilizza:
     *
     *     execute(definition)
     *
     * IMPORTANTE:
     *
     * PidDefinition non contiene il protocollo o il target ECU.
     * Per questo motivo questa API utilizza un target di compatibilità
     * fisso.
     *
     * Il percorso corretto per il nuovo catalogo ECU è:
     *
     *     execute(definition, target)
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

        DiagnosticTargetDefinition target =
                createDefaultTarget();

        return execute(
                definition,
                target
        );
    }

    /**
     * Esegue una richiesta diagnostica utilizzando esplicitamente
     * il target ECU.
     *
     * Questo è il nuovo percorso catalog-driven.
     *
     * Il target viene normalmente ottenuto da:
     *
     *     EcuDefinition.getTarget()
     *
     * La policy viene applicata prima dell'invio.
     *
     * @param definition definizione PID/DID.
     * @param target target diagnostico.
     *
     * @return risultato completo dell'esecuzione.
     *
     * @throws IOException errore di comunicazione.
     */
    @NonNull
    public DiagnosticPidExecution execute(
            @NonNull PidDefinition definition,
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        /*
         * ---------------------------------------------------------
         * COSTRUZIONE REQUEST
         * ---------------------------------------------------------
         */

        String request =
                requestBuilder.build(
                        definition
                );

        /*
         * ---------------------------------------------------------
         * POLICY DI SICUREZZA
         * ---------------------------------------------------------
         *
         * La policy viene valutata PRIMA del transport.
         *
         * Una richiesta non autorizzata non deve mai arrivare
         * alla Connection.
         */
        operationPolicy.validate(
                definition,
                request
        );

        /*
         * ---------------------------------------------------------
         * INVIO
         * ---------------------------------------------------------
         */

        transport.send(
                target,
                request
        );

        /*
         * ---------------------------------------------------------
         * RICEZIONE
         * ---------------------------------------------------------
         */

        String response =
                transport.receive(
                        target
                );

        if (response == null) {

            response =
                    "";
        }

        /*
         * ---------------------------------------------------------
         * PARSING
         * ---------------------------------------------------------
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

            } catch (
                    IllegalArgumentException exception) {

                /*
                 * La risposta raw viene comunque conservata.
                 *
                 * La classificazione definitiva viene lasciata
                 * al chiamante.
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
     * Crea un target di compatibilità per il percorso
     * legacy execute(PidDefinition).
     *
     * IMPORTANTE:
     *
     * Questo NON identifica un'ECU reale.
     *
     * Serve esclusivamente per non rompere i chiamanti
     * esistenti che non possiedono un EcuDefinition.
     *
     * Una ECU reale dovrà utilizzare:
     *
     *     execute(definition, ecuDefinition.getTarget())
     *
     * @return target compatibile.
     */
    @NonNull
    private DiagnosticTargetDefinition createDefaultTarget() {

        return new DiagnosticTargetDefinition(
                "CAN",
                "7E0",
                "7E8",
                "PHYSICAL",
                11
        );
    }

    /**
     * Restituisce il transport utilizzato.
     *
     * Utile soprattutto per test e per le future implementazioni
     * del livello di comunicazione.
     *
     * @return transport.
     */
    @NonNull
    public DiagnosticTransport getTransport() {

        return transport;
    }

    /**
     * Risultato della singola esecuzione diagnostica.
     *
     * Contiene:
     *
     * - request;
     * - risposta raw;
     * - risposta interpretata, quando disponibile.
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
         * @return risposta raw.
         */
        @NonNull
        public String getRawResponse() {

            return rawResponse;
        }

        /**
         * Restituisce il risultato diagnostico interpretato.
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