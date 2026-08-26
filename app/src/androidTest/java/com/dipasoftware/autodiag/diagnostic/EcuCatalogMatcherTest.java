package com.dipasoftware.autodiag.diagnostic;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
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
     * Verifica che una ECU non corrispondente
     * non venga selezionata arbitrariamente.
     */
    @Test
    public void noMatchWhenIdentifiersDoNotMatch() {

        EcuIdentification identification =
                new EcuIdentification(
                        "TESTVIN",
                        "EDC17",
                        "UNKNOWN",
                        "UNKNOWN"
                );

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "TEST_MODEL",
                        "1.6",
                        "Bosch EDC16C39",
                        "CAN",
                        "",
                        Collections.emptyList()
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
    }

    /**
     * Verifica un match basato sull'identificativo ECU
     * presente nel nome della EcuDefinition.
     */
    @Test
    public void hardwareIdentifierCanProduceMatch() {

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "",
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

        EcuDefinition definition =
                new EcuDefinition(
                        "Fiat",
                        "TEST_MODEL",
                        "1.6",
                        "Bosch EDC17C49",
                        "CAN",
                        "",
                        Collections.emptyList()
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
                        ||
                        result.isExact()
        );

        assertEquals(
                definition,
                result.getEcuDefinition()
        );
    }
}