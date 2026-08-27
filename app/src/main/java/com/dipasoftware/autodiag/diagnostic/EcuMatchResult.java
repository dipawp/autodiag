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
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta il risultato finale del confronto tra una
 * EcuIdentification e il catalogo ECU.
 *
 * Il risultato contiene:
 *
 * - stato del matching;
 * - migliore ECU candidata;
 * - punteggio del migliore candidato;
 * - numero di candidati;
 * - eventuale ambiguità;
 * - punteggio del secondo candidato;
 * - differenza tra primo e secondo candidato.
 *
 * IMPORTANTE:
 *
 * Un risultato EXACT non implica automaticamente che l'ECU
 * debba essere selezionata senza ulteriori controlli se il
 * risultato è ambiguo.
 *
 * La proprietà isAutoSelectionSafe() viene utilizzata per
 * distinguere un match forte e non ambiguo da un match che
 * richiede conferma.
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
         * Nessuna corrispondenza significativa.
         */
        NONE
    }

    /**
     * Stato del match.
     */
    @NonNull
    private final Status status;

    /**
     * Migliore ECU candidata.
     */
    @Nullable
    private final EcuDefinition ecuDefinition;

    /**
     * Punteggio del migliore candidato.
     *
     * Intervallo:
     *
     * 0 - 100
     */
    private final int score;

    /**
     * Numero totale di candidati considerati dal matcher.
     */
    private final int candidateCount;

    /**
     * Indica se il risultato è ambiguo.
     */
    private final boolean ambiguous;

    /**
     * Punteggio del secondo candidato.
     *
     * Vale 0 se non esiste un secondo candidato.
     */
    private final int secondBestScore;

    /**
     * Differenza tra primo e secondo candidato.
     *
     * Vale score quando non esiste un secondo candidato.
     */
    private final int scoreGap;

    /**
     * Costruttore compatibile con la versione precedente.
     *
     * Mantiene la firma già utilizzata dal progetto.
     *
     * @param status stato.
     * @param ecuDefinition ECU migliore.
     * @param score punteggio.
     */
    public EcuMatchResult(
            @NonNull Status status,
            @Nullable EcuDefinition ecuDefinition,
            int score) {

        this(
                status,
                ecuDefinition,
                score,
                ecuDefinition == null ? 0 : 1,
                false,
                0,
                score
        );
    }

    /**
     * Costruttore completo.
     *
     * @param status stato.
     * @param ecuDefinition migliore ECU candidata.
     * @param score punteggio migliore.
     * @param candidateCount numero candidati.
     * @param ambiguous true se ambiguo.
     * @param secondBestScore punteggio secondo candidato.
     * @param scoreGap differenza tra primo e secondo.
     */
    public EcuMatchResult(
            @NonNull Status status,
            @Nullable EcuDefinition ecuDefinition,
            int score,
            int candidateCount,
            boolean ambiguous,
            int secondBestScore,
            int scoreGap) {

        this.status =
                status;

        this.ecuDefinition =
                ecuDefinition;

        this.score =
                clampScore(
                        score
                );

        this.candidateCount =
                Math.max(
                        0,
                        candidateCount
                );

        this.ambiguous =
                ambiguous;

        this.secondBestScore =
                clampScore(
                        secondBestScore
                );

        this.scoreGap =
                Math.max(
                        0,
                        Math.min(
                                100,
                                scoreGap
                        )
                );
    }

    /**
     * Costruttore specifico quando il risultato deriva
     * da una lista di candidati.
     *
     * Questo costruttore evita che il chiamante debba
     * calcolare manualmente il gap.
     *
     * @param status stato.
     * @param ecuDefinition ECU migliore.
     * @param score punteggio migliore.
     * @param candidateCount candidati.
     * @param secondBestScore secondo punteggio.
     */
    public EcuMatchResult(
            @NonNull Status status,
            @Nullable EcuDefinition ecuDefinition,
            int score,
            int candidateCount,
            int secondBestScore) {

        this(
                status,
                ecuDefinition,
                score,
                candidateCount,
                candidateCount > 1
                        && score == secondBestScore,
                secondBestScore,
                score - secondBestScore
        );
    }

    /**
     * Limita il punteggio all'intervallo 0-100.
     *
     * @param score punteggio.
     *
     * @return punteggio limitato.
     */
    private int clampScore(
            int score) {

        return Math.max(
                0,
                Math.min(
                        100,
                        score
                )
        );
    }

    /**
     * Restituisce lo stato del match.
     *
     * @return stato.
     */
    @NonNull
    public Status getStatus() {

        return status;
    }

    /**
     * Restituisce la migliore ECU candidata.
     *
     * @return ECU oppure null.
     */
    @Nullable
    public EcuDefinition getEcuDefinition() {

        return ecuDefinition;
    }

    /**
     * Restituisce il punteggio del migliore candidato.
     *
     * @return punteggio 0-100.
     */
    public int getScore() {

        return score;
    }

    /**
     * Restituisce il numero di candidati.
     *
     * @return numero candidati.
     */
    public int getCandidateCount() {

        return candidateCount;
    }

    /**
     * Indica se il risultato è ambiguo.
     *
     * Un risultato ambiguo non deve essere selezionato
     * automaticamente.
     *
     * @return true se ambiguo.
     */
    public boolean isAmbiguous() {

        return ambiguous;
    }

    /**
     * Restituisce il punteggio del secondo candidato.
     *
     * @return punteggio.
     */
    public int getSecondBestScore() {

        return secondBestScore;
    }

    /**
     * Restituisce la differenza tra primo e secondo candidato.
     *
     * Un valore maggiore indica una migliore separazione.
     *
     * @return differenza.
     */
    public int getScoreGap() {

        return scoreGap;
    }

    /**
     * Indica se il risultato è EXACT.
     *
     * @return true se esatto.
     */
    public boolean isExact() {

        return status ==
                Status.EXACT;
    }

    /**
     * Indica se il risultato è PROBABLE.
     *
     * @return true se probabile.
     */
    public boolean isProbable() {

        return status ==
                Status.PROBABLE;
    }

    /**
     * Indica se il risultato è NONE.
     *
     * @return true se nessun match.
     */
    public boolean isNone() {

        return status ==
                Status.NONE;
    }

    /**
     * Indica se l'ECU può essere selezionata automaticamente
     * sulla base del risultato di matching.
     *
     * Regola:
     *
     * - deve essere EXACT;
     * - non deve essere ambiguo;
     * - deve esistere una ECU candidata;
     * - deve essere presente un punteggio significativo.
     *
     * @return true se l'autoselezione è sicura.
     */
    public boolean isAutoSelectionSafe() {

        return isExact()
                && !ambiguous
                && ecuDefinition != null
                && score > 0;
    }

    /**
     * Indica se il risultato richiede una scelta manuale
     * o una conferma dell'utente.
     *
     * @return true se serve intervento utente.
     */
    public boolean requiresUserConfirmation() {

        if (isNone()) {

            return true;
        }

        if (isAmbiguous()) {

            return true;
        }

        return !isAutoSelectionSafe();
    }

    /**
     * Rappresentazione testuale del risultato.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "EcuMatchResult{" +
                "status=" +
                status +
                ", ecuDefinition=" +
                ecuDefinition +
                ", score=" +
                score +
                ", candidateCount=" +
                candidateCount +
                ", ambiguous=" +
                ambiguous +
                ", secondBestScore=" +
                secondBestScore +
                ", scoreGap=" +
                scoreGap +
                '}';
    }
}