package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ConfigurationExecutorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ConfigurationExecutorTest {

    /**
     * Verifica esecuzione completa del piano.
     */
    @Test
    public void executeRunsCompletePlan()
            throws Exception {

        FakeCommandSender sender =
                new FakeCommandSender();

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        sender
                );

        Elm327ConfigurationExecutor executor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        Elm327CommandPlan plan =
                new Elm327CommandPlan(
                        target,
                        Arrays.asList(
                                "ATSP6",
                                "ATSH 7E0",
                                "ATCRA 7E8",
                                "ATE0",
                                "ATH0"
                        )
                );

        executor.execute(
                plan
        );

        assertEquals(
                5,
                sender.getCount()
        );

        assertEquals(
                "ATSP6",
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
     * Verifica che l'esecuzione si interrompa al primo errore.
     */
    @Test
    public void executeStopsAtFirstError()
            throws Exception {

        FakeCommandSender sender =
                new FakeCommandSender();

        sender.failOn(
                "ATSH 7E0"
        );

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        sender
                );

        Elm327ConfigurationExecutor executor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        Elm327CommandPlan plan =
                new Elm327CommandPlan(
                        target,
                        Arrays.asList(
                                "ATSP6",
                                "ATSH 7E0",
                                "ATCRA 7E8",
                                "ATE0",
                                "ATH0"
                        )
                );

        try {

            executor.execute(
                    plan
            );

        } catch (
                IOException exception) {

            /*
             * L'errore è atteso.
             */
            assertEquals(
                    "Risposta inattesa da ATSH 7E0: ?",
                    exception.getMessage()
            );

            /*
             * Deve essere stato eseguito soltanto:
             *
             * 1. ATSP6
             * 2. ATSH 7E0
             *
             * I comandi successivi NON devono essere inviati.
             */
            assertEquals(
                    2,
                    sender.getCount()
            );

            assertEquals(
                    "ATSP6",
                    sender.getCommands().get(0)
            );

            assertEquals(
                    "ATSH 7E0",
                    sender.getCommands().get(1)
            );

            return;
        }

        /*
         * Se non viene generata l'eccezione, il test deve fallire.
         */
        org.junit.Assert.fail(
                "Era attesa una IOException durante l'esecuzione "
                        + "del comando ATSH 7E0."
        );
    }

    /**
     * Verifica piano vuoto.
     */
    @Test(expected = IOException.class)
    public void emptyPlanIsRejected()
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

        Elm327CommandPlan plan =
                new Elm327CommandPlan(
                        target,
                        java.util.Collections.emptyList()
                );

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        new FakeCommandSender()
                );

        Elm327ConfigurationExecutor executor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        executor.execute(
                plan
        );
    }

    /**
     * Sender fittizio.
     */
    private static class FakeCommandSender
            implements Elm327CommandExecutor.CommandSender {

        @NonNull
        private final java.util.List<String> commands =
                new java.util.ArrayList<>();

        @NonNull
        private String failingCommand =
                "";

        @Override
        @NonNull
        public String sendCommand(
                @NonNull String command)
                throws IOException {

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

        int getCount() {

            return commands.size();
        }

        @NonNull
        java.util.List<String> getCommands() {

            return commands;
        }
    }
}