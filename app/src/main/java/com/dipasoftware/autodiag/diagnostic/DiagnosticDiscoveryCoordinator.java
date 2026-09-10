package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryCoordinator
 *
 * Tipo.......: Application coordinator
 *
 * Descrizione:
 *
 * Coordina il passaggio dalla discovery diagnostica alla decisione di
 * selezione ECU.
 *
 * Il coordinatore non esegue direttamente comunicazione diagnostica:
 *
 * DiagnosticDiscoveryService
 *          ->
 * DiagnosticDiscoveryResult
 *          ->
 * EcuSelectionPolicy
 *          ->
 * EcuSelectionPolicy.Result
 *
 * L'uso di DiscoveryRunner mantiene il coordinatore indipendente dalla
 * concreta implementazione della discovery e permette test unitari semplici.
 *
 * ****************************************************************************
 */
public class DiagnosticDiscoveryCoordinator {

    /**
     * Provider astratto della discovery.
     *
     * Permette di testare il coordinatore senza aprire una connessione
     * diagnostica reale.
     */
    public interface DiscoveryRunner {

        @NonNull
        DiagnosticDiscoveryResult discover();
    }

    /**
     * Risultato completo del coordinamento.
     *
     * Contiene sia il risultato grezzo della discovery sia la decisione
     * prodotta dalla policy.
     */
    public static class Result {

        @NonNull
        private final DiagnosticDiscoveryResult discoveryResult;

        @NonNull
        private final EcuSelectionPolicy.Result selectionResult;

        public Result(
                @NonNull DiagnosticDiscoveryResult discoveryResult,
                @NonNull EcuSelectionPolicy.Result selectionResult) {

            this.discoveryResult = discoveryResult;
            this.selectionResult = selectionResult;
        }

        @NonNull
        public DiagnosticDiscoveryResult getDiscoveryResult() {
            return discoveryResult;
        }

        @NonNull
        public EcuSelectionPolicy.Result getSelectionResult() {
            return selectionResult;
        }

        public boolean isAutoSelected() {
            return selectionResult.isAutoSelected();
        }

        public boolean requiresUserConfirmation() {
            return selectionResult.requiresUserConfirmation();
        }

        public boolean hasMultipleEcus() {
            return selectionResult.hasMultipleEcus();
        }

        public boolean hasSelectionCandidate() {
            return selectionResult.hasSelection();
        }
    }

    @NonNull
    private final DiscoveryRunner discoveryRunner;

    @NonNull
    private final EcuSelectionPolicy selectionPolicy;

    public DiagnosticDiscoveryCoordinator(
            @NonNull DiscoveryRunner discoveryRunner,
            @NonNull EcuSelectionPolicy selectionPolicy) {

        this.discoveryRunner = discoveryRunner;
        this.selectionPolicy = selectionPolicy;
    }

    /**
     * Esegue discovery e applica la policy di selezione ECU.
     *
     * @return risultato completo discovery + selezione.
     */
    @NonNull
    public Result discover() {

        DiagnosticDiscoveryResult discoveryResult =
                discoveryRunner.discover();

        EcuSelectionPolicy.Result selectionResult =
                selectionPolicy.evaluate(discoveryResult);

        return new Result(
                discoveryResult,
                selectionResult
        );
    }
}