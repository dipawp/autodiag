package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRealConnectionCheck
 *
 * Tipo.......: Service
 *
 * Descrizione:
 *
 * Esegue un controllo non distruttivo dell'adapter ELM327.
 *
 * Comandi utilizzati:
 *
 * ATI  -> identificazione dichiarata dell'adapter
 * ATDP -> descrizione del protocollo corrente
 *
 * NON invia richieste diagnostiche alla ECU.
 *
 * NON modifica il protocollo.
 *
 * NON modifica gli header.
 *
 * NON modifica i filtri CAN.
 *
 * NON esegue operazioni di scrittura sulla ECU.
 *
 * ****************************************************************************
 */
public class DiagnosticRealConnectionCheck {

    /**
     * Sender verso l'ELM327.
     */
    @NonNull
    private final Elm327CommandExecutor.CommandSender commandSender;

    /**
     * Executor dei comandi AT.
     */
    @NonNull
    private final Elm327CommandExecutor commandExecutor;

    /**
     * Costruttore.
     *
     * @param commandSender sender ELM327.
     */
    public DiagnosticRealConnectionCheck(
            @NonNull Elm327CommandExecutor.CommandSender commandSender) {

        this.commandSender =
                commandSender;

        this.commandExecutor =
                new Elm327CommandExecutor(
                        commandSender
                );
    }

    /**
     * Esegue il controllo dell'adapter.
     *
     * @return risultato.
     *
     * @throws java.io.IOException errore comunicazione.
     */
    @NonNull
    public Result check()
            throws java.io.IOException {

        String identification =
                commandExecutor.execute(
                        "ATI"
                );

        if (identification.trim().isEmpty()) {

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATI."
            );
        }

        String protocol =
                commandExecutor.execute(
                        "ATDP"
                );

        if (protocol.trim().isEmpty()) {

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATDP."
            );
        }

        return new Result(
                identification,
                protocol
        );
    }

    /**
     * Restituisce il sender.
     *
     * Package-private per test.
     *
     * @return sender.
     */
    @NonNull
    Elm327CommandExecutor.CommandSender
    getCommandSender() {

        return commandSender;
    }

    /**
     * Risultato del controllo.
     */
    public static class Result {

        /**
         * Risposta ATI.
         */
        @NonNull
        private final String identificationResponse;

        /**
         * Risposta ATDP.
         */
        @NonNull
        private final String protocolResponse;

        /**
         * Costruttore.
         *
         * @param identificationResponse risposta ATI.
         * @param protocolResponse risposta ATDP.
         */
        public Result(
                @NonNull String identificationResponse,
                @NonNull String protocolResponse) {

            this.identificationResponse =
                    identificationResponse.trim();

            this.protocolResponse =
                    protocolResponse.trim();
        }

        /**
         * Risposta ATI.
         *
         * @return risposta.
         */
        @NonNull
        public String getIdentificationResponse() {

            return identificationResponse;
        }

        /**
         * Risposta ATDP.
         *
         * @return risposta.
         */
        @NonNull
        public String getProtocolResponse() {

            return protocolResponse;
        }

        /**
         * Verifica che il risultato contenga entrambe
         * le risposte.
         *
         * @return true se valido.
         */
        public boolean isValid() {

            return !identificationResponse.isEmpty()
                    &&
                    !protocolResponse.isEmpty();
        }

        /**
         * Verifica se la risposta ATI sembra quella
         * tipica di un ELM327.
         *
         * Non è una verifica di autenticità del chip.
         *
         * @return true se il testo contiene ELM327.
         */
        public boolean looksLikeElm327() {

            return identificationResponse
                    .toUpperCase()
                    .contains("ELM327");
        }

        /**
         * Verifica se il protocollo descritto è CAN.
         *
         * Il controllo è volutamente testuale perché
         * ATDP restituisce una descrizione.
         *
         * @return true se contiene CAN.
         */
        public boolean reportsCanProtocol() {

            return protocolResponse
                    .toUpperCase()
                    .contains("CAN");
        }

        /**
         * Verifica se il protocollo descritto è ISO 15765.
         *
         * @return true se presente.
         */
        public boolean reportsIso15765() {

            String normalized =
                    protocolResponse
                            .toUpperCase();

            return normalized.contains(
                    "15765"
            );
        }

        /**
         * Indica se l'adapter sembra pronto per il nostro
         * successivo percorso CAN.
         *
         * Non effettua alcuna configurazione.
         *
         * @return true se identificazione e protocollo
         *         sono compatibili con CAN ISO 15765.
         */
        public boolean isCanReady() {

            return isValid()
                    &&
                    looksLikeElm327()
                    &&
                    reportsCanProtocol()
                    &&
                    reportsIso15765();
        }
    }
}