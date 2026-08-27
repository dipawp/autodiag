package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentificationDefinitionTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentificationDefinitionTest {

    /**
     * Verifica la costruzione standard di un DID 0x22.
     */
    @Test
    public void standardDidDefinitionWorks() {

        EcuIdentificationDefinition definition =
                new EcuIdentificationDefinition(
                        "F190",
                        "vin",
                        "STRING",
                        true
                );

        assertEquals(
                "22",
                definition.getService()
        );

        assertEquals(
                "F190",
                definition.getDid()
        );

        assertEquals(
                "vin",
                definition.getField()
        );

        assertEquals(
                "STRING",
                definition.getDecoder()
        );

        assertTrue(
                definition.isRequired()
        );

        assertEquals(
                "22F190",
                definition.buildRequest()
        );

        assertTrue(
                definition.isReadDataByIdentifier()
        );
    }

    /**
     * Verifica il costruttore completo.
     */
    @Test
    public void completeDefinitionStoresAllValues() {

        EcuIdentificationDefinition definition =
                new EcuIdentificationDefinition(
                        "22",
                        "F191",
                        "ecuHardwareNumber",
                        "ASCII",
                        false,
                        2,
                        5
                );

        assertEquals(
                "22",
                definition.getService()
        );

        assertEquals(
                "F191",
                definition.getDid()
        );

        assertEquals(
                "ecuHardwareNumber",
                definition.getField()
        );

        assertEquals(
                "ASCII",
                definition.getDecoder()
        );

        assertFalse(
                definition.isRequired()
        );

        assertEquals(
                2,
                definition.getByteOffset()
        );

        assertEquals(
                5,
                definition.getByteLength()
        );

        assertTrue(
                definition.hasByteLength()
        );

        assertEquals(
                "22F191",
                definition.buildRequest()
        );
    }

    /**
     * Verifica che DID minuscoli e con spazi vengano normalizzati.
     */
    @Test
    public void didIsNormalized() {

        EcuIdentificationDefinition definition =
                new EcuIdentificationDefinition(
                        " f190 ",
                        "vin",
                        "ascii",
                        true
                );

        assertEquals(
                "F190",
                definition.getDid()
        );

        assertEquals(
                "ASCII",
                definition.getDecoder()
        );

        assertEquals(
                "22F190",
                definition.buildRequest()
        );
    }

    /**
     * Verifica DID non valido.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidDidIsRejected() {

        new EcuIdentificationDefinition(
                "F19",
                "vin",
                "STRING",
                true
        );
    }

    /**
     * Verifica DID contenente caratteri non HEX.
     */
    @Test(expected = IllegalArgumentException.class)
    public void nonHexDidIsRejected() {

        new EcuIdentificationDefinition(
                "FG90",
                "vin",
                "STRING",
                true
        );
    }

    /**
     * Verifica offset negativo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void negativeOffsetIsRejected() {

        new EcuIdentificationDefinition(
                "22",
                "F190",
                "vin",
                "STRING",
                true,
                -1,
                0
        );
    }

    /**
     * Verifica lunghezza negativa.
     */
    @Test(expected = IllegalArgumentException.class)
    public void negativeLengthIsRejected() {

        new EcuIdentificationDefinition(
                "22",
                "F190",
                "vin",
                "STRING",
                true,
                0,
                -1
        );
    }
}