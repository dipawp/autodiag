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
 * NON esegue:
 *
 * - clear DTC;
 * - reset;
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
     * Request VIN OBD-II.
     */
    private static final String VIN_REQUEST =
            "0902";

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
                "Richiesta diagnostica: "
                        + VIN_REQUEST
        );

        String response =
                commandSender.send(
                        VIN_REQUEST
                );

        if (response == null) {

            throw new java.io.IOException(
                    "Nessuna risposta alla richiesta VIN."
            );
        }

        Log.d(
                TAG,
                "Risposta raw VIN:"
        );

        Log.d(
                TAG,
                response
        );

        if (response.trim().isEmpty()) {

            throw new java.io.IOException(
                    "Risposta VIN vuota."
            );
        }

        String vin;

        try {

            vin =
                    new ObdVehicleInformationParser()
                            .parseVin(
                                    response,
                                    VIN_REQUEST
                            )
                            .getValue();

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
                "VIN estratto: "
                        + vin
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
         * @param rawResponse raw.
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
         * Verifica risultato.
         *
         * @return true se VIN valido.
         */
        public boolean isValid() {

            return vin.length() == 17;
        }
    }

    /**
     * Interfaccia minima per invio di una richiesta diagnostica.
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
}