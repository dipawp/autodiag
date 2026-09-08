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
                3,
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

        assertEquals(
                "ATDPN\r",
                connection
                        .getSentCommands()
                        .get(2)
        );

        assertEquals(
                "6\r>",
                result.getProtocolNumber()
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

            /*
             * Ogni comando riceve una risposta propria.
             */
            if ("0100\r".equals(
                    data
            )) {

                response =
                        "41 00 BE 3F A8 13\r>";

                return;
            }

            /*
             * Comandi di inizializzazione ELM327.
             */
            response =
                    "OK\r";
        }

        @Override
        public String receive() {

            if (response != null) {

                String currentResponse =
                        response;

                response =
                        null;

                return currentResponse;
            }

            return "OK\r";
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

        connection.connect();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        String response =
                manager.executeRealObdRequest(
                        "01 00"
                );

        assertEquals(
                "OK\r",
                response
        );

        /*
         * L'ultima operazione del fake deve essere la richiesta
         * OBD normalizzata.
         */
        assertEquals(
                "0100\r",
                connection.lastSentData
        );

        assertTrue(
                connection.getSentCommands()
                        .contains(
                                "0100\r"
                        )
        );
    }


}