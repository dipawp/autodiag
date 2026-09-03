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
 * Operazione:
 *
 *     09 02
 *
 * La richiesta viene eseguita tramite DiagnosticPidExecutor e quindi
 * sottoposta alla ReadOnlyDiagnosticPolicy.
 *
 * Il risultato viene:
 *
 * - scritto nel Logcat;
 * - salvato tramite DiagnosticLogger quando disponibile.
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
     * Target OBD funzionale.
     *//*
    private static final String REQUEST_ID =
            "7DF";

    private static final String RESPONSE_ID =
            "7E8";

    private static final int CAN_ID_BITS =
            11;

    private static final int CAN_BITRATE_KBPS =
            500;*/


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
     * Richiesta bitmap PID 01-20.
     */
    private static final String SUPPORTED_PIDS_REQUEST = "0100";




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
     * Esegue la richiesta VIN reale.
     *
     * Il report viene scritto nel file logger anche in caso di errore.
     *
     * @return risultato.
     *
     * @throws IOException errore comunicazione o parsing.
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
         * 0100 è una richiesta OBD-II read-only.
         *
         * La utilizziamo prima di 0902 per verificare che:
         *
         * ELM327 -> protocollo -> ECU -> risposta
         *
         * sia operativo.
         */
        final String supportedPidsRequest =
                "0100";

        appendReportLine(
                report,
                "REQUEST: "
                        + supportedPidsRequest
        );

        Log.d(
                TAG,
                "REQUEST: "
                        + supportedPidsRequest
        );

        String supportedPidsResponse;

        try {

            supportedPidsResponse =
                    commandSender.send(
                            supportedPidsRequest
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
                    commandSender.send(
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

            throw new IOException(
                    "Risposta VIN vuota."
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
         * RISULTATO
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
     * Restituisce il target utilizzato per il primo
     * controllo OBD-II reale.
     *
     * Non forza un protocollo CAN.
     * L'ELM327 rimane in AUTO e utilizza il protocollo
     * che ha rilevato dalla vettura.
     *
     * @return target OBD automatico.
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
         * @return VIN.
         */
        @NonNull
        public String getVin() {

            return vin;
        }

        /**
         * File report.
         *
         * @return file oppure null se il logger non era disponibile
         *         o il salvataggio è fallito.
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
        String send(
                @NonNull String request)
                throws IOException;
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
                commandSender.send(
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