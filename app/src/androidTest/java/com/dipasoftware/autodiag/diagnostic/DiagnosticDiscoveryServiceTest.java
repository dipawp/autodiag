package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryServiceTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Descrizione:
 *
 * Verifica il percorso completo di discovery:
 *
 *     VIN
 *      ↓
 *     VehicleCatalogMatcher
 *      ↓
 *     EcuDefinition
 *      ↓
 *     EcuIdentifier
 *      ↓
 *     EcuCatalogMatcher
 *
 * Nessuna ECU reale viene utilizzata.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoveryServiceTest {

    /**
     * VIN fittizio di test.
     *
     * Lunghezza: 17 caratteri.
     */
    private static final String TEST_VIN =
            "TESTVIN1234567890";

    /**
     * Verifica che la discovery produca una identificazione
     * veicolo valida e mantenga le candidate del catalogo.
     */
    @Test
    public void discoveryKeepsVehicleCandidates()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        DiagnosticDiscoveryService service =
                new DiagnosticDiscoveryService(
                        context,
                        executor
                );

        DiagnosticDiscoveryResult result =
                service.discover();

        assertNotNull(
                result
        );

        assertNotNull(
                result.getVehicleIdentification()
        );

        assertTrue(
                result
                        .getVehicleIdentification()
                        .hasVin()
        );

        assertEquals(
                TEST_VIN,
                result
                        .getVehicleIdentification()
                        .getVin()
        );

        assertTrue(
                result.hasVehicleCandidates()
        );
    }

    /**
     * Verifica che una candidate unica venga identificata.
     */
    @Test
    public void uniqueVehicleCandidateIsIdentified()
            throws Exception {

        FakeConnection connection =
                new FakeConnection();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        DiagnosticDiscoveryService service =
                new DiagnosticDiscoveryService(
                        context,
                        executor
                );

        DiagnosticDiscoveryResult result =
                service.discover();

        assertNotNull(
                result
        );

        /*
         * Il VIN deve aver prodotto una sola ECU
         * candidate nel catalogo TEST.
         */
        assertEquals(
                1,
                result.getVehicleCandidates().size()
        );

        /*
         * L'identificazione ECU deve essere stata
         * effettuata.
         */
        assertTrue(
                result.hasEcuIdentification()
        );

        assertNotNull(
                result.getEcuIdentification()
        );

        /*
         * Deve essere disponibile anche il risultato
         * del matching ECU.
         */
        assertTrue(
                result.hasEcuMatchResult()
        );

        assertNotNull(
                result.getEcuMatchResult()
        );

        /*
         * Verifichiamo il VIN letto dalla vettura.
         */
        assertEquals(
                TEST_VIN,
                result
                        .getEcuIdentification()
                        .getVin()
        );
    }

    /**
     * Connection simulata.
     *
     * Simula:
     *
     * - OBD Mode 09 PID 02 per il VIN;
     * - UDS ReadDataByIdentifier 0x22 per
     *   gli identificatori ECU dichiarati dal catalogo.
     */
    private static class FakeConnection
            implements Connection {

        /**
         * Ultima request.
         */
        @NonNull
        private String lastRequest =
                "";

        /**
         * Stato della connection.
         */
        private boolean connected =
                true;

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

            lastRequest =
                    data
                            .replace(
                                    "\r",
                                    ""
                            )
                            .trim()
                            .toUpperCase();
        }

        @Override
        public String receive() {

            /*
             * ---------------------------------------------------------
             * OBD-II MODE 09 PID 02 - VIN
             * ---------------------------------------------------------
             *
             * 49 02 01 [17 ASCII characters]
             *
             * TESTVIN1234567890
             */
            if ("0902".equals(
                    lastRequest
            )) {

                return
                        "49 02 01 "
                                + "54 45 53 54 56 49 4E "
                                + "31 32 33 34 35 36 37 38 39 30"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F190 - VIN
             * ---------------------------------------------------------
             */
            if ("22F190".equals(
                    lastRequest
            )) {

                return
                        "62 F1 90 "
                                + "54 45 53 54 56 49 4E "
                                + "31 32 33 34 35 36 37 38 39 30"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F187 - ECU PART NUMBER
             * ---------------------------------------------------------
             */
            if ("22F187".equals(
                    lastRequest
            )) {

                return
                        "62 F1 87 "
                                + "45 43 55 2D 50 41 52 54 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F188 - ECU SOFTWARE NUMBER
             * ---------------------------------------------------------
             */
            if ("22F188".equals(
                    lastRequest
            )) {

                return
                        "62 F1 88 "
                                + "45 43 55 2D 53 57 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F189 - SOFTWARE VERSION
             * ---------------------------------------------------------
             */
            if ("22F189".equals(
                    lastRequest
            )) {

                return
                        "62 F1 89 "
                                + "31 2E 30 2E 35"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F191 - ECU HARDWARE NUMBER
             * ---------------------------------------------------------
             */
            if ("22F191".equals(
                    lastRequest
            )) {

                return
                        "62 F1 91 "
                                + "45 43 55 2D 48 57 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F18A - SUPPLIER
             * ---------------------------------------------------------
             */
            if ("22F18A".equals(
                    lastRequest
            )) {

                return
                        "62 F1 8A "
                                + "42 4F 53 43 48"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F18C - SERIAL NUMBER
             * ---------------------------------------------------------
             */
            if ("22F18C".equals(
                    lastRequest
            )) {

                return
                        "62 F1 8C "
                                + "53 45 52 49 41 4C 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F192 - SUPPLIER HARDWARE
             * ---------------------------------------------------------
             */
            if ("22F192".equals(
                    lastRequest
            )) {

                return
                        "62 F1 92 "
                                + "53 55 50 2D 48 57 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F194 - SUPPLIER SOFTWARE
             * ---------------------------------------------------------
             */
            if ("22F194".equals(
                    lastRequest
            )) {

                return
                        "62 F1 94 "
                                + "53 55 50 2D 53 57 2D 30 30 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F195 - SUPPLIER SOFTWARE VERSION
             * ---------------------------------------------------------
             */
            if ("22F195".equals(
                    lastRequest
            )) {

                return
                        "62 F1 95 "
                                + "32 2E 31"
                                + "\r>";
            }

            /*
             * ---------------------------------------------------------
             * UDS F197 - SYSTEM NAME
             * ---------------------------------------------------------
             */
            if ("22F197".equals(
                    lastRequest
            )) {

                return
                        "62 F1 97 "
                                + "54 45 53 54 5F 45 4E 47 49 4E 45"
                                + "\r>";
            }

            /*
             * DID non riconosciuto.
             */
            return
                    "7F 22 31\r>";
        }
    }
}