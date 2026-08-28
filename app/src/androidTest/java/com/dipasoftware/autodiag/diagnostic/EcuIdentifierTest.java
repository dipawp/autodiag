package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
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
 * Verifica l'identificazione automatica della ECU utilizzando
 * le EcuIdentificationDefinition caricate dal catalogo.
 *
 * Nessun DID è definito direttamente nel test come strategia
 * di identificazione: la strategia viene letta da ecu_catalog.json.
 *
 * Viene utilizzata una Connection simulata.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentifierTest {

    /**
     * Verifica che tutti gli identificatori disponibili
     * vengano letti correttamente dal catalogo.
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

        EcuDefinition ecuDefinition =
                loadTestEcuDefinition();

        EcuIdentification identification =
                identifier.identify(
                        ecuDefinition
                );

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

        /*
         * Verifichiamo anche che le richieste inviate
         * siano esclusivamente quelle dichiarate nel catalogo.
         */
        assertEquals(
                ecuDefinition.getIdentificationDefinitions().size(),
                connection.getSendCount()
        );
    }

    /**
     * Verifica che un DID non disponibile non impedisca
     * la raccolta degli altri identificatori.
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

        EcuDefinition ecuDefinition =
                loadTestEcuDefinition();

        EcuIdentification identification =
                identifier.identify(
                        ecuDefinition
                );

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

        /*
         * Il fatto che F191 non sia disponibile
         * non deve impedire la lettura degli altri DID.
         */
        assertTrue(
                connection.getSendCount() > 1
        );
    }

    /**
     * Carica dal catalogo la ECU TEST.
     *
     * In questo modo il test verifica il percorso reale:
     *
     * ecu_catalog.json
     *      ↓
     * EcuCatalogRepository
     *      ↓
     * EcuDefinition
     *      ↓
     * EcuIdentifier
     */
    private EcuDefinition loadTestEcuDefinition()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition definition =
                repository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        if (definition == null) {

            throw new AssertionError(
                    "ECU TEST non trovata nel catalogo."
            );
        }

        assertTrue(
                definition.hasIdentificationDefinitions()
        );

        return definition;
    }

    /**
     * Connection simulata.
     *
     * Restituisce una risposta differente in base
     * al DID richiesto.
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
         * Ultima richiesta ricevuta.
         */
        @NonNull
        private String lastRequest =
                "";

        /**
         * Numero di richieste inviate.
         */
        private int sendCount =
                0;

        /**
         * Indica se la connection è attiva.
         *
         * @return true.
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
         * Riceve la richiesta.
         *
         * @param data dati.
         */
        @Override
        public void send(
                String data) {

            sendCount++;

            lastRequest =
                    data
                            .replace(
                                    "\r",
                                    ""
                            )
                            .trim()
                            .toUpperCase();
        }

        /**
         * Restituisce la risposta corrispondente
         * all'ultimo DID richiesto.
         *
         * @return risposta.
         */
        @Override
        public String receive() {

            if (!lastRequest.startsWith(
                    "22"
            )) {

                return "";
            }

            if (lastRequest.length() < 6) {

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

        /**
         * Costruisce una risposta UDS positiva.
         *
         * @param did DID.
         * @param value valore ASCII.
         *
         * @return risposta.
         */
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

        /**
         * Disabilita un DID.
         *
         * @param did DID.
         */
        void disableDid(
                @NonNull String did) {

            disabledDids.add(
                    did.trim()
                            .toUpperCase()
            );
        }

        /**
         * Restituisce il numero di richieste inviate.
         *
         * @return numero richieste.
         */
        int getSendCount() {

            return sendCount;
        }
    }
}