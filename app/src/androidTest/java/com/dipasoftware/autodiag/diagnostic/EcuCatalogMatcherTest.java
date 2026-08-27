package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
     * Verifica un match esatto hardware + software.
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
                createDefinition(
                        identifiers
                );

        EcuIdentification identification =
                createIdentification(
                        "",
                        "",
                        "EDC17C49",
                        "SW-001",
                        "",
                        "BOSCH"
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
                90,
                result.getScore()
        );

        assertEquals(
                definition,
                result.getEcuDefinition()
        );
    }

    /**
     * Verifica che i candidati siano ordinati
     * dal punteggio più alto al più basso.
     */
    @Test
    public void candidatesAreSortedByScore() {

        EcuDefinitionIdentifier weak =
                new EcuDefinitionIdentifier(
                        Collections.emptyList(),
                        Collections.singletonList(
                                "SW-001"
                        ),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        EcuDefinitionIdentifier strong =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17C49"
                        ),
                        Collections.singletonList(
                                "SW-001"
                        ),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        EcuDefinition weakDefinition =
                createDefinition(
                        weak
                );

        EcuDefinition strongDefinition =
                createDefinition(
                        strong
                );

        EcuIdentification identification =
                createIdentification(
                        "",
                        "",
                        "EDC17C49",
                        "SW-001",
                        "",
                        ""
                );

        EcuCatalogMatcher matcher =
                new EcuCatalogMatcher();

        List<EcuMatchCandidate> candidates =
                matcher.findCandidates(
                        identification,
                        Arrays.asList(
                                weakDefinition,
                                strongDefinition
                        )
                );

        assertEquals(
                2,
                candidates.size()
        );

        assertEquals(
                strongDefinition,
                candidates.get(0)
                        .getEcuDefinition()
        );

        assertEquals(
                80,
                candidates.get(0)
                        .getScore()
        );

        assertEquals(
                30,
                candidates.get(1)
                        .getScore()
        );
    }

    /**
     * Verifica che supplier + VIN da soli non possano
     * identificare esattamente una ECU.
     */
    @Test
    public void supplierAndVinCannotProduceExactMatch() {

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
                createDefinition(
                        identifiers
                );

        EcuIdentification identification =
                createIdentification(
                        "ZFA123456",
                        "",
                        "",
                        "",
                        "",
                        "BOSCH"
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

        assertFalse(
                result.isExact()
        );
    }

    /**
     * Verifica che hardware + supplier possano produrre
     * un match esatto quando il punteggio raggiunge la soglia.
     */
    @Test
    public void hardwareAndSupplierProduceExactMatch() {

        EcuDefinitionIdentifier identifiers =
                new EcuDefinitionIdentifier(
                        Collections.singletonList(
                                "EDC17C49"
                        ),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.singletonList(
                                "BOSCH"
                        ),
                        Collections.emptyList()
                );

        EcuDefinition definition =
                createDefinition(
                        identifiers
                );

        EcuIdentification identification =
                createIdentification(
                        "",
                        "",
                        "EDC17C49",
                        "",
                        "",
                        "BOSCH"
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
                60,
                result.getScore()
        );

        assertFalse(
                result.isExact()
        );
    }

    /**
     * Verifica nessuna corrispondenza.
     */
    @Test
    public void unrelatedIdentifiersProduceNoMatch() {

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
                createDefinition(
                        identifiers
                );

        EcuIdentification identification =
                createIdentification(
                        "",
                        "PART-NEW",
                        "EDC17C49",
                        "SW-NEW",
                        "",
                        "MARELLI"
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

        assertEquals(
                null,
                result.getEcuDefinition()
        );
    }

    /**
     * Crea una ECU di test.
     */
    private EcuDefinition createDefinition(
            EcuDefinitionIdentifier identifiers) {

        return new EcuDefinition(
                "TEST",
                "TEST_MODEL",
                "TEST_ENGINE",
                "TEST_ECU",
                "CAN",
                "",
                identifiers,
                Collections.emptyList()
        );
    }

    /**
     * Crea un'identificazione di test.
     */
    private EcuIdentification createIdentification(
            String vin,
            String partNumber,
            String hardwareNumber,
            String softwareNumber,
            String softwareVersion,
            String supplier) {

        return new EcuIdentification(
                vin,
                partNumber,
                hardwareNumber,
                softwareNumber,
                softwareVersion,
                supplier,
                "",
                "",
                "",
                "",
                "",
                new LinkedHashMap<>()
        );
    }
}