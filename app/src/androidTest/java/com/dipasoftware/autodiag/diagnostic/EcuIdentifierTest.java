package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentifierTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Descrizione:
 *
 * Verifica l'identificazione automatica tramite DID UDS
 * senza utilizzare un veicolo reale.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentifierTest {

    /**
     * Verifica l'identificazione tramite VIN e dati ECU.
     *
     * La FakeConnection restituisce una risposta differente
     * in funzione della request ricevuta.
     */
    @Test
    public void identifyReadsAvailableDidValues()
            throws Exception {

        FakeIdentificationConnection connection =
                new FakeIdentificationConnection();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        EcuIdentifier identifier =
                new EcuIdentifier(
                        executor
                );

        EcuIdentification identification =
                identifier.identify();

        assertEquals(
                "TESTVIN123456789",
                identification.getVin()
        );

        assertEquals(
                "ECU-PART-001",
                identification.getEcuPartNumber()
        );

        assertEquals(
                "ECU-HW-001",
                identification.getEcuHardwareNumber()
        );

        assertEquals(
                "ECU-SW-001",
                identification.getEcuSoftwareNumber()
        );

        assertEquals(
                "1.0.5",
                identification.getEcuSoftwareVersion()
        );

        assertEquals(
                "BOSCH",
                identification.getSupplier()
        );

        assertEquals(
                "SERIAL-001",
                identification.getSerialNumber()
        );

        assertEquals(
                "SUP-HW-001",
                identification.getSupplierHardwareNumber()
        );

        assertEquals(
                "SUP-SW-001",
                identification.getSupplierSoftwareNumber()
        );

        assertEquals(
                "2.1",
                identification.getSupplierSoftwareVersion()
        );

        assertEquals(
                "TEST_ENGINE",
                identification.getSystemName()
        );

        assertTrue(
                identification.hasUsefulIdentification()
        );
    }

    /**
     * Verifica che un DID non disponibile non impedisca
     * la raccolta degli altri identificativi.
     */
    @Test
    public void unavailableDidDoesNotStopIdentification()
            throws Exception {

        FakeIdentificationConnection connection =
                new FakeIdentificationConnection();

        connection.disableDid(
                "F191"
        );

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        connection
                );

        EcuIdentifier identifier =
                new EcuIdentifier(
                        executor
                );

        EcuIdentification identification =
                identifier.identify();

        assertEquals(
                "",
                identification.getEcuHardwareNumber()
        );

        assertEquals(
                "TESTVIN123456789",
                identification.getVin()
        );

        assertEquals(
                "ECU-SW-001",
                identification.getEcuSoftwareNumber()
        );
    }

    /**
     * Connection simulata.
     */
    private static class FakeIdentificationConnection
            implements Connection {

        /**
         * DID disabilitati.
         */
        private final java.util.Set<String> disabledDids =
                new java.util.HashSet<>();

        /**
         * Stato connessione.
         */
        @Override
        public boolean isConnected() {

            return true;
        }

        /**
         * Nessuna operazione necessaria.
         */
        @Override
        public void connect() {
        }

        /**
         * Nessuna operazione necessaria.
         */
        @Override
        public void disconnect() {
        }

        /**
         * Request ignorata perché receive()
         * costruisce la risposta in base all'ultimo comando.
         */
        private String lastRequest =
                "";

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

            if (!lastRequest.startsWith("22")
                    ||
                    lastRequest.length() < 6) {

                return "";
            }

            String did =
                    lastRequest.substring(
                            2,
                            6
                    );

            if (disabledDids.contains(
                    did
            )) {

                return "7F 22 31\r>";
            }

            String value;

            switch (did) {

                case "F190":
                    value =
                            "TESTVIN123456789";
                    break;

                case "F187":
                    value =
                            "ECU-PART-001";
                    break;

                case "F188":
                    value =
                            "ECU-SW-001";
                    break;

                case "F189":
                    value =
                            "1.0.5";
                    break;

                case "F18A":
                    value =
                            "BOSCH";
                    break;

                case "F18C":
                    value =
                            "SERIAL-001";
                    break;

                case "F191":
                    value =
                            "ECU-HW-001";
                    break;

                case "F192":
                    value =
                            "SUP-HW-001";
                    break;

                case "F194":
                    value =
                            "SUP-SW-001";
                    break;

                case "F195":
                    value =
                            "2.1";
                    break;

                case "F197":
                    value =
                            "TEST_ENGINE";
                    break;

                default:
                    return "7F 22 31\r>";
            }

            return buildPositiveResponse(
                    did,
                    value
            );
        }

        private String buildPositiveResponse(
                String did,
                String value) {

            byte[] bytes =
                    value.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            StringBuilder result =
                    new StringBuilder();

            result.append(
                    "62 "
            );

            result.append(
                    did.substring(
                            0,
                            2
                    )
            );

            result.append(
                    " "
            );

            result.append(
                    did.substring(
                            2,
                            4
                    )
            );

            for (byte valueByte :
                    bytes) {

                result.append(
                        " "
                );

                result.append(
                        String.format(
                                java.util.Locale.US,
                                "%02X",
                                valueByte & 0xFF
                        )
                );
            }

            result.append(
                    "\r>"
            );

            return result.toString();
        }

        void disableDid(
                String did) {

            disabledDids.add(
                    did.toUpperCase()
            );
        }
    }
}