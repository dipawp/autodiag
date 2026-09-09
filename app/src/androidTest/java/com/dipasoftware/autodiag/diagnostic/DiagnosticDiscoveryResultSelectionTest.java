package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryResultSelectionTest
 *
 * Tipo.......: Android Instrumented Test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica le regole di autoselezione ECU.
 *
 * In particolare:
 *
 * - una sola observation con match EXACT sicuro può essere autoselezionata;
 * - più observations impediscono sempre l'autoselezione;
 * - una observation con match PROBABLE non può essere autoselezionata;
 * - il modello legacy senza observations mantiene il comportamento precedente.
 *
 * ****************************************************************************
 */
@RunWith(AndroidJUnit4.class)
public class DiagnosticDiscoveryResultSelectionTest {

    @Test
    public void singleExactObservationIsSafeForAutoSelection() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuMatchResult matchResult =
                new EcuMatchResult(
                        EcuMatchResult.Status.EXACT,
                        ecu,
                        100
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        ecu,
                        identification,
                        matchResult
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.singletonList(
                                observation
                        ),
                        identification,
                        matchResult
                );

        assertTrue(
                result.isAutoSelectionSafe()
        );
    }

    @Test
    public void multipleExactObservationsAreNotSafeForAutoSelection() {

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

        DiagnosticDiscoveryResult result =
                createResult(
                        java.util.Arrays.asList(
                                observationA,
                                observationB
                        ),
                        identificationA,
                        matchA
                );

        assertFalse(
                result.isAutoSelectionSafe()
        );
    }

    @Test
    public void probableObservationIsNotSafeForAutoSelection() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuMatchResult matchResult =
                new EcuMatchResult(
                        EcuMatchResult.Status.PROBABLE,
                        ecu,
                        60
                );

        EcuDiscoveryObservation observation =
                new EcuDiscoveryObservation(
                        ecu,
                        identification,
                        matchResult
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.singletonList(
                                observation
                        ),
                        identification,
                        matchResult
                );

        assertFalse(
                result.isAutoSelectionSafe()
        );
    }

    @Test
    public void legacyResultKeepsPreviousAutoSelectionBehavior() {

        EcuDefinition ecu =
                createEcu(
                        "ECU_A"
                );

        EcuIdentification identification =
                createIdentification(
                        "VIN_A"
                );

        EcuMatchResult matchResult =
                new EcuMatchResult(
                        EcuMatchResult.Status.EXACT,
                        ecu,
                        100
                );

        DiagnosticDiscoveryResult result =
                createResult(
                        Collections.emptyList(),
                        identification,
                        matchResult
                );

        assertTrue(
                result.isAutoSelectionSafe()
        );
    }

    @NonNull
    private DiagnosticDiscoveryResult createResult(
            @NonNull List<EcuDiscoveryObservation> observations,
            @NonNull EcuIdentification identification,
            @NonNull EcuMatchResult matchResult) {

        return new DiagnosticDiscoveryResult(
                new VehicleIdentification(""),
                Collections.emptyList(),
                identification,
                matchResult,
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
