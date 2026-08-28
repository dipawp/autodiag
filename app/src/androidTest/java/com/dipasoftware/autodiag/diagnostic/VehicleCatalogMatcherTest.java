package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: VehicleCatalogMatcherTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class VehicleCatalogMatcherTest {

    /**
     * Verifica che un VIN compatibile trovi la ECU candidate.
     */
    @Test
    public void vinFindsCandidate() {

        EcuDefinition definition =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        VehicleIdentification identification =
                new VehicleIdentification(
                        "TESTVIN123456789"
                );

        VehicleCatalogMatcher matcher =
                new VehicleCatalogMatcher();

        java.util.List<EcuDefinition> result =
                matcher.findCandidates(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertEquals(
                1,
                result.size()
        );

        assertSame(
                definition,
                result.get(0)
        );
    }

    /**
     * Verifica che un VIN non compatibile non trovi candidate.
     */
    @Test
    public void differentVinProducesNoCandidate() {

        EcuDefinition definition =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        VehicleIdentification identification =
                new VehicleIdentification(
                        "VFOTHER123456789"
                );

        VehicleCatalogMatcher matcher =
                new VehicleCatalogMatcher();

        java.util.List<EcuDefinition> result =
                matcher.findCandidates(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isEmpty()
        );
    }

    /**
     * Verifica che senza VIN non venga eseguita
     * una selezione automatica.
     */
    @Test
    public void missingVinProducesNoCandidate() {

        EcuDefinition definition =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        VehicleIdentification identification =
                new VehicleIdentification(
                        ""
                );

        VehicleCatalogMatcher matcher =
                new VehicleCatalogMatcher();

        java.util.List<EcuDefinition> result =
                matcher.findCandidates(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                );

        assertTrue(
                result.isEmpty()
        );
    }

    /**
     * Verifica che due ECU compatibili non vengano
     * trasformate arbitrariamente in una sola ECU.
     */
    @Test
    public void multipleCandidatesArePreserved() {

        EcuDefinition first =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        EcuDefinition second =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        VehicleIdentification identification =
                new VehicleIdentification(
                        "TESTVIN123456789"
                );

        VehicleCatalogMatcher matcher =
                new VehicleCatalogMatcher();

        java.util.List<EcuDefinition> result =
                matcher.findCandidates(
                        identification,
                        Arrays.asList(
                                first,
                                second
                        )
                );

        assertEquals(
                2,
                result.size()
        );

        assertSame(
                first,
                result.get(0)
        );

        assertSame(
                second,
                result.get(1)
        );

        assertNull(
                matcher.findUniqueCandidate(
                        identification,
                        Arrays.asList(
                                first,
                                second
                        )
                )
        );

        assertTrue(
                !matcher.hasUniqueCandidate(
                        identification,
                        Arrays.asList(
                                first,
                                second
                        )
                )
        );
    }

    /**
     * Verifica il caso in cui esiste una sola candidate.
     */
    @Test
    public void uniqueCandidateCanBeReturned() {

        EcuDefinition definition =
                createDefinition(
                        new EcuDefinitionIdentifier(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.singletonList(
                                        "TESTVIN"
                                )
                        )
                );

        VehicleIdentification identification =
                new VehicleIdentification(
                        "TESTVIN123456789"
                );

        VehicleCatalogMatcher matcher =
                new VehicleCatalogMatcher();

        assertTrue(
                matcher.hasUniqueCandidate(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                )
        );

        assertSame(
                definition,
                matcher.findUniqueCandidate(
                        identification,
                        Collections.singletonList(
                                definition
                        )
                )
        );
    }

    /**
     * Crea una definizione ECU di test.
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
}