package com.dipasoftware.autodiag.diagnostic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerRequestTest
 *
 * Tipo.......: Unit Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica la costruzione delle richieste diagnostiche utilizzate
 * dal percorso PID.
 *
 * Il test non utilizza una connessione ELM327 reale.
 *
 * ****************************************************************************
 */
public class Elm327ManagerRequestTest {

    /**
     * Verifica che un PID standard venga trasformato
     * nella richiesta OBD-II corretta.
     */
    @Test
    public void standardPidRequestIsBuiltCorrectly() {

        PidDefinition definition =
                new PidDefinition(
                        "0C",
                        "pid_engine_rpm",
                        "pid_engine_rpm_description",
                        "rpm",
                        "FORMULA",
                        "((A*256)+B)/4",
                        2,
                        "01",
                        "RPM"
                );

        DiagnosticRequestBuilder builder =
                new DiagnosticRequestBuilder();

        assertEquals(
                "010C",
                builder.build(
                        definition
                )
        );
    }

    /**
     * Verifica che un PID OEM con request esplicita
     * mantenga esattamente la richiesta definita dal dataset.
     */
    @Test
    public void explicitOemRequestIsPreserved() {

        PidDefinition definition =
                new PidDefinition(
                        "F190",
                        "pid_vin",
                        "pid_vin_description",
                        "",
                        "RAW",
                        "",
                        17,
                        "22",
                        "STRING",
                        "OEM",
                        false,
                        "BIG_ENDIAN",
                        0,
                        0,
                        0,
                        "22F190",
                        "62",
                        0
                );

        DiagnosticRequestBuilder builder =
                new DiagnosticRequestBuilder();

        assertEquals(
                "22F190",
                builder.build(
                        definition
                )
        );
    }
}