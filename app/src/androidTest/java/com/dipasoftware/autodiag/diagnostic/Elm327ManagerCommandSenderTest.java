package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerCommandSenderTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ManagerCommandSenderTest {

    /**
     * Verifica inoltro del comando verso Elm327Manager.
     */
    @Test
    public void sendCommandIsForwardedToManager()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "OK\r>"
                );

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        Elm327ManagerCommandSender sender =
                new Elm327ManagerCommandSender(
                        manager
                );

        String response =
                sender.sendCommand(
                        "ATE0"
                );

        assertEquals(
                "OK\r>",
                response
        );

        assertEquals(
                "ATE0\r",
                connection.getLastSentData()
        );

        assertNotNull(
                sender.getElm327Manager()
        );
    }

    /**
     * Verifica che ogni comando venga inoltrato.
     */
    @Test
    public void multipleCommandsAreForwarded()
            throws Exception {

        FakeConnection connection =
                new FakeConnection(
                        "OK\r>"
                );

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        Elm327ManagerCommandSender sender =
                new Elm327ManagerCommandSender(
                        manager
                );

        sender.sendCommand(
                "ATSH 7E0"
        );

        assertEquals(
                "ATSH 7E0\r",
                connection.getLastSentData()
        );

        sender.sendCommand(
                "ATCRA 7E8"
        );

        assertEquals(
                "ATCRA 7E8\r",
                connection.getLastSentData()
        );
    }

    /**
     * Connection simulata.
     */
    private static class FakeConnection
            implements Connection {

        /**
         * Risposta da restituire.
         */
        private final String response;

        /**
         * Ultimi dati ricevuti.
         */
        private String lastSentData =
                "";

        FakeConnection(
                String response) {

            this.response =
                    response;
        }

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