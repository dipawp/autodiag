package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuSelectionPolicyTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica le regole di selezione ECU:
 *
 * - nessuna ECU;
 * - una ECU con match EXACT sicuro;
 * - una ECU con match non sicuro;
 * - più ECU;
 * - verifica che una sola ECU sicura venga restituita dalla policy.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class EcuSelectionPolicyTest {

    @Test
    public void noObservationsProducesNone() {

        DiagnosticDiscoveryResult discovery =
                createResult(
                        Collections.emptyList()
                );

        EcuSelectionPolicy policy =
                new EcuSelectionPolicy();

        EcuSelectionPolicy.Result result =
                policy.evaluate(
                        discovery
                );

        assertEquals(
                EcuSelectionPolicy.Status.NONE,
                result.getStatus()
        );

        assertFalse(
                result.hasSelection()
        );
    }

    @Test
    public void singleSafeObservationIsAutoSelected() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuMatchResult match =
                new EcuMatchResult(
                        EcuMatchResult.Status.EXACT,
                        ecu,
                        100
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        ecu,
                        identification,
                        match
                );

        DiagnosticDiscoveryResult discovery =
                createResult(
                        Collections.singletonList(
                                observation
                        )
                );

        EcuSelectionPolicy policy =
                new EcuSelectionPolicy();

        EcuSelectionPolicy.Result result =
                policy.evaluate(
                        discovery
                );

        assertEquals(
                EcuSelectionPolicy.Status.AUTO_SELECTED,
                result.getStatus()
        );

        assertTrue(
                result.isAutoSelected()
        );

        assertSame(
                observation,
                result.getObservation()
        );
    }

    @Test
    public void singleUnsafeObservationRequiresConfirmation() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuMatchResult match =
                new EcuMatchResult(
                        EcuMatchResult.Status.PROBABLE,
                        ecu,
                        50
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        ecu,
                        identification,
                        match
                );

        DiagnosticDiscoveryResult discovery =
                createResult(
                        Collections.singletonList(
                                observation
                        )
                );

        EcuSelectionPolicy policy =
                new EcuSelectionPolicy();

        EcuSelectionPolicy.Result result =
                policy.evaluate(
                        discovery
                );

        assertEquals(
                EcuSelectionPolicy.Status.CONFIRMATION_REQUIRED,
                result.getStatus()
        );

        assertTrue(
                result.requiresUserConfirmation()
        );

        assertSame(
                observation,
                result.getObservation()
        );
    }

    @Test
    public void multipleObservationsRequireManualSelection() {

        EcuDefinition ecuA =
                createEcu(
                        "ECU_A"
                );

        EcuDefinition ecuB =
                createEcu(
                        "ECU_B"
                );

        EcuIdentification identificationA =
                createIdentification(
                        "VIN_A"
                );

        EcuIdentification identificationB =
                createIdentification(
                        "VIN_B"
                );

        EcuMatchResult matchA =
                new EcuMatchResult(
                        EcuMatchResult.Status.EXACT,
                        ecuA,
                        100
                );

        EcuMatchResult matchB =
                new EcuMatchResult(
                        EcuMatchResult.Status.EXACT,
                        ecuB,
                        100
                );

        EcuDiscoveryObservation observationA =
                new EcuDiscoveryObservation(
                        ecuA,
                        identificationA,
                        matchA
                );

        EcuDiscoveryObservation observationB =
                new EcuDiscoveryObservation(
                        ecuB,
                        identificationB,
                        matchB
                );

        DiagnosticDiscoveryResult discovery =
                createResult(
                        Arrays.asList(
                                observationA,
                                observationB
                        )
                );

        EcuSelectionPolicy policy =
                new EcuSelectionPolicy();

        EcuSelectionPolicy.Result result =
                policy.evaluate(
                        discovery
                );

        assertEquals(
                EcuSelectionPolicy.Status.MULTIPLE_ECUS,
                result.getStatus()
        );

        assertTrue(
                result.hasMultipleEcus()
        );

        assertFalse(
                result.hasSelection()
        );

        assertFalse(
                result.isAutoSelected()
        );
    }

    @Test
    public void observationWithoutMatchRequiresConfirmation() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        ecu,
                        identification,
                        null
                );

        DiagnosticDiscoveryResult discovery =
                createResult(
                        Collections.singletonList(
                                observation
                        )
                );

        EcuSelectionPolicy policy =
                new EcuSelectionPolicy();

        EcuSelectionPolicy.Result result =
                policy.evaluate(
                        discovery
                );

        assertEquals(
                EcuSelectionPolicy.Status.CONFIRMATION_REQUIRED,
                result.getStatus()
        );

        assertSame(
                observation,
                result.getObservation()
        );
    }

    @NonNull
    private DiagnosticDiscoveryResult createResult(
            @NonNull java.util.List<EcuDiscoveryObservation> observations) {

        return new DiagnosticDiscoveryResult(
                new VehicleIdentification(""),
                Collections.emptyList(),
                observations.isEmpty()
                        ? null
                        : observations.get(0).getIdentification(),
                observations.isEmpty()
                        ? null
                        : observations.get(0).getMatchResult(),
                observations
        );
    }

    @NonNull
    private EcuIdentification createIdentification(
            @NonNull String vin) {

        return new EcuIdentification(
                vin,
                "",
                "",
                ""
        );
    }

    @NonNull
    private EcuDefinition createEcu(
            @NonNull String name) {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "CAN",
                        "7E0",
                        "7E8",
                        "PHYSICAL",
                        11,
                        500
                );

        EcuIdentificationDefinition identificationDefinition =
                new EcuIdentificationDefinition(
                        "22",
                        "F190",
                        "vin",
                        "STRING",
                        false,
                        0,
                        0
                );

        return new EcuDefinition(
                "TEST",
                "SELECTION",
                "TEST_ENGINE",
                name,
                "CAN",
                "",
                new EcuDefinitionIdentifier(),
                Collections.singletonList(
                        identificationDefinition
                ),
                target,
                Collections.emptyList()
        );
    }
}