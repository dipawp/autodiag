package com.dipasoftware.autodiag.diagnostic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRequestBuilderTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica la costruzione delle richieste diagnostiche.
 *
 * ****************************************************************************
 */
public class DiagnosticRequestBuilderTest {

    /**
     * Verifica la costruzione di un PID standard
     * tramite mode + pid.
     */
    @Test
    public void buildStandardRequest() {

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
     * Verifica una request OEM esplicita.
     */
    @Test
    public void buildExplicitRequest() {

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

    /**
     * Verifica la normalizzazione di una request
     * contenente spazi e lettere minuscole.
     */
    @Test
    public void buildExplicitRequest_normalizesHex() {

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
                        "22 f1 90",
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


    /**
     * Verifica che un PID standard contenente già il mode
     * non venga duplicato nella richiesta.
     *
     * Esempio reale del catalogo:
     *
     * mode = 01
     * pid  = 010C
     *
     * risultato atteso:
     *
     * 010C
     */
    @Test
    public void buildFullStandardPidDoesNotDuplicateMode() {

        PidDefinition definition =
                new PidDefinition(
                        "010C",
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
}