package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDatasetRepositoryTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il caricamento di un dataset PID dagli assets
 * utilizzando il percorso contenuto in DiagnosticDataset.
 *
 * Il test utilizza il dataset OEM TEST.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticDatasetRepositoryTest {

    /**
     * Verifica che il dataset OEM TEST possa essere
     * caricato e che contenga il PID dichiarato.
     */
    @Test
    public void loadTestDataset()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository ecuRepository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                ecuRepository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                "La ECU TEST deve esistere.",
                ecu
        );

        DiagnosticDataset dataset =
                ecu.findDataset(
                        "OEM_PID"
                );

        assertNotNull(
                "Il dataset OEM deve esistere.",
                dataset
        );

        DiagnosticDatasetRepository repository =
                new DiagnosticDatasetRepository(
                        context
                );

        PidDatasetDefinition definition =
                repository.load(
                        dataset
                );

        assertNotNull(
                "Il dataset caricato non deve essere null.",
                definition
        );

        assertEquals(
                "OEM",
                definition.getSource()
        );

        assertEquals(
                "TEST",
                definition.getManufacturer()
        );

        assertEquals(
                "TEST",
                definition.getBrand()
        );

        assertEquals(
                "TEST_MODEL",
                definition.getModel()
        );

        assertEquals(
                "TEST_ECU",
                definition.getEcu()
        );

        assertTrue(
                "Il dataset deve contenere almeno un PID.",
                definition.getPids().size() > 0
        );
    }

    /**
     * Verifica che il PID di test possa essere trovato
     * tramite il repository.
     */
    @Test
    public void findTestPid()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository ecuRepository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                ecuRepository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                ecu
        );

        DiagnosticDataset dataset =
                ecu.findDataset(
                        "OEM_PID"
                );

        assertNotNull(
                dataset
        );

        DiagnosticDatasetRepository repository =
                new DiagnosticDatasetRepository(
                        context
                );

        PidDefinition definition =
                repository.findPid(
                        dataset,
                        "22TEST"
                );

        assertNotNull(
                "Il PID 22TEST deve essere presente.",
                definition
        );

        assertEquals(
                "OEM",
                definition.getSource()
        );

        assertEquals(
                "FORMULA",
                definition.getDecoder()
        );

        assertEquals(
                "A*100/255",
                definition.getFormula()
        );
    }



    /**
     * Verifica il caricamento diretto del dataset OEM
     * partendo dalla EcuDefinition.
     */
    @Test
    public void loadOemDatasetFromEcu()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository ecuRepository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                ecuRepository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                ecu
        );

        DiagnosticDatasetRepository repository =
                new DiagnosticDatasetRepository(
                        context
                );

        PidDatasetDefinition dataset =
                repository.loadOem(
                        ecu
                );

        assertNotNull(
                dataset
        );

        assertEquals(
                "OEM",
                dataset.getSource()
        );

        assertEquals(
                "TEST_MODEL",
                dataset.getModel()
        );
    }


    /**
     * Verifica la ricerca di un PID OEM direttamente
     * attraverso EcuDefinition.
     */
    @Test
    public void findOemPidFromEcu()
            throws Exception {

        Context context =
                ApplicationProvider
                        .getApplicationContext();

        EcuCatalogRepository ecuRepository =
                new EcuCatalogRepository(
                        context
                );

        EcuDefinition ecu =
                ecuRepository.find(
                        "TEST",
                        "TEST_MODEL",
                        "TEST_ENGINE",
                        "TEST_ECU"
                );

        assertNotNull(
                ecu
        );

        DiagnosticDatasetRepository repository =
                new DiagnosticDatasetRepository(
                        context
                );

        PidDefinition pid =
                repository.findPid(
                        ecu,
                        "OEM_PID",
                        "22TEST"
                );

        assertNotNull(
                pid
        );

        assertEquals(
                "OEM",
                pid.getSource()
        );

        assertEquals(
                "22TEST",
                pid.getPid()
        );
    }
}