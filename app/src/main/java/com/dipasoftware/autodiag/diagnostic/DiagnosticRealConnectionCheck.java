package com.dipasoftware.autodiag.diagnostic;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

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
 * ATI   -> identificazione dichiarata dell'adapter
 * ATDP  -> descrizione del protocollo corrente
 * ATDPN -> numero del protocollo corrente
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
     * Richiesta bitmap PID 01-20.
     */
    private static final String SUPPORTED_PIDS_REQUEST = "0100";







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
     * Il metodo:
     *
     * 1. esegue ATI;
     * 2. esegue ATDP;
     * 3. esegue ATDPN;
     * 4. rimuove l'echo eventualmente restituito dall'adapter;
     * 5. costruisce il risultato normalizzato.
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

        /*
         * ---------------------------------------------------------
         * ATI
         * ---------------------------------------------------------
         */

        Log.d(
                TAG,
                "Invio comando adapter: ATI"
        );

        String identificationRaw =
                commandExecutor.execute(
                        "ATI"
                );

        String identification =
                removeEcho(
                        "ATI",
                        identificationRaw
                );

        Log.d(
                TAG,
                "Risposta ATI: "
                        + identification
        );

        if (identification.isEmpty()) {

            Log.e(
                    TAG,
                    "ATI ha restituito una risposta vuota."
            );

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATI."
            );
        }

        /*
         * ---------------------------------------------------------
         * ATDP
         * ---------------------------------------------------------
         */

        Log.d(
                TAG,
                "Invio comando adapter: ATDP"
        );

        String protocolRaw =
                commandExecutor.execute(
                        "ATDP"
                );

        String protocol =
                removeEcho(
                        "ATDP",
                        protocolRaw
                );

        Log.d(
                TAG,
                "Risposta ATDP: "
                        + protocol
        );

        if (protocol.isEmpty()) {

            Log.e(
                    TAG,
                    "ATDP ha restituito una risposta vuota."
            );

            throw new java.io.IOException(
                    "L'ELM327 non ha restituito "
                            + "una risposta a ATDP."
            );
        }

        /*
         * ---------------------------------------------------------
         * ATDPN
         * ---------------------------------------------------------
         *
         * ATDPN è informativo.
         *
         * Alcuni adapter/firmware possono non supportarlo oppure
         * i test legacy possono non fornire una risposta.
         *
         * Quindi la sua assenza NON invalida ATI + ATDP.
         */

        Log.d(
                TAG,
                "Invio comando adapter: ATDPN"
        );

        String protocolNumberRaw =
                commandExecutor.execute(
                        "ATDPN"
                );

        String protocolNumber =
                removeEcho(
                        "ATDPN",
                        protocolNumberRaw
                );

        Log.d(
                TAG,
                "Risposta ATDPN: "
                        + protocolNumber
        );

        if (protocolNumber.isEmpty()) {

            Log.w(
                    TAG,
                    "ATDPN non disponibile; continuo usando ATI + ATDP."
            );
        }


        /*
         * ---------------------------------------------------------
         * RISULTATO
         * ---------------------------------------------------------
         */

        Result result =
                new Result(
                        identification,
                        protocol,
                        protocolNumber
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
                "Protocollo KWP: "
                        + result.reportsKwp()
        );

        Log.d(
                TAG,
                "Protocollo KWP FAST: "
                        + result.reportsKwpFast()
        );

        Log.d(
                TAG,
                "Protocollo numero: "
                        + result.getProtocolNumber()
        );

        Log.d(
                TAG,
                "Protocollo 5: "
                        + result.reportsProtocol5()
        );

        Log.d(
                TAG,
                "CAN READY: "
                        + result.isCanReady()
        );

        Log.d(
                TAG,
                "KWP FAST READY: "
                        + result.isKwpFastReady()
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
     * Rimuove l'echo del comando restituito dall'ELM327.
     *
     * Esempi:
     *
     * ATI + ELM327 v1.4>
     *      -> ELM327 v1.4
     *
     * ATDP + AUTO, ISO 14230-4 (KWP FAST)>
     *      -> AUTO, ISO 14230-4 (KWP FAST)
     *
     * ATDPN + A5>
     *      -> A5
     *
     * La risposta viene inoltre privata del prompt finale '>'.
     *
     * @param command comando inviato.
     * @param response risposta raw.
     *
     * @return risposta normalizzata.
     */
    @NonNull
    private String removeEcho(
            @NonNull String command,
            String response) {

        if (response == null) {

            return "";
        }

        String normalized =
                response
                        .replace(
                                '\r',
                                '\n'
                        )
                        .replace(
                                '\u0000',
                                ' '
                        )
                        .trim();

        /*
         * Elimina eventuali righe vuote iniziali/finali.
         */
        while (
                normalized.startsWith("\n")
        ) {

            normalized =
                    normalized.substring(
                            1
                    ).trim();
        }

        while (
                normalized.endsWith("\n")
        ) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    ).trim();
        }

        /*
         * Rimuove il prompt ELM327 finale.
         */
        if (normalized.endsWith(">")) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    ).trim();
        }

        String normalizedCommand =
                command
                        .replace(
                                " ",
                                ""
                        )
                        .trim()
                        .toUpperCase();

        String upperResponse =
                normalized.toUpperCase();

        /*
         * Echo attaccato alla risposta:
         *
         * ATIELM327 v1.4
         * ATDPAUTO,...
         * ATDPNA5
         */
        if (upperResponse.startsWith(
                normalizedCommand
        )) {

            normalized =
                    normalized.substring(
                            normalizedCommand.length()
                    ).trim();
        }

        return normalized;
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
         * Risposta ATDPN.
         */
        @NonNull
        private final String protocolNumberResponse;

        /**
         * Costruttore completo.
         *
         * @param identificationResponse risposta ATI.
         * @param protocolResponse risposta ATDP.
         * @param protocolNumberResponse risposta ATDPN.
         */
        public Result(
                @NonNull String identificationResponse,
                @NonNull String protocolResponse,
                @NonNull String protocolNumberResponse) {

            this.identificationResponse =
                    identificationResponse.trim();

            this.protocolResponse =
                    protocolResponse.trim();

            this.protocolNumberResponse =
                    protocolNumberResponse.trim();
        }

        /**
         * Costruttore compatibile con il vecchio codice.
         *
         * @param identificationResponse risposta ATI.
         * @param protocolResponse risposta ATDP.
         */
        public Result(
                @NonNull String identificationResponse,
                @NonNull String protocolResponse) {

            this(
                    identificationResponse,
                    protocolResponse,
                    ""
            );
        }

        /**
         * Restituisce la risposta ATI normalizzata.
         *
         * @return risposta.
         */
        @NonNull
        public String getIdentificationResponse() {

            return identificationResponse;
        }

        /**
         * Restituisce la risposta ATDP normalizzata.
         *
         * @return risposta.
         */
        @NonNull
        public String getProtocolResponse() {

            return protocolResponse;
        }

        /**
         * Restituisce la risposta ATDPN normalizzata.
         *
         * @return risposta.
         */
        @NonNull
        public String getProtocolNumber() {

            return protocolNumberResponse;
        }

        /**
         * Verifica la presenza dei dati base.
         *
         * Non richiede ATDPN per mantenere compatibilità
         * con Result creati tramite il vecchio costruttore.
         *
         * @return true se valido.
         */
        public boolean isValid() {

            return !identificationResponse.isEmpty()
                    &&
                    !protocolResponse.isEmpty();
        }

        /**
         * Verifica se ATI contiene ELM327.
         *
         * Non certifica l'originalità dell'adapter.
         *
         * @return true se sembra ELM327.
         */
        public boolean looksLikeElm327() {

            return identificationResponse
                    .toUpperCase()
                    .contains(
                            "ELM327"
                    );
        }

        /**
         * Verifica se ATDP indica CAN.
         *
         * @return true se CAN.
         */
        public boolean reportsCanProtocol() {

            return protocolResponse
                    .toUpperCase()
                    .contains(
                            "CAN"
                    );
        }

        /**
         * Verifica se ATDP indica ISO 15765.
         *
         * @return true se ISO 15765.
         */
        public boolean reportsIso15765() {

            return protocolResponse
                    .toUpperCase()
                    .contains(
                            "15765"
                    );
        }

        /**
         * Verifica se ATDP indica ISO 14230/KWP.
         *
         * @return true se ISO 14230.
         */
        public boolean reportsKwp() {

            return protocolResponse
                    .toUpperCase()
                    .contains(
                            "14230"
                    );
        }

        /**
         * Verifica se ATDP indica KWP FAST.
         *
         * @return true se KWP FAST.
         */
        public boolean reportsKwpFast() {

            return protocolResponse
                    .toUpperCase()
                    .contains(
                            "KWP FAST"
                    );
        }

        /**
         * Verifica se ATDPN indica protocollo 5.
         *
         * Sono accettati:
         *
         * 5
         * 05
         * A5
         * A05
         *
         * @return true se protocollo 5.
         */
        public boolean reportsProtocol5() {

            String normalized =
                    protocolNumberResponse
                            .toUpperCase()
                            .trim();

            return "5".equals(normalized)
                    ||
                    "05".equals(normalized)
                    ||
                    "A5".equals(normalized)
                    ||
                    "A05".equals(normalized);
        }

        /**
         * Indica se il percorso KWP FAST è compatibile.
         *
         * Se ATDPN non è disponibile, il controllo numerico
         * viene omesso per compatibilità con il vecchio
         * costruttore Result.
         *
         * @return true se compatibile.
         */
        public boolean isKwpFastReady() {

            return isValid()
                    &&
                    looksLikeElm327()
                    &&
                    reportsKwp()
                    &&
                    reportsKwpFast()
                    &&
                    (
                            protocolNumberResponse.isEmpty()
                                    ||
                                    reportsProtocol5()
                    );
        }

        /**
         * Indica se il percorso CAN ISO 15765 è compatibile.
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


    /**
     * Esegue una richiesta OBD-II standard 0100.
     *
     * 0100 consente di verificare che:
     *
     * ELM327 -> CAN -> ECU -> risposta
     *
     * sia operativo prima di tentare la lettura VIN.
     *
     * @return risposta raw.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    public String readSupportedPids()
            throws IOException {

        Log.d(
                TAG,
                "--------------------------------------------------"
        );

        Log.d(
                TAG,
                "INIZIO OBD PID CHECK"
        );

        Log.d(
                TAG,
                "REQUEST: "
                        + SUPPORTED_PIDS_REQUEST
        );

        String response =
                commandSender.sendCommand(
                        SUPPORTED_PIDS_REQUEST
                );

        if (response == null) {

            throw new IOException(
                    "Nessuna risposta alla richiesta 0100."
            );
        }

        Log.d(
                TAG,
                "RESPONSE RAW 0100:"
        );

        Log.d(
                TAG,
                response
        );

        if (response.trim().isEmpty()) {

            throw new IOException(
                    "Risposta 0100 vuota."
            );
        }

        return response;
    }
}
