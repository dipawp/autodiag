package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuCatalogRepositoryTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il caricamento e la ricerca delle ECU dal catalogo
 * diagnostico presente negli assets dell'applicazione.
 *
 * Il test utilizza volutamente una ECU TEST e non una ECU reale,
 * così da verificare solamente il meccanismo di caricamento
 * senza dichiarare compatibilità con un veicolo reale.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuCatalogRepositoryTest {

    /**
     * Verifica che il catalogo ECU possa essere caricato
     * e che contenga almeno la voce TEST definita nel JSON.
     */
    @Test
    public void loadCatalog_containsTestEcu()
            throws Exception {

        Context context =
                ApplicationProvider.getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        List<EcuDefinition> definitions =
                repository.loadAll();

        assertNotNull(
                "Il catalogo ECU non deve essere null.",
                definitions
        );

        assertTrue(
                "Il catalogo ECU deve contenere almeno una ECU.",
                definitions.size() > 0
        );

        EcuDefinition testEcu =
                repository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                "La ECU TEST deve essere trovata nel catalogo.",
                testEcu
        );

        assertEquals(
                "TEST",
                testEcu.getBrand()
        );

        assertEquals(
                "TEST_MODEL",
                testEcu.getModel()
        );

        assertEquals(
                "TEST_ENGINE",
                testEcu.getEngine()
        );

        assertEquals(
                "TEST_ECU",
                testEcu.getEcu()
        );

        assertEquals(
                "CAN",
                testEcu.getProtocol()
        );
    }

    /**
     * Verifica che la ECU TEST contenga i dataset
     * STANDARD_OBD e OEM_PID definiti nel catalogo.
     */
    @Test
    public void testEcu_containsExpectedDatasets()
            throws Exception {

        Context context =
                ApplicationProvider.getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition testEcu =
                repository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                "La ECU TEST deve essere presente.",
                testEcu
        );

        assertNotNull(
                "La lista dataset non deve essere null.",
                testEcu.getDatasets()
        );

        DiagnosticDataset standardDataset =
                testEcu.findDataset(
                        "STANDARD_OBD"
                );

        DiagnosticDataset oemDataset =
                testEcu.findDataset(
                        "OEM_PID"
                );

        assertNotNull(
                "Il dataset STANDARD_OBD deve essere presente.",
                standardDataset
        );

        assertNotNull(
                "Il dataset OEM_PID deve essere presente.",
                oemDataset
        );

        assertTrue(
                "La ECU TEST deve dichiarare "
                        + "la presenza di un dataset OEM.",
                testEcu.hasOemDataset()
        );

        assertEquals(
                "pids/standard/obd2_standard.json",
                standardDataset.getFilePath()
        );

        assertEquals(
                "pids/test/test_dataset.json",
                oemDataset.getFilePath()
        );
    }

    /**
     * Verifica le funzioni di ricerca per marca e modello.
     */
    @Test
    public void searchFunctions_findTestEcu()
            throws Exception {

        Context context =
                ApplicationProvider.getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        List<EcuDefinition> byBrand =
                repository.findByBrand(
                        "test"
                );

        assertEquals(
                1,
                byBrand.size()
        );

        List<EcuDefinition> byModel =
                repository.findByModel(
                        "TEST",
                        "test_model"
                );

        assertEquals(
                1,
                byModel.size()
        );

        List<EcuDefinition> byProtocol =
                repository.findByProtocol(
                        "can"
                );

        assertTrue(
                "La ricerca per protocollo CAN "
                        + "deve trovare almeno una ECU.",
                byProtocol.size() > 0
        );
    }


    /**
     * Verifica il caricamento degli identificativi ECU
     * dal catalogo JSON.
     */
    @Test
    public void loadEcuIdentifiers()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                repository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                ecu
        );

        EcuDefinitionIdentifier identifiers =
                ecu.getIdentifiers();

        assertNotNull(
                identifiers
        );

        assertTrue(
                identifiers.matchesHardware(
                        "EDC17"
                )
        );

        assertTrue(
                identifiers.matchesSoftware(
                        "SW-TEST-001"
                )
        );

        assertTrue(
                identifiers.matchesPartNumber(
                        "PART-TEST-001"
                )
        );

        assertTrue(
                identifiers.matchesSupplier(
                        "BOSCH"
                )
        );

        assertTrue(
                identifiers.matchesVin(
                        "TESTVIN123456"
                )
        );
    }


    /**
     * Verifica che un catalogo privo del blocco identifiers
     * continui a produrre una EcuDefinition valida.
     */
    @Test
    public void missingIdentifiersAreBackwardCompatible()
            throws Exception {

        EcuDefinition definition =
                new EcuDefinition(
                        "TEST",
                        "MODEL",
                        "ENGINE",
                        "ECU",
                        "CAN",
                        "",
                        Collections.emptyList()
                );

        assertNotNull(
                definition.getIdentifiers()
        );

        assertTrue(
                definition.getIdentifiers().isEmpty()
        );
    }



    /**
     * Verifica il caricamento della strategia
     * di identificazione ECU dal catalogo.
     */
    @Test
    public void loadIdentificationDefinitions()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository repository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                repository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                ecu
        );

        assertTrue(
                ecu.hasIdentificationDefinitions()
        );

        List<EcuIdentificationDefinition> definitions =
                ecu.getIdentificationDefinitions();

        assertEquals(
                11,
                definitions.size()
        );

        EcuIdentificationDefinition vin =
                definitions.get(0);

        assertEquals(
                "22",
                vin.getService()
        );

        assertEquals(
                "F190",
                vin.getDid()
        );

        assertEquals(
                "vin",
                vin.getField()
        );

        assertEquals(
                "STRING",
                vin.getDecoder()
        );

        assertEquals(
                "22F190",
                vin.buildRequest()
        );
    }
}