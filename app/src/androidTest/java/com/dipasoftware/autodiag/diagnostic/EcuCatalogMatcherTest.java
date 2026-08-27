package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuCatalogMatcherTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuCatalogMatcherTest {

    /**
     * Verifica un match ESATTO ottenuto tramite
     * hardware + software.
     *
     * 50 + 30 = 80
     */
    @Test
    public void hardwareAndSoftwareProduceExactMatch() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17C49"
                        ),
                        Collections.singletonList(
                                "SW-001"
                        ),
                        Collections.emptyList(),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.emptyList()
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "Test Model",
                        "1.6",
                        "Bosch EDC17C49",
                        "CAN",
                        "",
                        identifiers,
                        Collections.emptyList()
                );

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "",
                        "EDC17C49",
                        "SW-001",
                        "",
                        "BOSCH",
                        "",
                        "",
                        "",
                        "",
                        "",
                        new LinkedHashMap<>()
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        EcuMatchResult result =
                matcher.match(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isExact()
        );

        assertEquals(
                80,
                result.getScore()
        );

        assertEquals(
                definition,
                result.getEcuDefinition()
        );
    }

    /**
     * Verifica un match ESATTO ottenuto tramite
     * hardware + part number.
     *
     * 50 + 20 + 10 supplier = 80
     */
    @Test
    public void hardwareAndPartNumberProduceExactMatch() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17C49"
                        ),
                        Collections.emptyList(),
                        Collections.singletonList(
                                "PART-001"
                        ),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.emptyList()
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "Test Model",
                        "1.6",
                        "Bosch EDC17C49",
                        "CAN",
                        "",
                        identifiers,
                        Collections.emptyList()
                );

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "PART-001",
                        "EDC17C49",
                        "",
                        "",
                        "BOSCH",
                        "",
                        "",
                        "",
                        "",
                        "",
                        new LinkedHashMap<>()
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        EcuMatchResult result =
                matcher.match(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isExact()
        );

        assertEquals(
                80,
                result.getScore()
        );

        assertEquals(
                definition,
                result.getEcuDefinition()
        );
    }

    /**
     * Verifica un match PROBABILE basato solamente
     * sull'hardware number.
     *
     * 50 punti.
     */
    @Test
    public void hardwareOnlyProducesProbableMatch() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17C49"
                        ),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "Test Model",
                        "1.6",
                        "Bosch EDC17C49",
                        "CAN",
                        "",
                        identifiers,
                        Collections.emptyList()
                );

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "",
                        "EDC17C49",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        new LinkedHashMap<>()
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        EcuMatchResult result =
                matcher.match(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isProbable()
        );

        assertEquals(
                50,
                result.getScore()
        );

        assertEquals(
                definition,
                result.getEcuDefinition()
        );
    }

    /**
     * Verifica che supplier + VIN non possano identificare
     * da soli una ECU.
     */
    @Test
    public void supplierAndVinAloneAreNotExact() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.singletonList(
                                "ZFA"
                        )
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "Test Model",
                        "1.6",
                        "Bosch ECU",
                        "CAN",
                        "",
                        identifiers,
                        Collections.emptyList()
                );

        EcuIdentification identification =
                new EcuIdentification(
                        "ZFA123456789",
                        "",
                        "",
                        "",
                        "",
                        "BOSCH",
                        "",
                        "",
                        "",
                        "",
                        "",
                        new LinkedHashMap<>()
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        EcuMatchResult result =
                matcher.match(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isNone()
        );

        assertEquals(
                20,
                result.getScore()
        );
    }

    /**
     * Verifica che una ECU completamente diversa
     * non venga selezionata.
     */
    @Test
    public void differentIdentifiersProduceNoMatch() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC16C39"
                        ),
                        Collections.singletonList(
                                "SW-OLD"
                        ),
                        Collections.singletonList(
                                "PART-OLD"
                        ),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.emptyList()
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "Test Model",
                        "1.9",
                        "Bosch EDC16C39",
                        "CAN",
                        "",
                        identifiers,
                        Collections.emptyList()
                );

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "PART-NEW",
                        "EDC17C49",
                        "SW-NEW",
                        "",
                        "MARELLI",
                        "",
                        "",
                        "",
                        "",
                        "",
                        new LinkedHashMap<>()
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        EcuMatchResult result =
                matcher.match(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isNone()
        );

        assertEquals(
                0,
                result.getScore()
        );

        assertFalse(
                result.isProbable()
        );

        assertFalse(
                result.isExact()
        );

        assertEquals(
                null,
                result.getEcuDefinition()
        );
    }
}