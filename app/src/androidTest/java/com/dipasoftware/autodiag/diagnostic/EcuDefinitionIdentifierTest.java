package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuDefinitionIdentifierTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuDefinitionIdentifierTest {

    /**
     * Verifica la normalizzazione degli identificativi.
     */
    @Test
    public void identifiersAreNormalized() {

        EcuDefinitionIdentifier identifier =
                new EcuDefinitionIdentifier(
                        Arrays.asList(
                                " edc17c49 ",
                                "EDC17C49"
                        ),
                        Arrays.asList(
                                " sw123 "
                        ),
                        Arrays.asList(
                                " 552123456 "
                        ),
                        Arrays.asList(
                                " bosch "
                        ),
                        Arrays.asList(
                                "ZFA123"
                        )
                );

        assertEquals(
                1,
                identifier.getHardwareNumbers().size()
        );

        assertEquals(
                "EDC17C49",
                identifier.getHardwareNumbers().get(0)
        );

        assertEquals(
                "SW123",
                identifier.getSoftwareNumbers().get(0)
        );

        assertEquals(
                "552123456",
                identifier.getPartNumbers().get(0)
        );

        assertEquals(
                "BOSCH",
                identifier.getSuppliers().get(0)
        );

        assertTrue(
                identifier.matchesHardware(
                        "edc17c49"
                )
        );

        assertTrue(
                identifier.matchesSoftware(
                        "SW123"
                )
        );

        assertTrue(
                identifier.matchesPartNumber(
                        "552123456"
                )
        );

        assertTrue(
                identifier.matchesSupplier(
                        "bosch"
                )
        );
    }

    /**
     * Verifica il matching VIN.
     */
    @Test
    public void vinPatternMatches() {

        EcuDefinitionIdentifier identifier =
                new EcuDefinitionIdentifier(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Arrays.asList(
                                "ZFA123",
                                "ZFA456"
                        )
                );

        assertTrue(
                identifier.matchesVin(
                        "ZFA123456789"
                )
        );

        assertFalse(
                identifier.matchesVin(
                        "VF123456789"
                )
        );
    }

    /**
     * Verifica il comportamento con identificatori vuoti.
     */
    @Test
    public void emptyIdentifierIsSafe() {

        EcuDefinitionIdentifier identifier =
                new EcuDefinitionIdentifier();

        assertTrue(
                identifier.isEmpty()
        );

        assertFalse(
                identifier.matchesHardware(
                        "EDC17"
                )
        );

        assertFalse(
                identifier.matchesSoftware(
                        "SW"
                )
        );

        assertFalse(
                identifier.matchesPartNumber(
                        "123"
                )
        );

        assertFalse(
                identifier.matchesSupplier(
                        "BOSCH"
                )
        );

        assertFalse(
                identifier.matchesVin(
                        "ZFA123"
                )
        );
    }

    /**
     * Verifica che le liste siano immutabili.
     */
    @Test
    public void listsAreImmutable() {

        EcuDefinitionIdentifier identifier =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17"
                        ),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        try {

            identifier.getHardwareNumbers().add(
                    "EDC18"
            );

            throw new AssertionError(
                    "La lista deve essere immutabile."
            );

        } catch (
                UnsupportedOperationException expected) {

            // Test superato.
        }
    }
}