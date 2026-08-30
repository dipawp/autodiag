package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerSharedExecutorTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class Elm327ManagerSharedExecutorTest {

    /**
     * Verifica che il manager restituisca sempre lo stesso
     * executor catalog-driven.
     */
    @Test
    public void catalogExecutorIsShared() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticPidExecutor first =
                manager.createCatalogDiagnosticPidExecutor();

        DiagnosticPidExecutor second =
                manager.createCatalogDiagnosticPidExecutor();

        assertNotNull(
                first
        );

        assertNotNull(
                second
        );

        assertSame(
                first,
                second
        );
    }

    /**
     * Verifica che EcuIdentifier usi lo stesso executor
     * catalog-driven del manager.
     */
    @Test
    public void ecuIdentifierUsesSharedExecutor() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticPidExecutor executor =
                manager.createCatalogDiagnosticPidExecutor();

        EcuIdentifier identifier =
                manager.createCatalogEcuIdentifier();

        assertNotNull(
                identifier
        );

        assertSame(
                executor,
                identifier.getExecutor()
        );
    }

    /**
     * Verifica che i componenti creati dal manager condividano
     * anche lo stesso transport.
     */
    @Test
    public void sharedExecutorUsesSameTransport() {

        FakeConnection connection =
                new FakeConnection();

        Elm327Manager manager =
                new Elm327Manager(
                        connection
                );

        DiagnosticPidExecutor executor =
                manager.createCatalogDiagnosticPidExecutor();

        EcuIdentifier identifier =
                manager.createCatalogEcuIdentifier();

        assertSame(
                executor.getTransport(),
                identifier
                        .getExecutor()
                        .getTransport()
        );

        assertTrue(
                executor.getTransport()
                        instanceof Elm327DiagnosticTransport
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