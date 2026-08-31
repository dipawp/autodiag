package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327DiagnosticTransportTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327DiagnosticTransportTest {

    /**
     * Verifica invio, configurazione e ricezione.
     */
    @Test
    public void sendAndReceivePreserveTarget()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90 12 34\r>"
                );

        FakeConfigurator configurator =
                new FakeConfigurator();

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        transport.send(
                target,
                "22F190"
        );

        assertEquals(
                "22F190\r",
                connection.getLastSentData()
        );

        assertTrue(
                transport.isWaitingResponse()
        );

        assertEquals(
                target,
                transport.getConfiguredTarget()
        );

        assertEquals(
                1,
                configurator.getConfigureCount()
        );

        String response =
                transport.receive(
                        target
                );

        assertEquals(
                "62 F1 90 12 34\r>",
                response
        );

        assertTrue(
                transport
                        .getState()
                        .hasResponse()
        );
    }

    /**
     * Verifica che lo stesso target non venga
     * riconfigurato a ogni request.
     */
    @Test
    public void sameTargetDoesNotReconfigure()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90\r>"
                );

        FakeConfigurator configurator =
                new FakeConfigurator();

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        transport.send(
                target,
                "22F190"
        );

        transport.receive(
                target
        );

        transport.send(
                target,
                "22F191"
        );

        assertEquals(
                1,
                configurator.getConfigureCount()
        );
    }

    /**
     * Verifica che cambiando target venga richiesta
     * una nuova configurazione.
     */
    @Test
    public void differentTargetReconfiguresAdapter()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90\r>"
                );

        FakeConfigurator configurator =
                new FakeConfigurator();

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator
                );

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

        transport.send(
                first,
                "22F190"
        );

        transport.receive(
                first
        );

        transport.send(
                second,
                "22F191"
        );

        assertEquals(
                2,
                configurator.getConfigureCount()
        );

        assertEquals(
                second,
                transport.getConfiguredTarget()
        );
    }

    /**
     * Verifica che un target errato durante receive
     * venga rifiutato.
     */
    @Test(expected = java.io.IOException.class)
    public void receiveWithDifferentTargetIsRejected()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90\r>"
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection
                );

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

        transport.send(
                first,
                "22F190"
        );

        transport.receive(
                second
        );
    }

    /**
     * Verifica reset della configurazione.
     */
    @Test
    public void resetAdapterConfigurationClearsTarget()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90\r>"
                );

        FakeConfigurator configurator =
                new FakeConfigurator();

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        transport.send(
                target,
                "22F190"
        );

        assertNotNull(
                transport.getConfiguredTarget()
        );

        transport.resetAdapterConfiguration();

        assertFalse(
                configurator.isConfigured()
        );

        assertEquals(
                null,
                transport.getConfiguredTarget()
        );
    }

    /**
     * Configuratore fittizio.
     */
    private static class FakeConfigurator
            implements DiagnosticAdapterConfigurator {

        /**
         * Target corrente.
         */
        private DiagnosticTargetDefinition target;

        /**
         * Numero configure().
         */
        private int configureCount =
                0;

        @Override
        public void configure(
                DiagnosticTargetDefinition target) {

            this.target =
                    target;

            configureCount++;
        }

        @Override
        public void reset() {

            target =
                    null;
        }

        boolean isConfigured() {

            return target != null;
        }

        int getConfigureCount() {

            return configureCount;
        }
    }

    /**
     * Connection fittizia.
     */
    private static class FakeConnection
            implements Connection {

        private final String response;

        private String lastSentData =
                "";

        private boolean connected =
                true;

        FakeConnection(
                String response) {

            this.response =
                    response;
        }

        @Override
        public void connect() {

            connected =
                    true;
        }

        @Override
        public void disconnect() {

            connected =
                    false;
        }

        @Override
        public boolean isConnected() {

            return connected;
        }

        @Override
        public void send(
                String data) {

            lastSentData =
                    data;
        }

        @Override
        public String receive() {

            return response;
        }

        String getLastSentData() {

            return lastSentData;
        }
    }

    /**
     * Verifica che il nuovo transport esegua la configurazione
     * prima della request diagnostica.
     */
    @Test
    public void targetConfigurationIsExecutedBeforeDiagnosticRequest()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90 12 34\r>"
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        FakeCommandSender commandSender =
                new FakeCommandSender();

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        commandSender
                );

        Elm327ConfigurationExecutor configurationExecutor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator,
                        configurationExecutor
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

        transport.send(
                target,
                "22F190"
        );

        assertEquals(
                5,
                commandSender.getCount()
        );

        assertEquals(
                "ATTP6",
                commandSender.getCommands().get(0)
        );

        assertEquals(
                "ATSH 7E0",
                commandSender.getCommands().get(1)
        );

        assertEquals(
                "ATCRA 7E8",
                commandSender.getCommands().get(2)
        );

        assertEquals(
                "ATE0",
                commandSender.getCommands().get(3)
        );

        assertEquals(
                "ATH0",
                commandSender.getCommands().get(4)
        );

        assertEquals(
                "22F190\r",
                connection.getLastSentData()
        );
    }


    /**
     * Verifica che un errore di configurazione impedisca
     * l'invio della request diagnostica.
     */
    @Test
    public void configurationFailurePreventsDiagnosticRequest()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90 12 34\r>"
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        FakeCommandSender commandSender =
                new FakeCommandSender();

        commandSender.failOn(
                "ATSH 7E0"
        );

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        commandSender
                );

        Elm327ConfigurationExecutor configurationExecutor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator,
                        configurationExecutor
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

        try {

            transport.send(
                    target,
                    "22F190"
            );

            org.junit.Assert.fail(
                    "Era attesa una IOException."
            );

        } catch (
                java.io.IOException expected) {

            assertEquals(
                    2,
                    commandSender.getCount()
            );

            assertEquals(
                    "",
                    connection.getLastSentData()
            );

            assertEquals(
                    null,
                    transport.getConfiguredTarget()
            );
        }
    }

    /**
     * Verifica che un target già configurato non venga
     * riconfigurato per ogni richiesta successiva.
     */
    @Test
    public void sameTargetIsConfiguredOnlyOnce()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90\r>"
                );

        Elm327AdapterConfigurator configurator =
                new Elm327AdapterConfigurator();

        FakeCommandSender commandSender =
                new FakeCommandSender();

        Elm327CommandExecutor commandExecutor =
                new Elm327CommandExecutor(
                        commandSender
                );

        Elm327ConfigurationExecutor configurationExecutor =
                new Elm327ConfigurationExecutor(
                        commandExecutor
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection,
                        configurator,
                        configurationExecutor
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

        transport.send(
                target,
                "22F190"
        );

        transport.receive(
                target
        );

        transport.send(
                target,
                "22F191"
        );

        /*
         * Una sola configurazione:
         *
         * ATSP6
         * ATSH
         * ATCRA
         * ATE0
         * ATH0
         */
        assertEquals(
                5,
                commandSender.getCount()
        );
    }


    /**
     * Sender fittizio per i comandi AT.
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

        int getCount() {

            return commands.size();
        }

        @NonNull
        java.util.List<String> getCommands() {

            return commands;
        }
    }
}