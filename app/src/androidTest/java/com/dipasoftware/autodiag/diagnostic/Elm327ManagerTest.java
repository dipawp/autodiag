package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertEquals;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;

import java.io.IOException;


/******************************************************************************
 *
 * Classe.....: Elm327ManagerTest
 *
 * Tipo.......: Unit Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il comportamento di Elm327Manager utilizzando
 * una implementazione fittizia di Connection.
 *
 * Il test non utilizza Bluetooth reale.
 *
 ******************************************************************************/
public class Elm327ManagerTest {

    /**
     * Verifica che sendCommand():
     *
     * - aggiunga il carattere CR al comando;
     * - utilizzi Connection.send();
     * - restituisca la risposta ricevuta.
     */
    @Test
    public void testSendCommand() throws IOException {

        FakeConnection connection =
                new FakeConnection();

        connection.response = "OK\r";

        Elm327Manager manager =
                new Elm327Manager(connection);

        String response =
                manager.sendCommand("AT Z");

        assertEquals(
                "AT Z\r",
                connection.lastSentData
        );

        assertEquals(
                "OK\r",
                response
        );
    }

    /**
     * Implementazione fittizia di Connection.
     *
     * Viene utilizzata esclusivamente dal test.
     */
    private static class FakeConnection
            implements Connection {

        private boolean connected;

        private String lastSentData;

        private String response;

        @Override
        public void connect() {

            connected = true;
        }

        @Override
        public void disconnect() {

            connected = false;
        }

        @Override
        public boolean isConnected() {

            return connected;
        }

        @Override
        public void send(String data) {

            lastSentData = data;
        }

        @Override
        public String receive() {

            return response;
        }
    }
}