package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuSelectionPolicy
 *
 * Tipo.......: Policy
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Definisce le regole con cui una discovery ECU può produrre una
 * selezione automatica.
 *
 * La policy distingue esplicitamente:
 *
 * - nessuna ECU osservata;
 * - una sola ECU osservata e selezionabile automaticamente;
 * - una sola ECU osservata ma non sufficientemente certa;
 * - più ECU osservate, quindi selezione obbligatoriamente manuale.
 *
 * La classe non esegue comunicazione diagnostica e non modifica il risultato
 * della discovery.
 *
 * ****************************************************************************
 */
public class EcuSelectionPolicy {

    /**
     * Stati possibili della selezione.
     */
    public enum Status {

        /**
         * Nessuna ECU disponibile.
         */
        NONE,

        /**
         * Una sola ECU può essere selezionata automaticamente.
         */
        AUTO_SELECTED,

        /**
         * Esiste una sola ECU, ma richiede conferma.
         */
        CONFIRMATION_REQUIRED,

        /**
         * Sono presenti più ECU e l'utente deve scegliere.
         */
        MULTIPLE_ECUS
    }

    /**
     * Risultato della policy.
     */
    public static class Result {

        @NonNull
        private final Status status;

        @Nullable
        private final EcuDiscoveryObservation observation;

        private Result(
                @NonNull Status status,
                @Nullable EcuDiscoveryObservation observation) {

            this.status =
                    status;

            this.observation =
                    observation;
        }

        @NonNull
        public Status getStatus() {
            return status;
        }

        @Nullable
        public EcuDiscoveryObservation getObservation() {
            return observation;
        }

        public boolean hasSelection() {
            return observation != null;
        }

        public boolean isAutoSelected() {
            return status ==
                    Status.AUTO_SELECTED;
        }

        public boolean requiresUserConfirmation() {
            return status ==
                    Status.CONFIRMATION_REQUIRED;
        }

        public boolean hasMultipleEcus() {
            return status ==
                    Status.MULTIPLE_ECUS;
        }
    }

    /**
     * Applica la policy alle observations prodotte dalla discovery.
     *
     * @param result risultato discovery.
     *
     * @return risultato della policy.
     */
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
                &&
                observation
                        .getMatchResult()
                        .isAutoSelectionSafe()) {

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