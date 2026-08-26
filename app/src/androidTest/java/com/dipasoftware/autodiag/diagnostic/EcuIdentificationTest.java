package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentificationTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il modello che rappresenta le informazioni
 * identificative lette da una ECU.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuIdentificationTest {

    /**
     * Verifica la costruzione completa dell'identificazione.
     */
    @Test
    public void completeIdentificationStoresValues() {

        Map<String, String> additional =
                new LinkedHashMap<>();

        additional.put(
                "F193",
                "HW-V2"
        );

        additional.put(
                "F196",
                "EURO6"
        );

        EcuIdentification identification =
                new EcuIdentification(
                        "ZFA12345678901234",
                        "552123456",
                        "EDC17C49",
                        "12345678",
                        "AB12",
                        "BOSCH",
                        "SER123456",
                        "SYS-HW-01",
                        "SYS-SW-02",
                        "SYS-V03",
                        "MULTIJET",
                        additional
                );

        assertEquals(
                "ZFA12345678901234",
                identification.getVin()
        );

        assertEquals(
                "552123456",
                identification.getEcuPartNumber()
        );

        assertEquals(
                "EDC17C49",
                identification.getEcuHardwareNumber()
        );

        assertEquals(
                "12345678",
                identification.getEcuSoftwareNumber()
        );

        assertEquals(
                "AB12",
                identification.getEcuSoftwareVersion()
        );

        assertEquals(
                "BOSCH",
                identification.getSupplier()
        );

        assertEquals(
                "SER123456",
                identification.getSerialNumber()
        );

        assertEquals(
                "SYS-HW-01",
                identification.getSupplierHardwareNumber()
        );

        assertEquals(
                "SYS-SW-02",
                identification.getSupplierSoftwareNumber()
        );

        assertEquals(
                "SYS-V03",
                identification.getSupplierSoftwareVersion()
        );

        assertEquals(
                "MULTIJET",
                identification.getSystemName()
        );

        assertEquals(
                "HW-V2",
                identification.getAdditionalIdentifier(
                        "F193"
                )
        );

        assertEquals(
                "EURO6",
                identification.getAdditionalIdentifier(
                        "F196"
                )
        );

        assertTrue(
                identification.hasUsefulIdentification()
        );
    }

    /**
     * Verifica che un'identificazione parziale sia valida.
     */
    @Test
    public void partialIdentificationIsSupported() {

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "EDC17",
                        "SW123",
                        "V1"
                );

        assertFalse(
                identification.hasVin()
        );

        assertTrue(
                identification.hasEcuHardwareNumber()
        );

        assertTrue(
                identification.hasEcuSoftwareNumber()
        );

        assertTrue(
                identification.hasEcuSoftwareVersion()
        );

        assertTrue(
                identification.hasUsefulIdentification()
        );
    }

    /**
     * Verifica che gli identificativi aggiuntivi
     * siano restituiti come mappa immutabile.
     */
    @Test
    public void additionalIdentifiersAreImmutable() {

        Map<String, String> additional =
                new LinkedHashMap<>();

        additional.put(
                "F193",
                "HW-V2"
        );

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        additional
                );

        Map<String, String> result =
                identification
                        .getAdditionalIdentifiers();

        assertNotNull(
                result
        );

        assertEquals(
                "HW-V2",
                result.get(
                        "F193"
                )
        );

        try {

            result.put(
                    "F194",
                    "SW"
            );

            throw new AssertionError(
                    "La mappa deve essere immutabile."
            );

        } catch (
                UnsupportedOperationException expected) {

            // Test superato.
        }
    }
}