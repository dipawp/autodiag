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
 * Classe.....: DiagnosticTargetDefinitionTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticTargetDefinitionTest {

    /**
     * Verifica un target CAN standard 11 bit.
     */
    @Test
    public void standardCanTargetWorks() {

        DiagnosticTargetDefinition definition =
                new DiagnosticTargetDefinition(
                        "can",
                        "0x7e0",
                        "07E8",
                        "physical"
                );

        assertEquals(
                "CAN",
                definition.getProtocol()
        );

        assertEquals(
                "7E0",
                definition.getRequestId()
        );

        assertEquals(
                "07E8",
                definition.getResponseId()
        );

        assertEquals(
                "PHYSICAL",
                definition.getAddressingMode()
        );

        assertEquals(
                11,
                definition.getCanIdBits()
        );

        assertTrue(
                definition.isStandardCanId()
        );

        assertFalse(
                definition.isExtendedCanId()
        );

        assertTrue(
                definition.isCan()
        );

        assertTrue(
                definition.isPhysical()
        );

        assertFalse(
                definition.isFunctional()
        );
    }

    /**
     * Verifica un target CAN extended 29 bit.
     */
    @Test
    public void extendedCanTargetWorks() {

        DiagnosticTargetDefinition definition =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "functional",
                        29
                );

        assertEquals(
                "UDS",
                definition.getProtocol()
        );

        assertEquals(
                "18DAF110",
                definition.getRequestId()
        );

        assertEquals(
                "18DA10F1",
                definition.getResponseId()
        );

        assertEquals(
                29,
                definition.getCanIdBits()
        );

        assertTrue(
                definition.isExtendedCanId()
        );

        assertFalse(
                definition.isStandardCanId()
        );

        assertTrue(
                definition.isCan()
        );

        assertTrue(
                definition.isFunctional()
        );

        assertFalse(
                definition.isPhysical()
        );
    }

    /**
     * Verifica il costruttore compatibile con la versione precedente.
     */
    @Test
    public void legacyConstructorUses11Bit() {

        DiagnosticTargetDefinition definition =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL"
                );

        assertEquals(
                11,
                definition.getCanIdBits()
        );
    }

    /**
     * Verifica rifiuto CAN ID 11 bit fuori range.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidStandardCanIdIsRejected() {

        new DiagnosticTargetDefinition(
                "CAN",
                "800",
                "7E8",
                "PHYSICAL",
                11
        );
    }

    /**
     * Verifica rifiuto CAN ID 29 bit fuori range.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidExtendedCanIdIsRejected() {

        new DiagnosticTargetDefinition(
                "CAN",
                "20000000",
                "18DA10F1",
                "PHYSICAL",
                29
        );
    }

    /**
     * Verifica rifiuto CAN ID non HEX.
     */
    @Test(expected = IllegalArgumentException.class)
    public void nonHexCanIdIsRejected() {

        new DiagnosticTargetDefinition(
                "CAN",
                "ZZZ",
                "7E8",
                "PHYSICAL",
                11
        );
    }

    /**
     * Verifica rifiuto lunghezza CAN non supportata.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidCanIdBitsIsRejected() {

        new DiagnosticTargetDefinition(
                "CAN",
                "7E0",
                "7E8",
                "PHYSICAL",
                16
        );
    }

    /**
     * Verifica protocollo vuoto.
     */
    @Test(expected = IllegalArgumentException.class)
    public void emptyProtocolIsRejected() {

        new DiagnosticTargetDefinition(
                "",
                "7E0",
                "7E8",
                "PHYSICAL",
                11
        );
    }

    /**
     * Verifica addressing vuoto.
     */
    @Test(expected = IllegalArgumentException.class)
    public void emptyAddressingModeIsRejected() {

        new DiagnosticTargetDefinition(
                "CAN",
                "7E0",
                "7E8",
                "",
                11
        );
    }


    @Test
    public void canBitrateIsStored() {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        assertEquals(
                500,
                target.getCanBitrateKbps()
        );

        assertTrue(
                target.hasCanBitrate()
        );
    }
}