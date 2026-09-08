package com.dipasoftware.autodiag.diagnostic;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRealVehicleCheck
 *
 * Tipo.......: Service
 *
 * Descrizione:
 *
 * Esegue il primo controllo diagnostico reale del veicolo.
 *
 * La sequenza di prova è:
 *
 *     0100
 *     0902
 *
 * 0100 verifica che il percorso OBD-II verso la ECU sia operativo.
 *
 * 0902 richiede il VIN tramite OBD-II Mode 09.
 *
 * Il risultato viene:
 *
 * - scritto nel Logcat;
 * - salvato tramite DiagnosticLogger quando disponibile.
 *
 * Una risposta negativa 7F 09 12 viene riconosciuta come:
 *
 *     Service Not Supported / Sub-function Not Supported
 *
 * e non viene trattata come un errore del parser VIN.
 *
 * NON esegue:
 *
 * - clear DTC;
 * - reset ECU;
 * - coding;
 * - routine;
 * - security access;
 * - write data.
 *
 * ****************************************************************************
 */
public class DiagnosticRealVehicleCheck {

    /**
     * Tag Logcat.
     */
    private static final String TAG =
            "DiagnosticRealVehicleCheck";

    /**
     * Richiesta VIN OBD-II.
     */
    private static final String VIN_REQUEST =
            "0902";

    /**
     * Richiesta bitmap PID 01-20.
     */
    private static final String SUPPORTED_PIDS_REQUEST =
            "0100";

    /**
     * Risposta negativa UDS/diagnostica:
     *
     * 7F <service> <NRC>
     */
    private static final String VIN_NOT_SUPPORTED_RESPONSE =
            "7F 09 12";

    /**
     * Protocollo nominale del controllo real vehicle.
     *
     * Il protocollo viene comunque lasciato all'ELM327
     * nella modalità configurata dal percorso OBD.
     */
    private static final String PROTOCOL =
            "AUTO";

    /**
     * Sender diagnostico.
     */
    @NonNull
    private final DiagnosticCommandSender commandSender;

    /**
     * Logger diagnostico.
     *
     * Può essere null nel costruttore utilizzato dai test legacy.
     */
    private final DiagnosticLogger diagnosticLogger;

    /**
     * Costruttore compatibile con il percorso precedente.
     *
     * Non salva file.
     *
     * @param commandSender sender diagnostico.
     */
    public DiagnosticRealVehicleCheck(
            @NonNull DiagnosticCommandSender commandSender) {

        this(
                commandSender,
                null
        );
    }

    /**
     * Costruttore completo.
     *
     * @param commandSender sender diagnostico.
     * @param diagnosticLogger logger file.
     */
    public DiagnosticRealVehicleCheck(
            @NonNull DiagnosticCommandSender commandSender,
            DiagnosticLogger diagnosticLogger) {

        this.commandSender =
                commandSender;

        this.diagnosticLogger =
                diagnosticLogger;
    }

    /**
     * Esegue il controllo reale del veicolo.
     *
     * Sequenza:
     *
     * 1. 0100
     * 2. 0902
     * 3. parsing VIN
     *
     * La risposta 7F 09 12 viene riconosciuta e salvata
     * nel report come VIN non disponibile tramite Mode 09.
     *
     * @return risultato.
     *
     * @throws IOException errore comunicazione.
     */
    @NonNull
    public Result readVin()
            throws IOException {

        StringBuilder report =
                new StringBuilder();

        appendReportLine(
                report,
                "=================================================="
        );

        appendReportLine(
                report,
                "INIZIO VEHICLE CHECK"
        );

        Log.d(
                TAG,
                "=================================================="
        );

        Log.d(
                TAG,
                "INIZIO VEHICLE CHECK"
        );

        /*
         * ---------------------------------------------------------
         * CONTROLLO COMUNICAZIONE ECU
         * ---------------------------------------------------------
         *
         * Prima di richiedere il VIN verifichiamo che la ECU
         * risponda a una normale richiesta OBD-II.
         */
        appendReportLine(
                report,
                "REQUEST: "
                        + SUPPORTED_PIDS_REQUEST
        );

        Log.d(
                TAG,
                "REQUEST: "
                        + SUPPORTED_PIDS_REQUEST
        );

        String supportedPidsResponse;

        try {

            supportedPidsResponse =
                    commandSender.sendCommand(
                            SUPPORTED_PIDS_REQUEST
                    );

        } catch (
                IOException exception) {

            appendReportLine(
                    report,
                    "ERRORE INVIO 0100: "
                            + exception.getMessage()
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Errore durante l'invio di 0100.",
                    exception
            );

            throw exception;
        }

        if (supportedPidsResponse == null) {

            appendReportLine(
                    report,
                    "RESPONSE RAW 0100: <null>"
            );

            appendReportLine(
                    report,
                    "ERRORE: nessuna risposta alla richiesta 0100."
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Nessuna risposta alla richiesta 0100."
            );

            throw new IOException(
                    "Nessuna risposta alla richiesta 0100."
            );
        }

        appendReportLine(
                report,
                "RESPONSE RAW 0100:"
        );

        appendReportLine(
                report,
                supportedPidsResponse
        );

        Log.d(
                TAG,
                "RESPONSE RAW 0100:"
        );

        Log.d(
                TAG,
                supportedPidsResponse
        );

        if (supportedPidsResponse.trim().isEmpty()) {

            appendReportLine(
                    report,
                    "ERRORE: risposta 0100 vuota."
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Risposta 0100 vuota."
            );

            throw new IOException(
                    "Risposta 0100 vuota."
            );
        }

        /*
         * ---------------------------------------------------------
         * LETTURA VIN
         * ---------------------------------------------------------
         */

        appendReportLine(
                report,
                ""
        );

        appendReportLine(
                report,
                "REQUEST: "
                        + VIN_REQUEST
        );

        Log.d(
                TAG,
                "REQUEST: "
                        + VIN_REQUEST
        );

        String response;

        try {

            response =
                    commandSender.sendCommand(
                            VIN_REQUEST
                    );

        } catch (
                IOException exception) {

            appendReportLine(
                    report,
                    "ERRORE INVIO 0902: "
                            + exception.getMessage()
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Errore durante l'invio di 0902.",
                    exception
            );

            throw exception;
        }

        if (response == null) {

            appendReportLine(
                    report,
                    "RESPONSE RAW 0902: <null>"
            );

            appendReportLine(
                    report,
                    "ERRORE: nessuna risposta alla richiesta VIN."
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Nessuna risposta alla richiesta VIN."
            );

            throw new IOException(
                    "Nessuna risposta alla richiesta VIN."
            );
        }

        appendReportLine(
                report,
                "RESPONSE RAW 0902:"
        );

        appendReportLine(
                report,
                response
        );

        Log.d(
                TAG,
                "RESPONSE RAW 0902:"
        );

        Log.d(
                TAG,
                response
        );

        if (response.trim().isEmpty()) {

            appendReportLine(
                    report,
                    "ERRORE: risposta VIN vuota."
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Risposta VIN vuota."
            );

            throw new IOException(
                    "Risposta VIN vuota."
            );
        }

        /*
         * ---------------------------------------------------------
         * RISPOSTA NEGATIVA 7F 09 12
         * ---------------------------------------------------------
         *
         * Non è un problema del parser.
         *
         * È una risposta diagnostica valida che indica che
         * la richiesta 09 02 non è supportata dalla ECU.
         */
        if (isVinNotSupportedResponse(
                response
        )) {

            appendReportLine(
                    report,
                    "VIN NON DISPONIBILE VIA OBD-II MODE 09"
            );

            appendReportLine(
                    report,
                    "NEGATIVE RESPONSE: "
                            + VIN_NOT_SUPPORTED_RESPONSE
            );

            appendReportLine(
                    report,
                    "NRC: 12 - SUB-FUNCTION NOT SUPPORTED"
            );

            appendReportLine(
                    report,
                    "RESULT: VIN NOT AVAILABLE VIA MODE 09"
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            appendReportLine(
                    report,
                    "=================================================="
            );

            Log.w(
                    TAG,
                    "La ECU rifiuta 0902: "
                            + VIN_NOT_SUPPORTED_RESPONSE
            );

            File reportFile =
                    saveReportSafely(
                            report
                    );

            return new Result(
                    response,
                    "",
                    reportFile
            );
        }

        /*
         * ---------------------------------------------------------
         * PARSING VIN
         * ---------------------------------------------------------
         */

        String vin;

        try {

            ObdVehicleInformationParser.VehicleInformationResponse
                    vehicleResponse =
                    new ObdVehicleInformationParser()
                            .parseVin(
                                    response,
                                    VIN_REQUEST
                            );

            /*
             * VehicleInformationResponse espone getVin().
             */
            vin =
                    vehicleResponse.getVin();

        } catch (
                RuntimeException exception) {

            appendReportLine(
                    report,
                    "ERRORE PARSING VIN: "
                            + exception.getMessage()
            );

            appendReportLine(
                    report,
                    "FINE VEHICLE CHECK"
            );

            appendReportLine(
                    report,
                    "=================================================="
            );

            saveReportSafely(
                    report
            );

            Log.e(
                    TAG,
                    "Impossibile interpretare la risposta VIN.",
                    exception
            );

            throw new IOException(
                    "Risposta VIN non valida.",
                    exception
            );
        }

        /*
         * ---------------------------------------------------------
         * RISULTATO VALIDO
         * ---------------------------------------------------------
         */

        appendReportLine(
                report,
                ""
        );

        appendReportLine(
                report,
                "VIN: "
                        + vin
        );

        appendReportLine(
                report,
                "VIN LENGTH: "
                        + vin.length()
        );

        appendReportLine(
                report,
                "RESULT: VALID"
        );

        appendReportLine(
                report,
                "FINE VEHICLE CHECK"
        );

        appendReportLine(
                report,
                "=================================================="
        );

        Log.d(
                TAG,
                "VIN: "
                        + vin
        );

        Log.d(
                TAG,
                "VIN LENGTH: "
                        + vin.length()
        );

        Log.d(
                TAG,
                "RESULT: VALID"
        );

        Log.d(
                TAG,
                "FINE VEHICLE CHECK"
        );

        Log.d(
                TAG,
                "=================================================="
        );

        File reportFile =
                saveReportSafely(
                        report
                );

        return new Result(
                response,
                vin,
                reportFile
        );
    }

    /**
     * Determina se la risposta indica che il servizio 09
     * e/o la sottofunzione 02 non sono supportati.
     *
     * La risposta può contenere:
     *
     * 7F 09 12
     *
     * con CR/LF e prompt ELM327.
     *
     * @param response risposta raw.
     *
     * @return true se è 7F 09 12.
     */
    private boolean isVinNotSupportedResponse(
            @NonNull String response) {

        String normalized =
                response
                        .replace(
                                '\r',
                                ' '
                        )
                        .replace(
                                '\n',
                                ' '
                        )
                        .replace(
                                '>',
                                ' '
                        )
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .toUpperCase();

        return normalized.startsWith(
                VIN_NOT_SUPPORTED_RESPONSE
        );
    }

    /**
     * Salva il report tramite DiagnosticLogger.
     *
     * Gli errori del logger non devono nascondere l'esito
     * della richiesta diagnostica.
     *
     * @param report contenuto.
     *
     * @return file creato oppure null.
     */
    private File saveReportSafely(
            @NonNull StringBuilder report) {

        if (diagnosticLogger == null) {

            return null;
        }

        try {

            File file =
                    diagnosticLogger.saveLog(
                            report.toString()
                    );

            Log.d(
                    TAG,
                    "Report salvato in: "
                            + file.getAbsolutePath()
            );

            return file;

        } catch (
                IOException exception) {

            Log.e(
                    TAG,
                    "Impossibile salvare il report diagnostico.",
                    exception
            );

            return null;
        }
    }

    /**
     * Aggiunge una riga al report.
     *
     * @param report report.
     * @param line riga.
     */
    private void appendReportLine(
            @NonNull StringBuilder report,
            @NonNull String line) {

        report
                .append(line)
                .append('\n');
    }

    /**
     * Restituisce il target nominale del controllo.
     *
     * Il primo vehicle check non utilizza più questo target
     * per l'invio della richiesta OBD-II: la comunicazione viene
     * effettuata tramite il percorso OBD diretto.
     *
     * @return target automatico.
     */
    @NonNull
    public DiagnosticTargetDefinition getTarget() {

        return new DiagnosticTargetDefinition(
                PROTOCOL,
                "000",
                "000",
                "FUNCTIONAL",
                11,
                0
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
    DiagnosticCommandSender getCommandSender() {

        return commandSender;
    }

    /**
     * Risultato della richiesta VIN.
     */
    public static class Result {

        /**
         * Risposta raw.
         */
        @NonNull
        private final String rawResponse;

        /**
         * VIN.
         *
         * Può essere vuoto quando la ECU non supporta
         * 0902.
         */
        @NonNull
        private final String vin;

        /**
         * File report.
         */
        private final File reportFile;

        /**
         * Costruttore.
         *
         * @param rawResponse risposta raw.
         * @param vin VIN.
         * @param reportFile file report.
         */
        public Result(
                @NonNull String rawResponse,
                @NonNull String vin,
                File reportFile) {

            this.rawResponse =
                    rawResponse;

            this.vin =
                    vin;

            this.reportFile =
                    reportFile;
        }

        /**
         * Risposta raw.
         *
         * @return raw.
         */
        @NonNull
        public String getRawResponse() {

            return rawResponse;
        }

        /**
         * VIN.
         *
         * @return VIN oppure stringa vuota se non disponibile.
         */
        @NonNull
        public String getVin() {

            return vin;
        }

        /**
         * File report.
         *
         * @return file oppure null.
         */
        public File getReportFile() {

            return reportFile;
        }

        /**
         * Verifica VIN.
         *
         * @return true se lungo 17 caratteri.
         */
        public boolean isValid() {

            return vin.length() == 17;
        }
    }

    /**
     * Interfaccia per l'invio di una richiesta diagnostica.
     *
     * NOTA:
     *
     * Nel progetto locale utilizziamo sendCommand().
     */
    public interface DiagnosticCommandSender {

        /**
         * Invia una richiesta diagnostica.
         *
         * @param request request HEX.
         *
         * @return risposta raw.
         *
         * @throws IOException errore.
         */
        @NonNull
        String sendCommand(
                @NonNull String request)
                throws IOException;
    }
}
