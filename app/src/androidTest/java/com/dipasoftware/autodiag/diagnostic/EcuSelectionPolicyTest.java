package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class EcuSelectionPolicyTest {

    @Test
    public void noObservationsProducesNone() {

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.emptyList()
                );

        EcuSelectionPolicy.Result selection =
                new EcuSelectionPolicy().evaluate(result);

        assertTrue(
                selection.getStatus()
                        == EcuSelectionPolicy.Status.NONE
        );

        assertFalse(
                selection.hasSelectedEcu()
        );

        assertFalse(
                selection.hasCandidateForConfirmation()
        );

        assertFalse(
                selection.hasMultipleEcus()
        );

        assertFalse(
                selection.requiresUserConfirmation()
        );
    }

    @Test
    public void singleSafeObservationIsAutoSelected() {

        EcuDiscoveryObservation observation =
                createObservation(
                        createExactMatch()
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.singletonList(observation)
                );

        EcuSelectionPolicy.Result selection =
                new EcuSelectionPolicy().evaluate(result);

        assertTrue(
                selection.isAutoSelected()
        );

        assertTrue(
                selection.hasSelectedEcu()
        );

        assertFalse(
                selection.hasCandidateForConfirmation()
        );

        assertSame(
                observation,
                selection.getObservation()
        );
    }

    @Test
    public void singleUnsafeObservationRequiresConfirmation() {

        EcuDiscoveryObservation observation =
                createObservation(
                        createProbableMatch()
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.singletonList(observation)
                );

        EcuSelectionPolicy.Result selection =
                new EcuSelectionPolicy().evaluate(result);

        assertTrue(
                selection.requiresUserConfirmation()
        );

        assertTrue(
                selection.hasCandidateForConfirmation()
        );

        assertFalse(
                selection.hasSelectedEcu()
        );

        assertSame(
                observation,
                selection.getObservation()
        );
    }

    @Test
    public void multipleObservationsRequireManualSelection() {

        EcuDiscoveryObservation first =
                createObservation(
                        createExactMatch()
                );

        EcuDiscoveryObservation second =
                createObservation(
                        createExactMatch()
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Arrays.asList(
                                first,
                                second
                        )
                );

        EcuSelectionPolicy.Result selection =
                new EcuSelectionPolicy().evaluate(result);

        assertTrue(
                selection.hasMultipleEcus()
        );

        assertFalse(
                selection.hasSelectedEcu()
        );

        assertFalse(
                selection.hasCandidateForConfirmation()
        );

        assertFalse(
                selection.requiresUserConfirmation()
        );

        assertFalse(
                selection.isAutoSelected()
        );

        assertTrue(
                selection.getObservation() == null
        );
    }

    @Test
    public void observationWithoutMatchRequiresConfirmation() {

        EcuDefinition definition =
                createEcuDefinition();

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "HW123",
                        "SW123",
                        "01"
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        definition,
                        identification,
                        null
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.singletonList(observation)
                );

        EcuSelectionPolicy.Result selection =
                new EcuSelectionPolicy().evaluate(result);

        assertTrue(
                selection.requiresUserConfirmation()
        );

        assertTrue(
                selection.hasCandidateForConfirmation()
        );

        assertFalse(
                selection.hasSelectedEcu()
        );

        assertNotNull(
                selection.getObservation()
        );
    }

    private DiagnosticDiscoveryResult createResult(
            java.util.List<EcuDiscoveryObservation> observations) {

        EcuIdentification identification = null;
        EcuMatchResult matchResult = null;

        if (!observations.isEmpty()) {

            EcuDiscoveryObservation observation =
                    observations.get(0);

            identification =
                    observation.getIdentification();

            matchResult =
                    observation.getMatchResult();
        }

        return new DiagnosticDiscoveryResult(
                new VehicleIdentification(""),
                Collections.emptyList(),
                identification,
                matchResult,
                observations
        );
    }

    private EcuDiscoveryObservation createObservation(
            EcuMatchResult matchResult) {

        EcuDefinition definition =
                createEcuDefinition();

        EcuIdentification identification =
                new EcuIdentification(
                        "",
                        "HW123",
                        "SW123",
                        "01"
                );

        return new EcuDiscoveryObservation(
                definition,
                identification,
                matchResult
        );
    }

    private EcuMatchResult createExactMatch() {

        return new EcuMatchResult(
                EcuMatchResult.Status.EXACT,
                createEcuDefinition(),
                100,
                1,
                false,
                0,
                100
        );
    }

    private EcuMatchResult createProbableMatch() {

        return new EcuMatchResult(
                EcuMatchResult.Status.PROBABLE,
                createEcuDefinition(),
                50,
                1,
                false,
                0,
                50
        );
    }

    private EcuDefinition createEcuDefinition() {

        DiagnosticTargetDefinition target =
                new DiagnosticTargetDefinition(
                        "ISO_15765_4",
                        "7E0",
                        "7E8",
                        "NORMAL",
                        11,
                        500
                );

        EcuIdentificationDefinition identification =
                new EcuIdentificationDefinition(
                        "F190",
                        "vin",
                        "ascii",
                        false
                );

        return new EcuDefinition(
                "TEST",
                "MODEL",
                "ENGINE",
                "ECU",
                "ISO_15765_4",
                "test.xml",
                new EcuDefinitionIdentifier(),
                Collections.singletonList(
                        identification
                ),
                target,
                Collections.emptyList()
        );
    }
}