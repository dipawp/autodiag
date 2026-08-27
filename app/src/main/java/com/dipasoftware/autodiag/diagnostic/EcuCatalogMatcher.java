package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuCatalogMatcher
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Confronta le informazioni realmente lette dalla ECU con
 * gli identificatori presenti nel catalogo.
 *
 * Il matcher:
 *
 * - non comunica con la ECU;
 * - non invia comandi;
 * - non carica dataset;
 * - non modifica lo stato del veicolo.
 *
 * Produce una lista ordinata di candidati.
 *
 * Strategia di punteggio:
 *
 * Hardware number   = 50 punti
 * Software number   = 30 punti
 * Part number       = 20 punti
 * Supplier          = 10 punti
 * VIN pattern       = 10 punti
 *
 * Il VIN e il supplier da soli non sono sufficienti
 * per dichiarare una ECU identificata.
 *
 * ****************************************************************************
 */
public class EcuCatalogMatcher {

    /**
     * Punteggio hardware.
     */
    private static final int SCORE_HARDWARE =
            50;

    /**
     * Punteggio software.
     */
    private static final int SCORE_SOFTWARE =
            30;

    /**
     * Punteggio part number.
     */
    private static final int SCORE_PART_NUMBER =
            20;

    /**
     * Punteggio supplier.
     */
    private static final int SCORE_SUPPLIER =
            10;

    /**
     * Punteggio VIN.
     */
    private static final int SCORE_VIN =
            10;

    /**
     * Soglia match esatto.
     */
    private static final int EXACT_THRESHOLD =
            80;

    /**
     * Soglia match probabile.
     */
    private static final int PROBABLE_THRESHOLD =
            40;

    /**
     * Confronta l'identificazione con il catalogo
     * e restituisce tutti i candidati che hanno
     * almeno una corrispondenza significativa.
     *
     * IMPORTANTE:
     *
     * La lista dei candidati non utilizza la soglia
     * PROBABLE_THRESHOLD.
     *
     * Questo permette al chiamante di vedere anche
     * candidati deboli e utilizzare successivamente
     * il punteggio per la classificazione.
     *
     * @param identification identificazione letta.
     * @param definitions ECU disponibili nel catalogo.
     *
     * @return candidati ordinati per punteggio decrescente.
     */
    @NonNull
    public List<EcuMatchCandidate> findCandidates(
            @NonNull EcuIdentification identification,
            @NonNull List<EcuDefinition> definitions) {

        List<EcuMatchCandidate> candidates =
                new ArrayList<>();

        for (
                EcuDefinition definition :
                definitions
        ) {

            EcuMatchCandidate candidate =
                    createCandidate(
                            identification,
                            definition
                    );

            /*
             * Un candidato viene incluso se almeno un
             * identificatore ha prodotto una corrispondenza.
             *
             * Un punteggio pari a zero non è un candidato.
             */
            if (candidate.getScore() > 0) {

                candidates.add(
                        candidate
                );
            }
        }

        Collections.sort(
                candidates,
                new Comparator<EcuMatchCandidate>() {

                    @Override
                    public int compare(
                            EcuMatchCandidate first,
                            EcuMatchCandidate second) {

                        return Integer.compare(
                                second.getScore(),
                                first.getScore()
                        );
                    }
                }
        );

        return Collections.unmodifiableList(
                candidates
        );
    }

    /**
     * Restituisce il miglior candidato classificato
     * come EXACT, PROBABLE oppure NONE.
     *
     * @param identification identificazione letta.
     * @param definitions catalogo ECU.
     *
     * @return risultato migliore.
     */
    @NonNull
    public EcuMatchResult match(
            @NonNull EcuIdentification identification,
            @NonNull List<EcuDefinition> definitions) {

        List<EcuMatchCandidate> candidates =
                findCandidates(
                        identification,
                        definitions
                );

        /*
         * Nessun candidato con corrispondenze.
         */
        if (candidates.isEmpty()) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    0
            );
        }

        EcuMatchCandidate best =
                candidates.get(0);

        /*
         * ---------------------------------------------------------
         * AMBIGUITÀ
         * ---------------------------------------------------------
         *
         * Se due candidati hanno lo stesso punteggio,
         * non selezioniamo automaticamente l'ECU.
         *
         * Il punteggio migliore viene comunque restituito
         * per consentire alla UI di mostrare il risultato.
         */
        if (candidates.size() > 1) {

            EcuMatchCandidate second =
                    candidates.get(1);

            if (best.getScore() ==
                    second.getScore()) {

                return new EcuMatchResult(
                        EcuMatchResult.Status.PROBABLE,
                        best.getEcuDefinition(),
                        best.getScore()
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * NESSUN MATCH SIGNIFICATIVO
         * ---------------------------------------------------------
         *
         * Manteniamo il punteggio reale.
         *
         * Esempio:
         *
         * supplier = 10
         * VIN      = 10
         * totale   = 20
         *
         * risultato:
         *
         * NONE
         * score = 20
         */
        if (best.getScore() <
                PROBABLE_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    best.getScore()
            );
        }

        /*
         * ---------------------------------------------------------
         * MATCH ESATTO
         * ---------------------------------------------------------
         *
         * Per EXACT deve essere presente almeno un
         * identificatore ECU forte:
         *
         * - hardware;
         * - software;
         * - part number.
         *
         * Supplier e VIN da soli non bastano.
         */
        if (best.hasStrongIdentifierMatch()
                &&
                best.getScore() >=
                        EXACT_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.EXACT,
                    best.getEcuDefinition(),
                    best.getScore()
            );
        }

        /*
         * ---------------------------------------------------------
         * MATCH PROBABILE
         * ---------------------------------------------------------
         */

        return new EcuMatchResult(
                EcuMatchResult.Status.PROBABLE,
                best.getEcuDefinition(),
                best.getScore()
        );
    }

    /**
     * Crea il candidato per una specifica ECU.
     *
     * @param identification identificazione letta.
     * @param definition ECU catalogata.
     *
     * @return candidato.
     */
    @NonNull
    private EcuMatchCandidate createCandidate(
            @NonNull EcuIdentification identification,
            @NonNull EcuDefinition definition) {

        EcuDefinitionIdentifier identifiers =
                definition.getIdentifiers();

        int hardwareScore =
                0;

        int softwareScore =
                0;

        int partNumberScore =
                0;

        int supplierScore =
                0;

        int vinScore =
                0;

        boolean strongIdentifierMatch =
                false;

        /*
         * ---------------------------------------------------------
         * HARDWARE
         * ---------------------------------------------------------
         */

        if (identification.hasEcuHardwareNumber()
                &&
                identifiers.matchesHardware(
                        identification
                                .getEcuHardwareNumber()
                )) {

            hardwareScore =
                    SCORE_HARDWARE;

            strongIdentifierMatch =
                    true;
        }

        /*
         * ---------------------------------------------------------
         * SOFTWARE
         * ---------------------------------------------------------
         */

        if (identification.hasEcuSoftwareNumber()
                &&
                identifiers.matchesSoftware(
                        identification
                                .getEcuSoftwareNumber()
                )) {

            softwareScore =
                    SCORE_SOFTWARE;

            strongIdentifierMatch =
                    true;
        }

        /*
         * ---------------------------------------------------------
         * PART NUMBER
         * ---------------------------------------------------------
         */

        if (identification.hasEcuPartNumber()
                &&
                identifiers.matchesPartNumber(
                        identification
                                .getEcuPartNumber()
                )) {

            partNumberScore =
                    SCORE_PART_NUMBER;

            strongIdentifierMatch =
                    true;
        }

        /*
         * ---------------------------------------------------------
         * SUPPLIER
         * ---------------------------------------------------------
         */

        if (identification.hasSupplier()
                &&
                identifiers.matchesSupplier(
                        identification.getSupplier()
                )) {

            supplierScore =
                    SCORE_SUPPLIER;
        }

        /*
         * ---------------------------------------------------------
         * VIN
         * ---------------------------------------------------------
         */

        if (identification.hasVin()
                &&
                identifiers.matchesVin(
                        identification.getVin()
                )) {

            vinScore =
                    SCORE_VIN;
        }

        int totalScore =
                hardwareScore
                        + softwareScore
                        + partNumberScore
                        + supplierScore
                        + vinScore;

        return new EcuMatchCandidate(
                definition,
                totalScore,
                strongIdentifierMatch,
                hardwareScore,
                softwareScore,
                partNumberScore,
                supplierScore,
                vinScore
        );
    }
}