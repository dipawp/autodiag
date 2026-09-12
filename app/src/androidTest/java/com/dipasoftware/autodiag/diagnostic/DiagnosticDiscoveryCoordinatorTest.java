package com.dipasoftware.autodiag.diagnostic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoveryCoordinatorTest {

    @Test
    public void coordinatorRunsDiscoveryAndReturnsAutoSelection() {

        EcuDiscoveryObservation observation =
                createObservation(
                        createExactMatch()
                );

        DiagnosticDiscoveryResult discoveryResult =
                createResult(
                        Collections.singletonList(observation)
                );

        DiagnosticDiscoveryCoordinator.DiscoveryRunner runner =
                new DiagnosticDiscoveryCoordinator.DiscoveryRunner() {
                    @Override
                    public DiagnosticDiscoveryResult discover() {
                        return discoveryResult;
                    }
                };

        DiagnosticDiscoveryCoordinator coordinator =
                new DiagnosticDiscoveryCoordinator(
                        runner,
                        new EcuSelectionPolicy()
                );

        DiagnosticDiscoveryCoordinator.Result result =
                coordinator.discover();

        assertNotNull(result);

        assertSame(
                discoveryResult,
                result.getDiscoveryResult()
        );

        assertEquals(
                EcuSelectionPolicy.Status.AUTO_SELECTED,
                result.getSelectionResult().getStatus()
        );

        assertTrue(
                result.isAutoSelected()
        );

        assertTrue(
                result.hasSelectedEcu()
        );
    }

    @Test
    public void coordinatorPropagatesConfirmationRequired() {

        EcuDiscoveryObservation observation =
                createObservation(
                        createProbableMatch()
                );

        DiagnosticDiscoveryResult discoveryResult =
                createResult(
                        Collections.singletonList(observation)
                );

        DiagnosticDiscoveryCoordinator.DiscoveryRunner runner =
                new DiagnosticDiscoveryCoordinator.DiscoveryRunner() {
                    @Override
                    public DiagnosticDiscoveryResult discover() {
                        return discoveryResult;
                    }
                };

        DiagnosticDiscoveryCoordinator coordinator =
                new DiagnosticDiscoveryCoordinator(
                        runner,
                        new EcuSelectionPolicy()
                );

        DiagnosticDiscoveryCoordinator.Result result =
                coordinator.discover();

        assertEquals(
                EcuSelectionPolicy.Status.CONFIRMATION_REQUIRED,
                result.getSelectionResult().getStatus()
        );

        assertTrue(
                result.requiresUserConfirmation()
        );

        assertTrue(
                result.hasCandidateForConfirmation()
        );
    }

    @Test
    public void coordinatorPropagatesMultipleEcus() {

        EcuDiscoveryObservation first =
                createObservation(
                        createExactMatch()
                );

        EcuDiscoveryObservation second =
                createObservation(
                        createExactMatch()
                );

        DiagnosticDiscoveryResult discoveryResult =
                createResult(
                        Arrays.asList(
                                first,
                                second
                        )
                );

        DiagnosticDiscoveryCoordinator.DiscoveryRunner runner =
                new DiagnosticDiscoveryCoordinator.DiscoveryRunner() {
                    @Override
                    public DiagnosticDiscoveryResult discover() {
                        return discoveryResult;
                    }
                };

        DiagnosticDiscoveryCoordinator coordinator =
                new DiagnosticDiscoveryCoordinator(
                        runner,
                        new EcuSelectionPolicy()
                );

        DiagnosticDiscoveryCoordinator.Result result =
                coordinator.discover();

        assertEquals(
                EcuSelectionPolicy.Status.MULTIPLE_ECUS,
                result.getSelectionResult().getStatus()
        );

        assertTrue(
                result.hasMultipleEcus()
        );
    }

    @Test
    public void coordinatorPropagatesNoEcu() {

        DiagnosticDiscoveryResult discoveryResult =
                createResult(
                        Collections.emptyList()
                );

        DiagnosticDiscoveryCoordinator.DiscoveryRunner runner =
                new DiagnosticDiscoveryCoordinator.DiscoveryRunner() {
                    @Override
                    public DiagnosticDiscoveryResult discover() {
                        return discoveryResult;
                    }
                };

        DiagnosticDiscoveryCoordinator coordinator =
                new DiagnosticDiscoveryCoordinator(
                        runner,
                        new EcuSelectionPolicy()
                );

        DiagnosticDiscoveryCoordinator.Result result =
                coordinator.discover();

        assertEquals(
                EcuSelectionPolicy.Status.NONE,
                result.getSelectionResult().getStatus()
        );
    }

    @NonNull
    private DiagnosticDiscoveryResult createResult(
            @NonNull List<EcuDiscoveryObservation> observations) {

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

    @NonNull
    private EcuDiscoveryObservation createObservation(
            @NonNull EcuMatchResult matchResult) {

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

    @NonNull
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

    @NonNull
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

    @NonNull
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