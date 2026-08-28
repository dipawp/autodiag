package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerDiagnosticTransportTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ManagerDiagnosticTransportTest {

    /**
     * Verifica che Elm327Manager crei correttamente il nuovo
     * transport catalog-driven.
     */
    @Test
    public void managerCreatesCatalogDiagnosticTransport()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticTransport transport =
                manager.createDiagnosticTransport();

        assertNotNull(
                transport
        );

        assertTrue(
                transport
                        instanceof
                        Elm327DiagnosticTransport
        );

        Elm327DiagnosticTransport elmTransport =
                (Elm327DiagnosticTransport)
                        transport;

        assertNotNull(
                elmTransport
                        .getAdapterConfigurator()
        );

        assertNotNull(
                elmTransport
                        .getConfigurationExecutor()
        );
    }

    /**
     * Verifica che il transport creato dal manager possa
     * configurare il target e inviare la richiesta diagnostica.
     */
    @Test
    public void managerCreatedTransportRunsConfiguration()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticTransport transport =
                manager.createDiagnosticTransport();

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

        /*
         * I primi cinque comandi devono essere
         * quelli della configurazione ELM327.
         */
        assertEquals(
                6,
                connection.getSentCommands().size()
        );

        assertEquals(
                "ATSP6\r",
                connection
                        .getSentCommands()
                        .get(0)
        );

        assertEquals(
                "ATSH 7E0\r",
                connection
                        .getSentCommands()
                        .get(1)
        );

        assertEquals(
                "ATCRA 7E8\r",
                connection
                        .getSentCommands()
                        .get(2)
        );

        assertEquals(
                "ATE0\r",
                connection
                        .getSentCommands()
                        .get(3)
        );

        assertEquals(
                "ATH0\r",
                connection
                        .getSentCommands()
                        .get(4)
        );

        /*
         * La sesta operazione è la vera richiesta diagnostica.
         */
        assertEquals(
                "22F190\r",
                connection
                        .getSentCommands()
                        .get(5)
        );
    }

    /**
     * Verifica che il transport creato dal manager riceva
     * correttamente la risposta diagnostica.
     */
    @Test
    public void managerCreatedTransportCanReceiveResponse()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticTransport transport =
                manager.createDiagnosticTransport();

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

        String response =
                transport.receive(
                        target
                );

        assertEquals(
                "62 F1 90 12 34\r>",
                response
        );
    }

    /**
     * Connection simulata.
     */
    private static class FakeConnection
            implements Connection {

        @NonNull
        private final java.util.List<String>
                sentCommands =
                new java.util.ArrayList<>();

        private int receiveIndex =
                0;

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean isConnected() {

            return true;
        }

        @Override
        public void send(
                String data) {

            sentCommands.add(
                    data
            );
        }

        @Override
        public String receive() {

            /*
             * Risposte per i cinque comandi AT.
             */
            if (receiveIndex < 5) {

                receiveIndex++;

                return "OK\r>";
            }

            /*
             * Risposta diagnostica.
             */
            return "62 F1 90 12 34\r>";
        }

        @NonNull
        java.util.List<String>
        getSentCommands() {

            return sentCommands;
        }
    }
}