package com.dipasoftware.autodiag.diagnostic;

import android.util.Log;

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
 * ****************************************************************************
 */
public class DiagnosticRealConnectionCheck {

    /**
     * Tag utilizzato per Logcat.
     */
    private static final String TAG =
            "DiagnosticRealConnectionCheck";

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

        Log.d(
                TAG,
                "=================================================="
        );

        Log.d(
                TAG,
                "INIZIO CHECK ELM327"
        );

        Log.d(
                TAG,
                "Invio comando adapter: ATI"
        );

        String identification =
                commandExecutor.execute(
                        "ATI"
                );

        Log.d(
                TAG,
                "Risposta ATI: "
                        + identification
        );

        if (identification.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "ATI ha restituito una risposta vuota."
            );

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATI."
            );
        }

        Log.d(
                TAG,
                "Invio comando adapter: ATDP"
        );

        String protocol =
                commandExecutor.execute(
                        "ATDP"
                );

        Log.d(
                TAG,
                "Risposta ATDP: "
                        + protocol
        );

        if (protocol.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "ATDP ha restituito una risposta vuota."
            );

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATDP."
            );
        }

        Result result =
                new Result(
                        identification,
                        protocol
                );

        Log.d(
                TAG,
                "ELM327 check completato."
        );

        Log.d(
                TAG,
                "ELM327 riconosciuto: "
                        + result.looksLikeElm327()
        );

        Log.d(
                TAG,
                "Protocollo CAN: "
                        + result.reportsCanProtocol()
        );

        Log.d(
                TAG,
                "Protocollo ISO 15765: "
                        + result.reportsIso15765()
        );

        Log.d(
                TAG,
                "CAN READY: "
                        + result.isCanReady()
        );

        Log.d(
                TAG,
                "FINE CHECK ELM327"
        );

        Log.d(
                TAG,
                "=================================================="
        );

        return result;
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
         * Verifica che entrambe le risposte siano presenti.
         *
         * @return true se valide.
         */
        public boolean isValid() {

            return !identificationResponse.isEmpty()
                    &&
                    !protocolResponse.isEmpty();
        }

        /**
         * Verifica se ATI contiene ELM327.
         *
         * Non certifica che l'adapter sia originale.
         *
         * @return true se sembra ELM327.
         */
        public boolean looksLikeElm327() {

            return identificationResponse
                    .toUpperCase()
                    .contains("ELM327");
        }

        /**
         * Verifica se ATDP indica CAN.
         *
         * @return true se CAN.
         */
        public boolean reportsCanProtocol() {

            return protocolResponse
                    .toUpperCase()
                    .contains("CAN");
        }

        /**
         * Verifica se ATDP indica ISO 15765.
         *
         * @return true se ISO 15765.
         */
        public boolean reportsIso15765() {

            return protocolResponse
                    .toUpperCase()
                    .contains("15765");
        }

        /**
         * Indica se l'adapter sembra pronto per il
         * successivo percorso CAN.
         *
         * Non esegue configurazioni.
         *
         * @return true se compatibile.
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