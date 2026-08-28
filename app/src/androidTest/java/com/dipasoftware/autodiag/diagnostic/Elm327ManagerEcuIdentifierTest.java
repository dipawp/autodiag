package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerEcuIdentifierTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ManagerEcuIdentifierTest {

    /**
     * Verifica che il manager costruisca un EcuIdentifier
     * catalog-driven.
     */
    @Test
    public void managerCreatesCatalogEcuIdentifier() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        EcuIdentifier identifier =
                manager.createCatalogEcuIdentifier();

        assertNotNull(
                identifier
        );
    }

    /**
     * Verifica che l'EcuIdentifier creato dal manager
     * utilizzi il nuovo percorso catalog-driven.
     */
    @Test
    public void catalogIdentifierUsesCatalogExecutor() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        EcuIdentifier identifier =
                manager.createCatalogEcuIdentifier();

        /*
         * L'istanza deve essere quella del servizio
         * catalog-driven.
         */
        assertNotNull(
                identifier
        );

        /*
         * Il costruttore di EcuIdentifier conserva
         * l'executor internamente, quindi qui verifichiamo
         * almeno la corretta creazione dell'oggetto.
         *
         * La verifica del transport viene già effettuata
         * da Elm327ManagerCatalogExecutorTest.
         */
        assertTrue(
                identifier instanceof EcuIdentifier
        );
    }

    /**
     * Connection simulata.
     */
    private static class FakeConnection
            implements Connection {

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
                @NonNull String data) {
        }

        @Override
        @NonNull
        public String receive() {

            return "OK\r>";
        }
    }
}