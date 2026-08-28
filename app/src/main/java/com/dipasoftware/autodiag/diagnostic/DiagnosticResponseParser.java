package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticResponseParser
 *
 * Tipo.......: Parser / Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Seleziona il parser corretto in base al servizio diagnostico
 * dichiarato da PidDefinition.
 *
 * Il parser coordina i parser specializzati senza duplicarne
 * la logica.
 *
 * Attualmente supporta:
 *
 * - Mode 01 OBD-II tramite ObdResponseParser;
 * - UDS ReadDataByIdentifier 0x22 tramite UdsResponseParser.
 *
 * Altri servizi verranno aggiunti solamente quando sarà
 * disponibile il relativo parser specializzato.
 *
 * ****************************************************************************
 */
public class DiagnosticResponseParser {

    /**
     * Parser specifico per OBD-II Mode 01.
     */
    @NonNull
    private final ObdResponseParser obdResponseParser;

    /**
     * Parser specifico per UDS 0x22.
     */
    @NonNull
    private final UdsResponseParser udsResponseParser;


    /**
     * Parser OBD-II Mode 09.
     */
    @NonNull
    private final ObdVehicleInformationParser
            obdVehicleInformationParser;

    /**
     * Costruttore.
     */
    public DiagnosticResponseParser() {

        this.obdResponseParser =
                new ObdResponseParser();

        this.udsResponseParser =
                new UdsResponseParser();

        this.obdVehicleInformationParser =
                new ObdVehicleInformationParser();
    }

    /**
     * Analizza una risposta diagnostica.
     *
     * La scelta del parser viene effettuata utilizzando
     * il service dichiarato nella definizione del parametro.
     *
     * @param definition definizione diagnostica.
     * @param response risposta ELM327.
     * @param request richiesta effettivamente inviata.
     *
     * @return risultato del parser specifico.
     *
     * @throws IllegalArgumentException risposta non valida
     *         oppure servizio non ancora supportato.
     */
    @NonNull
    public DiagnosticResponseResult parse(
            @NonNull PidDefinition definition,
            @NonNull String response,
            @NonNull String request) {

        String mode =
                definition.getMode()
                        .trim()
                        .toUpperCase();

        DiagnosticService service;

        try {

            service =
                    DiagnosticService.fromCode(
                            mode
                    );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Servizio diagnostico non supportato: "
                            + mode,
                    exception
            );
        }

        switch (service) {

            case SHOW_CURRENT_DATA:

                return parseObd(
                        response,
                        request
                );

            case VEHICLE_INFORMATION:

                return parseVehicleInformation(
                        response,
                        request
                );

            case READ_DATA_BY_IDENTIFIER:

                return parseUds(
                        response,
                        request
                );

            default:

                throw new IllegalArgumentException(
                        "Parser non ancora disponibile "
                                + "per il servizio "
                                + service.getCode()
                );
        }
    }

    /**
     * Utilizza ObdResponseParser per Mode 01.
     *
     * @param response risposta.
     * @param request richiesta.
     *
     * @return risultato normalizzato.
     */
    @NonNull
    private DiagnosticResponseResult parseObd(
            @NonNull String response,
            @NonNull String request) {

        ObdResponseParser.ObdResponse obdResponse =
                obdResponseParser.parse(
                        response,
                        request
                );

        return DiagnosticResponseResult.fromObd(
                obdResponse
        );
    }

    /**
     * Utilizza UdsResponseParser per servizio 0x22.
     *
     * @param response risposta.
     * @param request richiesta.
     *
     * @return risultato normalizzato.
     */
    @NonNull
    private DiagnosticResponseResult parseUds(
            @NonNull String response,
            @NonNull String request) {

        UdsResponseParser.UdsResponse udsResponse =
                udsResponseParser.parse(
                        response,
                        request
                );

        return DiagnosticResponseResult.fromUds(
                udsResponse
        );
    }


    /**
     * Analizza una risposta OBD-II Mode 09.
     *
     * Attualmente supporta esclusivamente 09 02 VIN.
     *
     * @param response risposta.
     * @param request richiesta.
     *
     * @return risultato normalizzato.
     */
    @NonNull
    private DiagnosticResponseResult parseVehicleInformation(
            @NonNull String response,
            @NonNull String request) {

        String normalizedRequest =
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
                                ">",
                                ""
                        )
                        .trim()
                        .toUpperCase();

        if (!"0902".equals(
                normalizedRequest
        )) {

            throw new IllegalArgumentException(
                    "Mode 09 non ancora supportato "
                            + "per request "
                            + request
            );
        }

        ObdVehicleInformationParser
                .VehicleInformationResponse parsed =
                obdVehicleInformationParser
                        .parseVin(
                                response,
                                request
                        );

        byte[] data =
                parsed.getVin()
                        .getBytes(
                                java.nio.charset.StandardCharsets
                                        .US_ASCII
                        );

        return new DiagnosticResponseResult(
                "OBD",
                parsed.getService(),
                parsed.getPid(),
                data,
                true,
                -1
        );
    }
}