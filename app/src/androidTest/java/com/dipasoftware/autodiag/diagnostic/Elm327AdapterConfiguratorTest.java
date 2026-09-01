package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
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


    /**
     * Verifica che bitrate non specificato mantenga
     * la compatibilità con ATSP0.
     */
    @Test
    public void unspecifiedCanBitrateUsesAutomaticProtocol()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        Elm327CommandPlan plan =
                configurator.buildCommandPlan(
                        target
                );

        assertEquals(
                "ATSP0",
                plan.getCommand(0)
        );
    }


    /**
     * Verifica che tutti i comandi del piano vengano eseguiti
     * nell'ordine corretto.
     */
    @Test
    public void executePlanRunsAllCommands()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        FakeCommandSender sender =
                new FakeCommandSender();

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        configurator.configure(
                target
        );

        /*
         * configure() costruisce e registra il piano
         * e, secondo il contratto attuale della classe,
         * imposta configured=true.
         */
        assertTrue(
                configurator.isConfigured()
        );

        configurator.executePlan(
                executor
        );

        assertTrue(
                configurator.isConfigured()
        );

        assertEquals(
                5,
                sender.getCommandCount()
        );

        assertEquals(
                "ATTP6",
                sender.getCommands().get(0)
        );

        assertEquals(
                "ATSH 7E0",
                sender.getCommands().get(1)
        );

        assertEquals(
                "ATCRA 7E8",
                sender.getCommands().get(2)
        );

        assertEquals(
                "ATE0",
                sender.getCommands().get(3)
        );

        assertEquals(
                "ATH0",
                sender.getCommands().get(4)
        );
    }


    /**
     * Verifica che un errore interrompa immediatamente
     * l'esecuzione del piano.
     */
    @Test(expected = java.io.IOException.class)
    public void executePlanStopsOnCommandError()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        FakeCommandSender sender =
                new FakeCommandSender();

        sender.failOn(
                "ATSH 7E0"
        );

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        configurator.configure(
                target
        );

        configurator.executePlan(
                executor
        );
    }


    /**
     * Sender fittizio dei comandi ELM327.
     */
    private static class FakeCommandSender
            implements Elm327CommandExecutor.CommandSender {

        @NonNull
        private final java.util.List<String> commands =
                new java.util.ArrayList<>();

        private String failingCommand =
                "";

        @Override
        @NonNull
        public String sendCommand(
                @NonNull String command)
                throws java.io.IOException {

            commands.add(
                    command
            );

            if (command.equalsIgnoreCase(
                    failingCommand
            )) {

                return "?";
            }

            return "OK\r>";
        }

        void failOn(
                @NonNull String command) {

            failingCommand =
                    command;
        }

        int getCommandCount() {

            return commands.size();
        }

        @NonNull
        java.util.List<String> getCommands() {

            return commands;
        }
    }

    /**
     * Verifica il percorso completo:
     *
     * target
     *   ↓
     * command plan
     *   ↓
     * execution
     */
    @Test
    public void configureAndExecuteWorks()
            throws Exception {

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        FakeCommandSender sender =
                new FakeCommandSender();

        Elm327CommandExecutor executor =
                new Elm327CommandExecutor(
                        sender
                );

        configurator.configureAndExecute(
                target,
                executor
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
                sender.getCommandCount()
        );
    }


    @Test
    public void automaticProtocolDoesNotConfigureCan()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "AUTO",
                        "000",
                        "000",
                        "FUNCTIONAL",
                        11,
                        0
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        Elm327CommandPlan plan =
                configurator.buildCommandPlan(
                        target
                );

        assertEquals(
                2,
                plan.getCommands().size()
        );

        assertEquals(
                "ATE0",
                plan.getCommands().get(0)
        );

        assertEquals(
                "ATH0",
                plan.getCommands().get(1)
        );
    }



    @Test
    public void automaticProtocolDoesNotUseCanCommands()
            throws Exception {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "AUTO",
                        "000",
                        "000",
                        "FUNCTIONAL",
                        11,
                        0
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        Elm327CommandPlan plan =
                configurator.buildCommandPlan(
                        target
                );

        for (
                String command :
                plan.getCommands()
        ) {

            assertTrue(
                    !command.startsWith(
                            "ATTP"
                    )
            );

            assertTrue(
                    !command.startsWith(
                            "ATSH"
                    )
            );

            assertTrue(
                    !command.startsWith(
                            "ATCRA"
                    )
            );
        }
    }
}