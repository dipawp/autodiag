package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        connection.response =
                "OK\r";

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        String response =
                manager.sendCommand(
                        "AT Z"
                );

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
     * Verifica che checkRealConnection():
     *
     * - utilizzi ATI;
     * - utilizzi ATDP;
     * - invii entrambi i comandi tramite Connection;
     * - restituisca un risultato valido.
     */
    @Test
    public void checkRealConnectionUsesAdapterDiagnostics()
            throws IOException {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticRealConnectionCheck.Result result =
                manager.checkRealConnection();

        assertTrue(
                result.isValid()
        );

        assertTrue(
                result.looksLikeElm327()
        );

        assertEquals(
                2,
                connection.getSentCommands().size()
        );

        assertEquals(
                "ATI\r",
                connection
                        .getSentCommands()
                        .get(0)
        );

        assertEquals(
                "ATDP\r",
                connection
                        .getSentCommands()
                        .get(1)
        );
    }

    /**
     * Implementazione fittizia di Connection.
     *
     * Viene utilizzata esclusivamente dal test.
     */
    private static class FakeConnection
            implements Connection {

        /**
         * Stato della connessione.
         */
        private boolean connected;

        /**
         * Ultimo comando inviato.
         *
         * Manteniamo questo campo perché viene utilizzato
         * dal test legacy.
         */
        private String lastSentData;

        /**
         * Tutti i comandi inviati.
         */
        private final List<String> sentCommands =
                new ArrayList<>();

        /**
         * Risposta restituita da receive().
         */
        private String response;

        /**
         * Risposta progressiva per ATI/ATDP.
         */
        private int receiveIndex =
                0;

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

            sentCommands.add(
                    data
            );
        }

        @Override
        public String receive() {

            /*
             * Quando il vecchio test imposta esplicitamente
             * una response, continuiamo a restituire quella.
             */
            if (response != null) {

                return response;
            }

            /*
             * Risposte utilizzate dal test
             * checkRealConnection().
             */
            if (receiveIndex == 0) {

                receiveIndex++;

                return "ELM327 v1.5\r>";
            }

            return "ISO 15765-4 (CAN 11/500)\r>";
        }

        List<String> getSentCommands() {

            return sentCommands;
        }
    }
    @Test
    public void executeRealObdRequestUsesDirectObdPath()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        connection.response =
                "41 00 BE 3F A8 13\r>";

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        String response =
                manager.executeRealObdRequest(
                        "01 00"
                );

        assertEquals(
                "41 00 BE 3F A8 13\r>",
                response
        );

        assertTrue(
                connection.lastSentData
                        .contains(
                                "0100"
                        )
        );
    }

}