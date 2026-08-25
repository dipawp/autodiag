package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: UdsResponseParserTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il parsing delle risposte UDS ReadDataByIdentifier (0x22).
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class UdsResponseParserTest {

    /**
     * Verifica una risposta positiva UDS.
     *
     * Request:
     *
     * 22 F1 90
     *
     * Response:
     *
     * 62 F1 90 12 34 56
     */
    @Test
    public void parsePositiveResponse()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        UdsResponseParser.UdsResponse result =
                parser.parse(
                        "62 F1 90 12 34 56",
                        "22 F1 90"
                );

        assertTrue(
                result.isPositive()
        );

        assertFalse(
                result.isNegative()
        );

        assertEquals(
                0x62,
                result.getService()
        );

        assertEquals(
                0xF190,
                result.getDid()
        );

        assertEquals(
                -1,
                result.getNegativeResponseCode()
        );

        assertArrayEquals(
                new byte[]{
                        0x12,
                        0x34,
                        0x56
                },
                result.getData()
        );

        assertEquals(
                "12 34 56",
                result.getDataHex()
        );
    }

    /**
     * Verifica che una risposta positiva possa essere
     * analizzata anche quando la risposta contiene
     * CR, LF e prompt ELM327.
     */
    @Test
    public void parsePositiveResponseWithElmFormatting()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        UdsResponseParser.UdsResponse result =
                parser.parse(
                        "62 F1 90 01 02 03\r\r>",
                        "22F190"
                );

        assertTrue(
                result.isPositive()
        );

        assertEquals(
                0xF190,
                result.getDid()
        );

        assertEquals(
                "01 02 03",
                result.getDataHex()
        );
    }

    /**
     * Verifica una risposta negativa UDS.
     *
     * 7F 22 31
     *
     * 31 = Request Out Of Range.
     */
    @Test
    public void parseNegativeResponse()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        UdsResponseParser.UdsResponse result =
                parser.parse(
                        "7F 22 31",
                        "22 F1 90"
                );

        assertFalse(
                result.isPositive()
        );

        assertTrue(
                result.isNegative()
        );

        assertEquals(
                0x7F,
                result.getService()
        );

        assertEquals(
                0xF190,
                result.getDid()
        );

        assertEquals(
                0x31,
                result.getNegativeResponseCode()
        );

        assertEquals(
                "",
                result.getDataHex()
        );
    }

    /**
     * Verifica che un DID differente da quello richiesto
     * venga rifiutato.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectUnexpectedDid()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        parser.parse(
                "62 F1 91 12 34",
                "22 F1 90"
        );
    }

    /**
     * Verifica che un service positivo errato
     * venga rifiutato.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectUnexpectedPositiveService()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        parser.parse(
                "61 F1 90 12 34",
                "22 F1 90"
        );
    }

    /**
     * Verifica una risposta negativa incompleta.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectIncompleteNegativeResponse()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        parser.parse(
                "7F 22",
                "22 F1 90"
        );
    }

    /**
     * Verifica una richiesta 0x22 incompleta.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectIncompleteRequest()
            throws Exception {

        UdsResponseParser parser =
                new UdsResponseParser();

        parser.parse(
                "62 F1 90 12",
                "22 F1"
        );
    }
}