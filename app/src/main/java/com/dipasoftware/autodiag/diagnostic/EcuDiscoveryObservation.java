package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuDiscoveryObservation
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta una singola osservazione ottenuta durante la discovery di una ECU.
 *
 * Una observation mantiene insieme:
 *
 * - la definizione ECU utilizzata come candidata/probe;
 * - l'identificazione effettivamente letta dalla ECU;
 * - il risultato del matching associato a quella identificazione.
 *
 * Questo modello è importante per la discovery multi-ECU perché impedisce
 * di perdere la relazione tra:
 *
 *      ECU -> target -> identification -> match
 *
 * Ogni ECU interrogata deve produrre una observation indipendente.
 *
 * ****************************************************************************
 */
public class EcuDiscoveryObservation {

    /**
     * Definizione ECU utilizzata durante la discovery.
     */
    @NonNull
    private final EcuDefinition candidate;

    /**
     * Identificazione effettivamente letta dalla ECU.
     */
    @NonNull
    private final EcuIdentification identification;

    /**
     * Risultato del matching dell'identificazione.
     *
     * Può essere null quando il matching non è disponibile
     * oppure non è stato possibile determinarlo.
     */
    @Nullable
    private final EcuMatchResult matchResult;

    /**
     * Costruttore.
     *
     * @param candidate definizione ECU interrogata.
     * @param identification identificazione letta.
     * @param matchResult risultato matching, eventualmente null.
     */
    public EcuDiscoveryObservation(
            @NonNull EcuDefinition candidate,
            @NonNull EcuIdentification identification,
            @Nullable EcuMatchResult matchResult) {

        this.candidate =
                candidate;

        this.identification =
                identification;

        this.matchResult =
                matchResult;
    }

    /**
     * Restituisce la definizione ECU utilizzata come candidata.
     *
     * @return definizione ECU.
     */
    @NonNull
    public EcuDefinition getCandidate() {

        return candidate;
    }

    /**
     * Restituisce l'identificazione letta dalla ECU.
     *
     * @return identificazione ECU.
     */
    @NonNull
    public EcuIdentification getIdentification() {

        return identification;
    }

    /**
     * Restituisce il risultato del matching.
     *
     * @return risultato oppure null.
     */
    @Nullable
    public EcuMatchResult getMatchResult() {

        return matchResult;
    }

    /**
     * Indica se è disponibile un risultato di matching.
     *
     * @return true se il matching è disponibile.
     */
    public boolean hasMatchResult() {

        return matchResult != null;
    }
}