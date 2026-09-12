package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EcuSelectionPolicy {

    public enum Status {
        NONE,
        AUTO_SELECTED,
        CONFIRMATION_REQUIRED,
        MULTIPLE_ECUS
    }

    public static class Result {

        @NonNull
        private final Status status;

        @Nullable
        private final EcuDiscoveryObservation observation;

        private Result(
                @NonNull Status status,
                @Nullable EcuDiscoveryObservation observation) {

            this.status = status;
            this.observation = observation;
        }

        @NonNull
        public Status getStatus() {
            return status;
        }

        @Nullable
        public EcuDiscoveryObservation getObservation() {
            return observation;
        }

        /**
         * Restituisce true solo quando esiste una ECU effettivamente
         * selezionata automaticamente dalla policy.
         */
        public boolean hasSelectedEcu() {
            return status == Status.AUTO_SELECTED
                    && observation != null;
        }

        /**
         * Restituisce true quando esiste una singola ECU candidata che
         * necessita della conferma dell'utente.
         */
        public boolean hasCandidateForConfirmation() {
            return status == Status.CONFIRMATION_REQUIRED
                    && observation != null;
        }

        public boolean isAutoSelected() {
            return status == Status.AUTO_SELECTED;
        }

        public boolean requiresUserConfirmation() {
            return status == Status.CONFIRMATION_REQUIRED;
        }

        public boolean hasMultipleEcus() {
            return status == Status.MULTIPLE_ECUS;
        }
    }

    @NonNull
    public Result evaluate(
            @NonNull DiagnosticDiscoveryResult result) {

        if (!result.hasEcuObservations()) {
            return new Result(
                    Status.NONE,
                    null
            );
        }

        if (result.getEcuObservations().size() > 1) {
            return new Result(
                    Status.MULTIPLE_ECUS,
                    null
            );
        }

        EcuDiscoveryObservation observation =
                result.getEcuObservations().get(0);

        if (observation.getMatchResult() != null
                && observation.getMatchResult().isAutoSelectionSafe()) {

            return new Result(
                    Status.AUTO_SELECTED,
                    observation
            );
        }

        return new Result(
                Status.CONFIRMATION_REQUIRED,
                observation
        );
    }
}