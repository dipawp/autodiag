package com.dipasoftware.autodiag.diagnostic;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticResponseParserTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica che DiagnosticResponseParser selezioni il parser
 * corretto in base al servizio diagnostico.
 *
 * ****************************************************************************
 */
public class DiagnosticResponseParserTest {

    /**
     * Verifica la selezione del parser OBD-II Mode 01.
     */
    @Test
    public void parseMode01UsesObdParser()
            throws Exception {

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

        DiagnosticResponseParser parser =
                new DiagnosticResponseParser();

        DiagnosticResponseResult result =
                parser.parse(
                        definition,
                        "41 0C 1A F8",
                        "010C"
                );

        assertEquals(
                "OBD",
                result.getProtocolType()
        );

        assertEquals(
                0x41,
                result.getService()
        );

        assertEquals(
                0x0C,
                result.getIdentifier()
        );

        assertTrue(
                result.isPositive()
        );

        assertArrayEquals(
                new byte[]{
                        0x1A,
                        (byte) 0xF8
                },
                result.getData()
        );
    }

    /**
     * Verifica la selezione del parser UDS 0x22.
     */
    @Test
    public void parseMode22UsesUdsParser()
            throws Exception {

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

        DiagnosticResponseParser parser =
                new DiagnosticResponseParser();

        DiagnosticResponseResult result =
                parser.parse(
                        definition,
                        "62 F1 90 31 48 47 43 4D",
                        "22F190"
                );

        assertEquals(
                "UDS",
                result.getProtocolType()
        );

        assertEquals(
                0x62,
                result.getService()
        );

        assertEquals(
                0xF190,
                result.getIdentifier()
        );

        assertTrue(
                result.isPositive()
        );

        assertEquals(
                "31 48 47 43 4D",
                result.getDataHex()
        );
    }

    /**
     * Verifica che un servizio non ancora implementato
     * venga rifiutato chiaramente.
     */
    @Test(expected = IllegalArgumentException.class)
    public void unsupportedServiceIsRejected() {

        PidDefinition definition =
                new PidDefinition(
                        "19",
                        "pid_dtc",
                        "pid_dtc_description",
                        "",
                        "RAW",
                        "",
                        0,
                        "19",
                        "DTC",
                        "OEM"
                );

        DiagnosticResponseParser parser =
                new DiagnosticResponseParser();

        parser.parse(
                definition,
                "59 02",
                "1902"
        );
    }
}