package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuMatchCandidate
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta una singola ECU candidata durante il processo
 * di identificazione automatica.
 *
 * Contiene:
 *
 * - EcuDefinition candidata;
 * - punteggio;
 * - presenza di identificatori forti;
 * - dettaglio del punteggio.
 *
 * La classe non decide se il candidato sia definitivo.
 * La decisione finale viene presa da EcuCatalogMatcher.
 *
 * ****************************************************************************
 */
public class EcuMatchCandidate {

    /**
     * Definizione ECU candidata.
     */
    @NonNull
    private final EcuDefinition ecuDefinition;

    /**
     * Punteggio totale.
     */
    private final int score;

    /**
     * Indica se almeno un identificatore forte
     * ha prodotto una corrispondenza.
     */
    private final boolean strongIdentifierMatch;

    /**
     * Punteggio hardware.
     */
    private final int hardwareScore;

    /**
     * Punteggio software.
     */
    private final int softwareScore;

    /**
     * Punteggio part number.
     */
    private final int partNumberScore;

    /**
     * Punteggio supplier.
     */
    private final int supplierScore;

    /**
     * Punteggio VIN.
     */
    private final int vinScore;

    /**
     * Costruttore.
     *
     * @param ecuDefinition ECU candidata.
     * @param score punteggio totale.
     * @param strongIdentifierMatch identificatore forte trovato.
     * @param hardwareScore punteggio hardware.
     * @param softwareScore punteggio software.
     * @param partNumberScore punteggio part number.
     * @param supplierScore punteggio supplier.
     * @param vinScore punteggio VIN.
     */
    public EcuMatchCandidate(
            @NonNull EcuDefinition ecuDefinition,
            int score,
            boolean strongIdentifierMatch,
            int hardwareScore,
            int softwareScore,
            int partNumberScore,
            int supplierScore,
            int vinScore) {

        this.ecuDefinition =
                ecuDefinition;

        this.score =
                score;

        this.strongIdentifierMatch =
                strongIdentifierMatch;

        this.hardwareScore =
                hardwareScore;

        this.softwareScore =
                softwareScore;

        this.partNumberScore =
                partNumberScore;

        this.supplierScore =
                supplierScore;

        this.vinScore =
                vinScore;
    }

    @NonNull
    public EcuDefinition getEcuDefinition() {

        return ecuDefinition;
    }

    public int getScore() {

        return score;
    }

    public boolean hasStrongIdentifierMatch() {

        return strongIdentifierMatch;
    }

    public int getHardwareScore() {

        return hardwareScore;
    }

    public int getSoftwareScore() {

        return softwareScore;
    }

    public int getPartNumberScore() {

        return partNumberScore;
    }

    public int getSupplierScore() {

        return supplierScore;
    }

    public int getVinScore() {

        return vinScore;
    }
}