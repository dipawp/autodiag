package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoverySessionTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoverySessionTest {

    /**
     * Verifica stato iniziale.
     */
    @Test
    public void sessionStartsOpen() {

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        new FakeConnection()
                );

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        executor
                );

        assertTrue(
                session.isOpen()
        );

        assertFalse(
                session.isClosed()
        );
    }

    /**
     * Verifica che executor e componenti siano condivisi.
     */
    @Test
    public void sessionSharesExecutor() {

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        new FakeConnection()
                );

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        executor
                );

        assertSame(
                executor,
                session.getExecutor()
        );

       /* assertSame(
                executor,
                session
                        .getVehicleIdentifier()
                        .getExecutor()
        );*/

        assertSame(
                executor,
                session
                        .getEcuIdentifier()
                        .getExecutor()
        );
    }

    /**
     * Verifica che VehicleIdentifier ed EcuIdentifier
     * condividano lo stesso transport.
     */
    @Test
    public void sessionSharesTransport() {

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        new FakeConnection()
                );

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        executor
                );

        /*assertSame(
                session
                        .getVehicleIdentifier()
                        .getExecutor()
                        .getTransport(),
                session
                        .getEcuIdentifier()
                        .getExecutor()
                        .getTransport()
        );*/
    }

    /**
     * Verifica chiusura sessione.
     */
    @Test
    public void closeMarksSessionClosed() {

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        new DiagnosticPidExecutor(
                                new FakeConnection()
                        )
                );

        session.close();

        assertTrue(
                session.isClosed()
        );

        assertFalse(
                session.isOpen()
        );
    }

    /**
     * Verifica che una sessione chiusa non permetta
     * nuove identificazioni.
     */
    @Test(expected = IllegalStateException.class)
    public void closedSessionRejectsVehicleIdentification() {

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        new DiagnosticPidExecutor(
                                new FakeConnection()
                        )
                );

        session.close();

        session.identifyVehicle();
    }

    /**
     * Verifica che una sessione chiusa non permetta
     * identificazioni ECU.
     */
    @Test(expected = IllegalStateException.class)
    public void closedSessionRejectsEcuIdentification() {

        DiagnosticDiscoverySession session =
                new DiagnosticDiscoverySession(
                        new DiagnosticPidExecutor(
                                new FakeConnection()
                        )
                );

        session.close();

        EcuDefinition definition =
                new EcuDefinition(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU",
                        "CAN",
                        "",
                        new EcuDefinitionIdentifier(),
                        Collections.emptyList(),
                        new DiagnosticTargetDefinition(
                                "CAN",
                                "7E0",
                                "7E8",
                                "PHYSICAL",
                                11,
                                500
                        ),
                        Collections.emptyList()
                );

        session.identifyEcu(
                definition
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

            return "";
        }
    }
}