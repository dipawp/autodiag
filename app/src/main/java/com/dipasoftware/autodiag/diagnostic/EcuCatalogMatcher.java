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
     * e restituisce tutti i candidati con almeno
     * una corrispondenza.
     *
     * I candidati vengono ordinati per punteggio
     * decrescente.
     *
     * @param identification identificazione letta.
     * @param definitions catalogo ECU.
     *
     * @return candidati ordinati.
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
             * Il candidato viene conservato se esiste
             * almeno una corrispondenza.
             *
             * Non applichiamo qui la soglia PROBABLE.
             * La classificazione viene fatta da match().
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
     * Restituisce il miglior risultato del matching.
     *
     * Tiene conto anche dell'ambiguità tra candidati
     * con lo stesso punteggio.
     *
     * @param identification identificazione letta.
     * @param definitions catalogo ECU.
     *
     * @return risultato.
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
         * ---------------------------------------------------------
         * NESSUN CANDIDATO
         * ---------------------------------------------------------
         */

        if (candidates.isEmpty()) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    0,
                    0,
                    false,
                    0,
                    0
            );
        }

        EcuMatchCandidate best =
                candidates.get(0);

        int bestScore =
                best.getScore();

        /*
         * ---------------------------------------------------------
         * SECONDO CANDIDATO
         * ---------------------------------------------------------
         */

        int candidateCount =
                candidates.size();

        int secondBestScore =
                0;

        boolean ambiguous =
                false;

        int scoreGap =
                bestScore;

        if (candidateCount > 1) {

            EcuMatchCandidate second =
                    candidates.get(1);

            secondBestScore =
                    second.getScore();

            scoreGap =
                    bestScore
                            - secondBestScore;

            /*
             * Due o più candidati con lo stesso punteggio
             * rendono il risultato ambiguo.
             */
            if (bestScore ==
                    secondBestScore) {

                ambiguous =
                        true;
            }
        }

        /*
         * ---------------------------------------------------------
         * NESSUN MATCH SIGNIFICATIVO
         * ---------------------------------------------------------
         */

        if (bestScore <
                PROBABLE_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    bestScore,
                    candidateCount,
                    ambiguous,
                    secondBestScore,
                    scoreGap
            );
        }

        /*
         * ---------------------------------------------------------
         * EXACT
         * ---------------------------------------------------------
         *
         * Un EXACT richiede un identificatore forte:
         *
         * - hardware;
         * - software;
         * - part number.
         *
         * Inoltre il risultato NON deve essere ambiguo.
         */
        if (best.hasStrongIdentifierMatch()
                &&
                bestScore >=
                        EXACT_THRESHOLD
                &&
                !ambiguous) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.EXACT,
                    best.getEcuDefinition(),
                    bestScore,
                    candidateCount,
                    false,
                    secondBestScore,
                    scoreGap
            );
        }

        /*
         * ---------------------------------------------------------
         * PROBABLE / AMBIGUO
         * ---------------------------------------------------------
         *
         * Anche un candidato con punteggio alto ma ambiguo
         * rimane PROBABLE.
         */
        return new EcuMatchResult(
                EcuMatchResult.Status.PROBABLE,
                best.getEcuDefinition(),
                bestScore,
                candidateCount,
                ambiguous,
                secondBestScore,
                scoreGap
        );
    }

    /**
     * Costruisce il candidato per una ECU.
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