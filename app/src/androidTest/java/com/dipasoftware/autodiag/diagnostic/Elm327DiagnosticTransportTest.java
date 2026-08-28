package com.dipasoftware.autodiag.diagnostic;

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
 * Classe.....: Elm327DiagnosticTransportTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327DiagnosticTransportTest {

    /**
     * Verifica invio e ricezione con target CAN 11 bit.
     */
    @Test
    public void sendAndReceivePreserveTarget()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90 12 34\r>"
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection
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

        assertEquals(
                "22F190",
                transport.getLastRequest()
        );

        assertNotNull(
                transport.getLastTarget()
        );

        assertEquals(
                "7E0",
                transport
                        .getLastTarget()
                        .getRequestId()
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
     * Verifica che un transport non collegato fallisca.
     */
    @Test(expected = java.io.IOException.class)
    public void disconnectedConnectionIsRejected()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        ""
                );

        connection.connected =
                false;

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection
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
                "010C"
        );
    }

    /**
     * Verifica request vuota.
     */
    @Test(expected = java.io.IOException.class)
    public void emptyRequestIsRejected()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        ""
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection
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
                "   "
        );
    }

    /**
     * Verifica che receive senza send precedente
     * non venga accettato.
     */
    @Test(expected = java.io.IOException.class)
    public void receiveWithoutSendIsRejected()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90"
                );

        Elm327DiagnosticTransport transport =
                new Elm327DiagnosticTransport(
                        connection
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                );

        transport.receive(
                target
        );
    }

    /**
     * Connection simulata.
     */
    private static class FakeConnection
            implements Connection {

        /**
         * Risposta.
         */
        private final String response;

        /**
         * Ultimi dati inviati.
         */
        private String lastSentData =
                "";

        /**
         * Stato connessione.
         */
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
}