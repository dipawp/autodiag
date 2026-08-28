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
 * Classe.....: Elm327ManagerCatalogExecutorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ManagerCatalogExecutorTest {

    /**
     * Verifica la creazione del nuovo executor.
     */
    @Test
    public void managerCreatesCatalogExecutor() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticPidExecutor executor =
                manager.createCatalogDiagnosticPidExecutor();

        assertNotNull(
                executor
        );

        assertNotNull(
                executor.getTransport()
        );

        assertTrue(
                executor.getTransport()
                        instanceof Elm327DiagnosticTransport
        );
    }

    /**
     * Verifica che il nuovo executor utilizzi
     * un Elm327DiagnosticTransport con configurazione.
     */
    @Test
    public void catalogExecutorHasConfigurationPath() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticPidExecutor executor =
                manager.createCatalogDiagnosticPidExecutor();

        Elm327DiagnosticTransport transport =
                (Elm327DiagnosticTransport)
                        executor.getTransport();

        assertNotNull(
                transport.getAdapterConfigurator()
        );

        assertNotNull(
                transport.getConfigurationExecutor()
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