package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327AdapterConfiguratorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327AdapterConfiguratorTest {

    /**
     * Verifica configurazione target CAN standard.
     */
    @Test
    public void configureStoresTarget()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        configurator.configure(
                target
        );

        assertTrue(
                configurator.isConfigured()
        );

        assertEquals(
                target,
                configurator.getConfiguredTarget()
        );

        assertEquals(
                "7E0",
                configurator
                        .getConfiguredTarget()
                        .getRequestId()
        );

        assertEquals(
                "7E8",
                configurator
                        .getConfiguredTarget()
                        .getResponseId()
        );
    }

    /**
     * Verifica cambio target.
     */
    @Test
    public void configureCanChangeTarget()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

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
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "PHYSICAL",
                        29
                );

        configurator.configure(
                first
        );

        configurator.configure(
                second
        );

        assertTrue(
                configurator.isConfigured()
        );

        assertEquals(
                second,
                configurator.getConfiguredTarget()
        );
    }

    /**
     * Verifica reset.
     */
    @Test
    public void resetClearsConfiguration()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        configurator.configure(
                target
        );

        configurator.reset();

        assertFalse(
                configurator.isConfigured()
        );

        assertNull(
                configurator.getConfiguredTarget()
        );
    }

    /**
     * Verifica rifiuto di un protocollo non CAN.
     */
    @Test(expected = java.io.IOException.class)
    public void nonCanTargetIsRejected()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "KLINE",
                        "1",
                        "2",
                        "PHYSICAL",
                        11
                );

        configurator.configure(
                target
        );
    }
}