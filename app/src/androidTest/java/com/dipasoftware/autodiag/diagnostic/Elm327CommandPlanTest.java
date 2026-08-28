package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327CommandPlanTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327CommandPlanTest {

    /**
     * Verifica piano CAN 11 bit.
     */
    @Test
    public void standardCanPlanIsCreated()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        Elm327CommandPlan plan =
                configurator.buildCommandPlan(
                        target
                );

        assertEquals(
                5,
                plan.size()
        );

        assertEquals(
                "ATSP6",
                plan.getCommand(0)
        );

        assertEquals(
                "ATSH 7E0",
                plan.getCommand(1)
        );

        assertEquals(
                "ATCRA 7E8",
                plan.getCommand(2)
        );

        assertEquals(
                "ATE0",
                plan.getCommand(3)
        );

        assertEquals(
                "ATH0",
                plan.getCommand(4)
        );

        assertTrue(
                plan.hasCommands()
        );

        assertEquals(
                target,
                plan.getTarget()
        );
    }

    /**
     * Verifica target CAN extended.
     */
    @Test
    public void extendedCanPlanUsesExtendedIds()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "PHYSICAL",
                        29,
                        500
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        Elm327CommandPlan plan =
                configurator.buildCommandPlan(
                        target
                );

        assertEquals(
                "ATSH 18DAF110",
                plan.getCommand(1)
        );

        assertEquals(
                "ATCRA 18DA10F1",
                plan.getCommand(2)
        );
    }

    /**
     * Verifica che configure() salvi il piano.
     */
    @Test
    public void configureStoresCommandPlan()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

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
                5,
                configurator
                        .getPlannedCommands()
                        .size()
        );

        assertEquals(
                "ATSH 7E0",
                configurator
                        .getPlannedCommands()
                        .get(1)
        );
    }

    /**
     * Verifica reset.
     */
    @Test
    public void resetClearsPlan()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        configurator.configure(
                target
        );

        configurator.reset();

        assertTrue(
                !configurator.isConfigured()
        );

        assertEquals(
                0,
                configurator
                        .getPlannedCommands()
                        .size()
        );
    }


    @Test(expected = java.io.IOException.class)
    public void unsupportedCanBitrateIsRejected()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        250
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        configurator.buildCommandPlan(
                target
        );
    }
}