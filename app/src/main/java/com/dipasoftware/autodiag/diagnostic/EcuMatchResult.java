package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuMatchResult
 *
 * Tipo.......: Model
 *
 * Descrizione:
 *
 * Rappresenta il risultato del confronto tra l'identificazione
 * letta dalla vettura e una ECU presente nel catalogo.
 *
 * ****************************************************************************
 */
public class EcuMatchResult {

    /**
     * Tipologia del risultato.
     */
    public enum Status {

        /**
         * Corrispondenza forte.
         */
        EXACT,

        /**
         * Corrispondenza plausibile ma non definitiva.
         */
        PROBABLE,

        /**
         * Nessuna corrispondenza.
         */
        NONE
    }

    /**
     * Stato del match.
     */
    @NonNull
    private final Status status;

    /**
     * ECU trovata.
     */
    @Nullable
    private final EcuDefinition ecuDefinition;

    /**
     * Punteggio numerico.
     *
     * Intervallo:
     *
     * 0 - 100
     */
    private final int score;

    /**
     * Costruttore.
     *
     * @param status stato.
     * @param ecuDefinition ECU.
     * @param score punteggio.
     */
    public EcuMatchResult(
            @NonNull Status status,
            @Nullable EcuDefinition ecuDefinition,
            int score) {

        this.status =
                status;

        this.ecuDefinition =
                ecuDefinition;

        this.score =
                Math.max(
                        0,
                        Math.min(
                                100,
                                score
                        )
                );
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public EcuDefinition getEcuDefinition() {
        return ecuDefinition;
    }

    public int getScore() {
        return score;
    }

    public boolean isExact() {
        return status == Status.EXACT;
    }

    public boolean isProbable() {
        return status == Status.PROBABLE;
    }

    public boolean isNone() {
        return status == Status.NONE;
    }
}