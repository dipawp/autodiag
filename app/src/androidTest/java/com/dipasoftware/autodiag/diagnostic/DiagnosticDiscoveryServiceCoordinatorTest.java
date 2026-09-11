package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoveryServiceCoordinatorTest {

    @Test
    public void serviceCanBeUsedAsDiscoveryRunner() {

        Context context =
                ApplicationProvider.getApplicationContext();

        DiagnosticDiscoveryService service =
                new DiagnosticDiscoveryService(
                        context,
                        new DiagnosticPidExecutor(
                                new TestConnection()
                        )
                );

        DiagnosticDiscoveryCoordinator.DiscoveryRunner runner =
                service;

        assertNotNull(runner);
        assertTrue(
                runner instanceof DiagnosticDiscoveryService
        );
    }

    private static class TestConnection
            implements com.dipasoftware.autodiag.connection.Connection {

        private boolean connected = true;

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
            // Nessuna comunicazione necessaria per questo test.
        }

        @Override
        public String receive() {
            return "NO DATA\r>";
        }
    }
}