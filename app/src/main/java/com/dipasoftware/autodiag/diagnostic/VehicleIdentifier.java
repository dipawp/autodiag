package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: VehicleIdentifier
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Esegue la discovery iniziale del veicolo.
 *
 * La prima informazione ricercata è il VIN tramite:
 *
 *     OBD-II Mode 09 PID 02
 *
 * Tutte le richieste passano tramite DiagnosticPidExecutor
 * e quindi tramite ReadOnlyDiagnosticPolicy.
 *
 * Se il VIN non è disponibile, viene restituita un'identificazione
 * vuota invece di interrompere il processo diagnostico generale.
 *
 * ****************************************************************************
 */
public class VehicleIdentifier {

    /**
     * Esecutore diagnostico.
     */
    @NonNull
    private final DiagnosticPidExecutor executor;

    /**
     * Costruttore.
     *
     * @param executor esecutore diagnostico.
     */
    public VehicleIdentifier(
            @NonNull DiagnosticPidExecutor executor) {

        this.executor =
                executor;
    }

    /**
     * Esegue la discovery iniziale del veicolo.
     *
     * @return identificazione veicolo.
     */
    @NonNull
    public VehicleIdentification identify() {

        PidDefinition definition =
                createVinDefinition();

        try {

            DiagnosticPidExecutor.DiagnosticPidExecution
                    execution =
                    executor.execute(
                            definition
                    );

            if (!execution.hasParsedResponse()) {

                return new VehicleIdentification(
                        ""
                );
            }

            DiagnosticResponseResult response =
                    execution.getParsedResponse();

            if (response == null ||
                    response.isNegative()) {

                return new VehicleIdentification(
                        ""
                );
            }

            String vin =
                    new String(
                            response.getData(),
                            java.nio.charset.StandardCharsets
                                    .US_ASCII
                    ).trim();

            return new VehicleIdentification(
                    vin
            );

        } catch (
                Exception exception) {

            /*
             * VIN non disponibile:
             * la discovery deve poter continuare.
             */
            return new VehicleIdentification(
                    ""
            );
        }
    }

    /**
     * Crea la definizione del VIN Mode 09.
     *
     * @return definizione.
     */
    @NonNull
    private PidDefinition createVinDefinition() {

        return new PidDefinition(
                "02",
                "vehicle_vin",
                "vehicle_vin_description",
                "",
                "RAW",
                "",
                17,
                "09",
                "STRING",
                "STANDARD",
                false,
                "BIG_ENDIAN",
                0,
                0,
                0,
                "0902",
                "49",
                0
        );
    }
}