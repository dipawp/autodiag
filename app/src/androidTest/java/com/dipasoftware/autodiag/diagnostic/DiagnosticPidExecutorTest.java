package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticPidExecutorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il percorso completo di esecuzione di un parametro
 * diagnostico senza utilizzare un ELM327 reale.
 *
 * Viene utilizzata una Connection simulata.
 *
 * I test verificano:
 *
 * - costruzione della request;
 * - invio tramite Connection;
 * - conservazione della risposta raw;
 * - parsing OBD-II standard;
 * - parsing UDS;
 * - risultato diagnostico normalizzato.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticPidExecutorTest {

    /**
     * Verifica l'esecuzione di un PID OBD-II standard.
     *
     * La request viene costruita dal PID completo:
     *
     * 010C
     *
     * e la risposta simulata è:
     *
     * 41 0C 1A F8
     */
    @Test
    public void executeStandardPid()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "41 0C 1A F8\r>"
                );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        PidDefinition definition =
                new PidDefinition(
                        "010C",
                        "pid_engine_rpm",
                        "pid_engine_rpm_description",
                        "rpm",
                        "FORMULA",
                        "((A*256)+B)/4",
                        2,
                        "01",
                        "RPM"
                );

        DiagnosticPidExecutor.DiagnosticPidExecution execution =
                executor.execute(
                        definition
                );

        /**
         * Verifica la request effettivamente costruita
         * ed eseguita.
         */
        assertEquals(
                "010C",
                execution.getRequest()
        );

        /**
         * Verifica che la risposta raw originale
         * sia stata conservata.
         */
        assertEquals(
                "41 0C 1A F8\r>",
                execution.getRawResponse()
        );

        /**
         * Verifica che la risposta sia stata interpretata
         * correttamente.
         */
        assertTrue(
                execution.hasParsedResponse()
        );

        DiagnosticResponseResult result =
                execution.getParsedResponse();

        assertNotNull(
                result
        );

        /**
         * La risposta deve essere positiva.
         */
        assertTrue(
                result.isPositive()
        );

        /**
         * Il parser utilizzato deve essere quello OBD-II.
         */
        assertEquals(
                "OBD",
                result.getProtocolType()
        );

        /**
         * Il PID ricevuto deve essere 0C.
         */
        assertEquals(
                0x0C,
                result.getIdentifier()
        );

        /**
         * I dati devono essere:
         *
         * 1A F8
         */
        assertArrayEquals(
                new byte[]{
                        0x1A,
                        (byte) 0xF8
                },
                result.getData()
        );

        /**
         * Verifica inoltre direttamente il comando
         * inviato alla Connection simulata.
         */
        assertEquals(
                "010C\r",
                connection.getLastSentData()
        );
    }

    /**
     * Verifica l'esecuzione di un parametro OEM/UDS.
     *
     * Request:
     *
     * 22F190
     *
     * Response:
     *
     * 62 F1 90 12 34 56
     */
    @Test
    public void executeUdsDid()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "62 F1 90 12 34 56\r>"
                );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        PidDefinition definition =
                new PidDefinition(
                        "F190",
                        "pid_test_did",
                        "pid_test_did_description",
                        "",
                        "RAW",
                        "",
                        3,
                        "22",
                        "RAW",
                        "OEM",
                        false,
                        "BIG_ENDIAN",
                        0,
                        0,
                        0,
                        "22F190",
                        "62",
                        0
                );

        DiagnosticPidExecutor.DiagnosticPidExecution execution =
                executor.execute(
                        definition
                );

        /**
         * Verifica la request esplicita.
         */
        assertEquals(
                "22F190",
                execution.getRequest()
        );

        /**
         * Verifica la risposta raw.
         */
        assertEquals(
                "62 F1 90 12 34 56\r>",
                execution.getRawResponse()
        );

        /**
         * La risposta deve essere stata interpretata.
         */
        assertTrue(
                execution.hasParsedResponse()
        );

        DiagnosticResponseResult result =
                execution.getParsedResponse();

        assertNotNull(
                result
        );

        /**
         * La risposta UDS deve essere positiva.
         */
        assertTrue(
                result.isPositive()
        );

        /**
         * Il parser utilizzato deve essere UDS.
         */
        assertEquals(
                "UDS",
                result.getProtocolType()
        );

        /**
         * Il DID ricevuto deve essere F190.
         */
        assertEquals(
                0xF190,
                result.getIdentifier()
        );

        /**
         * I dati devono essere:
         *
         * 12 34 56
         */
        assertEquals(
                "12 34 56",
                result.getDataHex()
        );

        /**
         * Verifica il comando effettivamente inviato
         * alla Connection simulata.
         */
        assertEquals(
                "22F190\r",
                connection.getLastSentData()
        );
    }

    /**
     * Verifica il comportamento quando la Connection
     * restituisce una risposta vuota.
     */
    @Test
    public void executeEmptyResponse()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        ""
                );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        PidDefinition definition =
                new PidDefinition(
                        "010C",
                        "pid_engine_rpm",
                        "pid_engine_rpm_description",
                        "rpm",
                        "FORMULA",
                        "((A*256)+B)/4",
                        2,
                        "01",
                        "RPM"
                );

        DiagnosticPidExecutor.DiagnosticPidExecution execution =
                executor.execute(
                        definition
                );

        /**
         * La request deve essere comunque corretta.
         */
        assertEquals(
                "010C",
                execution.getRequest()
        );

        /**
         * La risposta raw deve essere vuota.
         */
        assertEquals(
                "",
                execution.getRawResponse()
        );

        /**
         * Nessun parsing deve essere disponibile.
         */
        assertTrue(
                !execution.hasParsedResponse()
        );

        /**
         * Nessun risultato interpretato.
         */
        assertEquals(
                null,
                execution.getParsedResponse()
        );
    }

    /**
     * Connection simulata per i test.
     *
     * Riceve una risposta predefinita senza utilizzare
     * Bluetooth, Wi-Fi o USB.
     */
    private static class FakeConnection
            implements Connection {

        /**
         * Risposta da restituire al receive().
         */
        @NonNull
        private final String response;

        /**
         * Ultimo comando inviato.
         */
        @NonNull
        private String lastSentData;

        /**
         * Costruttore.
         *
         * @param response risposta simulata.
         */
        FakeConnection(
                @NonNull String response) {

            this.response =
                    response;

            this.lastSentData =
                    "";
        }

        /**
         * Apre la connessione simulata.
         */
        @Override
        public void connect() {
        }

        /**
         * Chiude la connessione simulata.
         */
        @Override
        public void disconnect() {
        }

        /**
         * Indica che la connessione simulata è attiva.
         *
         * @return true.
         */
        @Override
        public boolean isConnected() {

            return true;
        }

        /**
         * Memorizza l'ultimo comando inviato.
         *
         * @param data dati inviati.
         */
        @Override
        public void send(
                String data) {

            lastSentData =
                    data;
        }

        /**
         * Restituisce la risposta simulata.
         *
         * @return risposta.
         */
        @Override
        public String receive() {

            return response;
        }

        /**
         * Restituisce l'ultimo comando inviato.
         *
         * @return comando.
         */
        @NonNull
        String getLastSentData() {

            return lastSentData;
        }
    }
}