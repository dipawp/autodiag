package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dipasoftware.autodiag.connection.Connection;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * Verifica l'identificazione ECU utilizzando le definizioni
 * presenti nel catalogo e il target dichiarato dalla EcuDefinition.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentifierTest {

    /**
     * Verifica che i valori identificativi vengano letti
     * secondo le definizioni del catalogo.
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

        EcuDefinition definition =
                createTestDefinition();

        EcuIdentification identification =
                identifier.identify(
                        definition
                );

        assertEquals(
                "TESTVIN1234567890",
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
     * Verifica che un DID non disponibile non interrompa
     * l'intera identificazione.
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

        EcuDefinition definition =
                createTestDefinition();

        EcuIdentification identification =
                identifier.identify(
                        definition
                );

        assertEquals(
                "",
                identification.getEcuHardwareNumber()
        );

        assertEquals(
                "TESTVIN1234567890",
                identification.getVin()
        );

        assertEquals(
                "ECU-SW-001",
                identification.getEcuSoftwareNumber()
        );
    }

    /**
     * Verifica che EcuIdentifier utilizzi il target
     * definito dalla EcuDefinition.
     */
    @Test
    public void identificationUsesEcuDefinitionTarget()
            throws Exception {

        FakeDiagnosticTransport transport =
                new FakeDiagnosticTransport();

        DiagnosticPidExecutor executor =
                new DiagnosticPidExecutor(
                        transport
                );

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "UDS",
                        "18DAF110",
                        "18DA10F1",
                        "PHYSICAL",
                        29
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU",
                        "CAN",
                        "",
                        new EcuDefinitionIdentifier(),
                        Collections.singletonList(
                                new EcuIdentificationDefinition(
                                        "F190",
                                        "vin",
                                        "STRING",
                                        false
                                )
                        ),
                        target,
                        Collections.emptyList()
                );

        EcuIdentifier identifier =
                new EcuIdentifier(
                        executor
                );

        identifier.identify(
                definition
        );

        assertNotNull(
                transport.getLastTarget()
        );

        assertEquals(
                "18DAF110",
                transport
                        .getLastTarget()
                        .getRequestId()
        );

        assertEquals(
                "18DA10F1",
                transport
                        .getLastTarget()
                        .getResponseId()
        );

        assertEquals(
                29,
                transport
                        .getLastTarget()
                        .getCanIdBits()
        );
    }

    /**
     * Crea la EcuDefinition TEST.
     *
     * Utilizza esattamente la struttura presente
     * nel catalogo di test.
     */
    @NonNull
    private EcuDefinition createTestDefinition() {

        return new EcuDefinition(
                "TEST",
                "TEST_MODEL",
                "TEST_ENGINE",
                "TEST_ECU",
                "CAN",
                "",
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "ECU-HW-001"
                        ),
                        Collections.singletonList(
                                "ECU-SW-001"
                        ),
                        Collections.singletonList(
                                "ECU-PART-001"
                        ),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.singletonList(
                                "TESTVIN"
                        )
                ),
                createIdentificationDefinitions(),
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11
                ),
                Collections.emptyList()
        );
    }

    /**
     * Crea le definizioni di identificazione TEST.
     *
     * Queste corrispondono alle voci presenti
     * nel catalogo ECU di test.
     */
    @NonNull
    private java.util.List<EcuIdentificationDefinition>
    createIdentificationDefinitions() {

        java.util.List<EcuIdentificationDefinition> definitions =
                new java.util.ArrayList<>();

        definitions.add(
                new EcuIdentificationDefinition(
                        "F190",
                        "vin",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F187",
                        "ecuPartNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F188",
                        "ecuSoftwareNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F189",
                        "ecuSoftwareVersion",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F191",
                        "ecuHardwareNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F18A",
                        "supplier",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F18C",
                        "serialNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F192",
                        "supplierHardwareNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F194",
                        "supplierSoftwareNumber",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F195",
                        "supplierSoftwareVersion",
                        "STRING",
                        false
                )
        );

        definitions.add(
                new EcuIdentificationDefinition(
                        "F197",
                        "systemName",
                        "STRING",
                        false
                )
        );

        return definitions;
    }

    /**
     * Connection simulata.
     *
     * Utilizzata dai test legacy che verificano
     * l'identificazione tramite Connection.
     */
    private static class FakeIdentificationConnection
            implements Connection {

        /**
         * DID disabilitati.
         */
        @NonNull
        private final java.util.Set<String> disabledDids =
                new java.util.HashSet<>();

        /**
         * Ultima request.
         */
        @NonNull
        private String lastRequest =
                "";

        @Override
        public boolean isConnected() {

            return true;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
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

            switch (
                    did
            ) {

                case "F190":
                    value =
                            "TESTVIN1234567890";
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

        @NonNull
        private String buildPositiveResponse(
                @NonNull String did,
                @NonNull String value) {

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

            for (
                    byte valueByte :
                    bytes
            ) {

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
                @NonNull String did) {

            disabledDids.add(
                    did.trim()
                            .toUpperCase()
            );
        }
    }

    /**
     * Transport simulato per verificare il passaggio
     * del target dalla EcuDefinition al transport.
     */
    private static class FakeDiagnosticTransport
            implements DiagnosticTransport {

        /**
         * Ultimo target ricevuto.
         */
        private DiagnosticTargetDefinition lastTarget;

        /**
         * Ultima request ricevuta.
         */
        @NonNull
        private String lastRequest =
                "";

        @Override
        public void send(
                @NonNull DiagnosticTargetDefinition target,
                @NonNull String request) {

            lastTarget =
                    target;

            lastRequest =
                    request;
        }

        @Override
        @NonNull
        public String receive(
                @NonNull DiagnosticTargetDefinition target) {

            lastTarget =
                    target;

            /*
             * Risposta minima valida per F190.
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

            return "7F 22 31\r>";
        }

        /**
         * Restituisce l'ultimo target ricevuto.
         *
         * Questo è il metodo richiesto dal test.
         *
         * @return target oppure null.
         */
        DiagnosticTargetDefinition getLastTarget() {

            return lastTarget;
        }

        /**
         * Restituisce l'ultima request.
         *
         * @return request.
         */
        @NonNull
        String getLastRequest() {

            return lastRequest;
        }
    }
}