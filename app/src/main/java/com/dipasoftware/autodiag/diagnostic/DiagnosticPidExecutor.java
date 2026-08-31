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
 * Esegue singole richieste diagnostiche.
 *
 * Responsabilità:
 *
 * - costruire la request;
 * - verificare la policy di sicurezza;
 * - inviare la request tramite DiagnosticTransport;
 * - ricevere la risposta raw;
 * - delegare il parsing al DiagnosticResponseParser;
 * - supportare richieste raw read-only quando non esiste una
 *   PidDefinition completa.
 *
 * La Connection rimane il livello fisico di comunicazione.
 *
 * DiagnosticTransport rappresenta il livello di trasporto diagnostico.
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
     */
    @NonNull
    private final DiagnosticOperationPolicy operationPolicy;

    /**
     * Costruttore compatibile con il codice esistente.
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
     * Costruttore compatibile che permette di specificare
     * la policy.
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
     * Mantiene la compatibilità con il vecchio percorso:
     *
     * execute(definition)
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
     * La policy viene sempre verificata prima del transport.
     *
     * @param definition definizione PID/DID.
     * @param target target diagnostico.
     *
     * @return risultato completo.
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
         * POLICY
         * ---------------------------------------------------------
         *
         * La validazione deve avvenire PRIMA del transport.
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
                 * La risposta raw resta disponibile.
                 *
                 * Il chiamante decide come interpretare
                 * una risposta che il parser generico non riconosce.
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
     * Esegue una richiesta diagnostica raw utilizzando
     * il target di compatibilità predefinito.
     *
     * Il metodo è destinato a richieste read-only per le quali
     * non esiste una PidDefinition completa.
     *
     * @param request request HEX.
     *
     * @return risultato completo.
     *
     * @throws IOException errore o richiesta non consentita.
     */
    @NonNull
    public DiagnosticPidExecution executeRaw(
            @NonNull String request)
            throws IOException {

        return executeRaw(
                createDefaultTarget(),
                request
        );
    }

    /**
     * Esegue una richiesta diagnostica raw utilizzando
     * un target specifico.
     *
     * Questa API è necessaria, ad esempio, per:
     *
     * 0902
     *
     * quando vogliamo conservare la risposta raw e delegare
     * successivamente il parsing al chiamante.
     *
     * La richiesta viene comunque sottoposta alla stessa
     * whitelist read-only utilizzata dall'executor normale.
     *
     * @param target target diagnostico.
     * @param request request HEX.
     *
     * @return risultato completo.
     *
     * @throws IOException errore o richiesta non consentita.
     */
    @NonNull
    public DiagnosticPidExecution executeRaw(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull String request)
            throws IOException {

        /*
         * ---------------------------------------------------------
         * NORMALIZZAZIONE
         * ---------------------------------------------------------
         */

        String normalizedRequest =
                normalizeRawRequest(
                        request
                );

        if (normalizedRequest.isEmpty()) {

            throw new IOException(
                    "Richiesta diagnostica raw vuota."
            );
        }

        if (normalizedRequest.length() < 2) {

            throw new IOException(
                    "Richiesta diagnostica raw non valida: "
                            + request
            );
        }

        /*
         * Una request HEX deve avere un numero pari
         * di caratteri.
         */
        if ((normalizedRequest.length() & 1) != 0) {

            throw new IOException(
                    "Richiesta diagnostica raw con "
                            + "numero dispari di caratteri HEX: "
                            + normalizedRequest
            );
        }

        /*
         * ---------------------------------------------------------
         * POLICY
         * ---------------------------------------------------------
         *
         * La policy esistente richiede una PidDefinition.
         *
         * Costruiamo quindi una definizione minimale che rappresenta
         * esclusivamente il contesto della request.
         *
         * La request stessa viene passata invariata alla policy.
         *
         * Non utilizziamo questa definizione per costruire la request.
         */
        PidDefinition policyDefinition =
                createRawPolicyDefinition(
                        normalizedRequest
                );

        try {

            operationPolicy.validate(
                    policyDefinition,
                    normalizedRequest
            );

        } catch (
                IllegalArgumentException exception) {

            throw new IOException(
                    "Richiesta diagnostica non consentita "
                            + "dalla read-only policy: "
                            + normalizedRequest,
                    exception
            );
        }

        /*
         * ---------------------------------------------------------
         * INVIO
         * ---------------------------------------------------------
         */

        transport.send(
                target,
                normalizedRequest
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
         * NESSUN PARSING AUTOMATICO
         * ---------------------------------------------------------
         *
         * Una request raw può appartenere a un formato
         * specifico non rappresentato dalla PidDefinition.
         *
         * Per questo restituiamo la response raw senza
         * passare dal parser generico.
         */

        return new DiagnosticPidExecution(
                normalizedRequest,
                response,
                null
        );
    }

    /**
     * Normalizza una richiesta HEX raw.
     *
     * @param request request.
     *
     * @return request normalizzata.
     */
    @NonNull
    private String normalizeRawRequest(
            @NonNull String request) {

        String normalized =
                request
                        .replace(
                                " ",
                                ""
                        )
                        .replace(
                                "\r",
                                ""
                        )
                        .replace(
                                "\n",
                                ""
                        )
                        .replace(
                                "\t",
                                ""
                        )
                        .toUpperCase();

        /*
         * Verifichiamo che ogni carattere sia HEX.
         */
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
                        "Carattere non HEX nella "
                                + "richiesta raw: "
                                + character
                );
            }
        }

        return normalized;
    }

    /**
     * Crea una PidDefinition minimale esclusivamente
     * per permettere alla DiagnosticOperationPolicy
     * esistente di valutare una request raw.
     *
     * La request reale NON viene costruita da questo oggetto:
     * viene passata direttamente al transport.
     *
     * @param request request normalizzata.
     *
     * @return definizione di contesto.
     */
    @NonNull
    private PidDefinition createRawPolicyDefinition(
            @NonNull String request) {

        /*
         * Usiamo il costruttore completo di PidDefinition
         * con valori neutri per i campi che non riguardano
         * la validazione del servizio.
         *
         * La policy esistente utilizza il contenuto della request
         * per determinare il servizio consentito.
         */
        return new PidDefinition(
                "RAW",
                "raw",
                "raw",
                "",
                "RAW",
                "",
                0,
                request.substring(
                        0,
                        2
                ),
                "RAW",
                "RAW",
                false,
                "BIG_ENDIAN",
                0,
                0,
                0,
                request,
                "",
                0
        );
    }

    /**
     * Crea il target di compatibilità per il percorso legacy.
     *
     * IMPORTANTE:
     *
     * Questo target NON identifica un'ECU reale.
     *
     * Serve esclusivamente ai chiamanti che non possiedono
     * un EcuDefinition.
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
     * @return transport.
     */
    @NonNull
    public DiagnosticTransport getTransport() {

        return transport;
    }

    /**
     * Risultato della singola esecuzione diagnostica.
     */
    public static class DiagnosticPidExecution {

        /**
         * Request inviata.
         */
        @NonNull
        private final String request;

        /**
         * Risposta raw.
         */
        @NonNull
        private final String rawResponse;

        /**
         * Risposta interpretata, quando disponibile.
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
         * Restituisce la risposta interpretata.
         *
         * @return risultato oppure null.
         */
        public DiagnosticResponseResult
        getParsedResponse() {

            return parsedResponse;
        }

        /**
         * Indica se esiste un risultato interpretato.
         *
         * @return true se disponibile.
         */
        public boolean hasParsedResponse() {

            return parsedResponse != null;
        }
    }
}
