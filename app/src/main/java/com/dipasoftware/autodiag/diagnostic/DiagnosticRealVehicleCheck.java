package com.dipasoftware.autodiag.diagnostic;

import android.util.Log;

import androidx.annotation.NonNull;

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
 * La prima operazione utilizzata è esclusivamente:
 *
 *     09 02
 *
 * cioè la richiesta OBD-II del VIN.
 *
 * La richiesta passa attraverso DiagnosticPidExecutor e quindi
 * attraverso la ReadOnlyDiagnosticPolicy.
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
     * Target utilizzato per la richiesta OBD-II funzionale.
     *
     * 7DF = functional request.
     * 7E8 = tipica risposta powertrain.
     */
    private static final String REQUEST_ID =
            "7DF";

    private static final String RESPONSE_ID =
            "7E8";

    private static final int CAN_ID_BITS =
            11;

    private static final int CAN_BITRATE_KBPS =
            500;

    /**
     * Sender diagnostico.
     */
    @NonNull
    private final DiagnosticCommandSender commandSender;

    /**
     * Costruttore.
     *
     * @param commandSender sender diagnostico.
     */
    public DiagnosticRealVehicleCheck(
            @NonNull DiagnosticCommandSender commandSender) {

        this.commandSender =
                commandSender;
    }




    /**
     * Costruttore basato direttamente sul DiagnosticPidExecutor.
     *
     * @param executor executor diagnostico.
     */
    public DiagnosticRealVehicleCheck(
            @NonNull DiagnosticPidExecutor executor) {

        this(
                request -> {

                    DiagnosticPidExecutionBridge bridge =
                            new DiagnosticPidExecutionBridge(
                                    executor
                            );

                    return bridge.send(
                            request
                    );
                }
        );
    }

    /**
     * Esegue la richiesta VIN reale.
     *
     * @return risultato.
     *
     * @throws java.io.IOException errore comunicazione.
     */
    @NonNull
    public Result readVin()
            throws java.io.IOException {

        Log.d(
                TAG,
                "=================================================="
        );

        Log.d(
                TAG,
                "INIZIO VEHICLE CHECK"
        );

        Log.d(
                TAG,
                "REQUEST: "
                        + VIN_REQUEST
        );

        String response =
                commandSender.send(
                        VIN_REQUEST
                );

        if (response == null) {

            Log.e(
                    TAG,
                    "Risposta nulla alla richiesta VIN."
            );

            throw new java.io.IOException(
                    "Nessuna risposta alla richiesta VIN."
            );
        }

        Log.d(
                TAG,
                "RESPONSE RAW:"
        );

        Log.d(
                TAG,
                response
        );

        if (response.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "Risposta VIN vuota."
            );

            throw new java.io.IOException(
                    "Risposta VIN vuota."
            );
        }

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

            Log.e(
                    TAG,
                    "Impossibile interpretare la risposta VIN.",
                    exception
            );

            throw new java.io.IOException(
                    "Risposta VIN non valida.",
                    exception
            );
        }

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
                "FINE VEHICLE CHECK"
        );

        Log.d(
                TAG,
                "=================================================="
        );

        return new Result(
                response,
                vin
        );
    }

    /**
     * Restituisce il target OBD funzionale utilizzato
     * dal vehicle check.
     *
     * @return target.
     */
    @NonNull
    public DiagnosticTargetDefinition getTarget() {

        return new DiagnosticTargetDefinition(
                "CAN",
                REQUEST_ID,
                RESPONSE_ID,
                "FUNCTIONAL",
                CAN_ID_BITS,
                CAN_BITRATE_KBPS
        );
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
         * Costruttore.
         *
         * @param rawResponse risposta raw.
         * @param vin VIN.
         */
        public Result(
                @NonNull String rawResponse,
                @NonNull String vin) {

            this.rawResponse =
                    rawResponse;

            this.vin =
                    vin;
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
         * Verifica che il VIN sia completo.
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
         * @throws java.io.IOException errore.
         */
        @NonNull
        String send(
                @NonNull String request)
                throws java.io.IOException;
    }


    /**
     * Bridge interno tra DiagnosticRealVehicleCheck
     * e DiagnosticPidExecutor.
     */
    private static class DiagnosticPidExecutionBridge {

        @NonNull
        private final DiagnosticPidExecutor executor;

        DiagnosticPidExecutionBridge(
                @NonNull DiagnosticPidExecutor executor) {

            this.executor =
                    executor;
        }

        @NonNull
        String send(
                @NonNull String request)
                throws java.io.IOException {

            DiagnosticRealVehicleCheck temporary =
                    null;

            DiagnosticTargetDefinition target =
                    new DiagnosticTargetDefinition(
                            "CAN",
                            REQUEST_ID,
                            RESPONSE_ID,
                            "FUNCTIONAL",
                            CAN_ID_BITS,
                            CAN_BITRATE_KBPS
                    );

            DiagnosticPidExecutor.DiagnosticPidExecution
                    execution =
                    executor.executeRaw(
                            target,
                            request
                    );

            return execution.getRawResponse();
        }
    }
}