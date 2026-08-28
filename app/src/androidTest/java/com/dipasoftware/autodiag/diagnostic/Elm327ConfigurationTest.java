package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ConfigurationTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ConfigurationTest {

    /**
     * Verifica creazione da DiagnosticTargetDefinition.
     */
    @Test
    public void configurationCanBeCreatedFromTarget() {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        Elm327Configuration configuration =
                new Elm327Configuration(
                        target
                );

        assertEquals(
                "CAN",
                configuration.getProtocol()
        );

        assertEquals(
                "7E0",
                configuration.getRequestId()
        );

        assertEquals(
                "7E8",
                configuration.getResponseId()
        );

        assertEquals(
                "PHYSICAL",
                configuration.getAddressingMode()
        );

        assertEquals(
                11,
                configuration.getCanIdBits()
        );

        assertTrue(
                configuration.isStandardCan()
        );

        assertFalse(
                configuration.isExtendedCan()
        );

        assertTrue(
                configuration.isPhysicalAddressing()
        );
    }

    /**
     * Verifica CAN extended 29 bit.
     */
    @Test
    public void extendedConfigurationWorks() {

        Elm327Configuration configuration =
                new Elm327Configuration(
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "PHYSICAL",
                        29
                );

        assertEquals(
                "UDS",
                configuration.getProtocol()
        );

        assertEquals(
                "18DAF110",
                configuration.getRequestId()
        );

        assertEquals(
                "18DA10F1",
                configuration.getResponseId()
        );

        assertEquals(
                29,
                configuration.getCanIdBits()
        );

        assertTrue(
                configuration.isExtendedCan()
        );

        assertFalse(
                configuration.isStandardCan()
        );
    }

    /**
     * Verifica conversione inversa a DiagnosticTargetDefinition.
     */
    @Test
    public void configurationCanBeConvertedBackToTarget() {

        DiagnosticTargetDefinition original =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        Elm327Configuration configuration =
                new Elm327Configuration(
                        original
                );

        DiagnosticTargetDefinition restored =
                configuration.toDiagnosticTarget();

        assertEquals(
                "CAN",
                restored.getProtocol()
        );

        assertEquals(
                "7E0",
                restored.getRequestId()
        );

        assertEquals(
                "7E8",
                restored.getResponseId()
        );

        assertEquals(
                "PHYSICAL",
                restored.getAddressingMode()
        );

        assertEquals(
                11,
                restored.getCanIdBits()
        );
    }

    /**
     * Verifica matching target.
     */
    @Test
    public void matchingTargetIsRecognized() {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        Elm327Configuration configuration =
                new Elm327Configuration(
                        target
                );

        assertTrue(
                configuration.matchesTarget(
                        target
                )
        );
    }

    /**
     * Verifica che un target differente non venga considerato equivalente.
     */
    @Test
    public void differentTargetIsNotRecognized() {

        DiagnosticTargetDefinition first =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        DiagnosticTargetDefinition second =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E1",
                        "7E9",
                        "PHYSICAL",
                        11
                );

        Elm327Configuration configuration =
                new Elm327Configuration(
                        first
                );

        assertFalse(
                configuration.matchesTarget(
                        second
                )
        );
    }

    /**
     * Verifica equivalenza di due configurazioni.
     */
    @Test
    public void equivalentConfigurationsMatch() {

        Elm327Configuration first =
                new Elm327Configuration(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        Elm327Configuration second =
                new Elm327Configuration(
                        "can",
                        "7e0",
                        "7e8",
                        "physical",
                        11
                );

        assertTrue(
                first.equalsConfiguration(
                        second
                )
        );
    }

    /**
     * Verifica che il modello utilizzi esattamente
     * i valori del DiagnosticTargetDefinition.
     */
    @Test
    public void targetValuesArePreserved() {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "FUNCTIONAL",
                        29
                );

        Elm327Configuration configuration =
                new Elm327Configuration(
                        target
                );

        DiagnosticTargetDefinition converted =
                configuration.toDiagnosticTarget();

        assertSame(
                target.getClass(),
                converted.getClass()
        );

        assertEquals(
                target.getProtocol(),
                converted.getProtocol()
        );

        assertEquals(
                target.getRequestId(),
                converted.getRequestId()
        );

        assertEquals(
                target.getResponseId(),
                converted.getResponseId()
        );

        assertEquals(
                target.getAddressingMode(),
                converted.getAddressingMode()
        );

        assertEquals(
                target.getCanIdBits(),
                converted.getCanIdBits()
        );
    }
}