package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

public class DiagnosticDiscoveryCoordinator {

    public interface DiscoveryRunner {

        @NonNull
        DiagnosticDiscoveryResult discover();
    }

    public static class Result {

        @NonNull
        private final DiagnosticDiscoveryResult discoveryResult;

        @NonNull
        private final EcuSelectionPolicy.Result selectionResult;

        private Result(
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

        /**
         * Restituisce true quando esiste una ECU che la policy considera
         * effettivamente selezionata automaticamente.
         */
        public boolean hasSelectedEcu() {
            return selectionResult.hasSelectedEcu();
        }

        /**
         * Restituisce true quando esiste una singola ECU candidata che
         * deve essere confermata dall'utente.
         */
        public boolean hasCandidateForConfirmation() {
            return selectionResult.hasCandidateForConfirmation();
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