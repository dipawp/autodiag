package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuCatalogMatcher
 *
 * Tipo.......: Service
 *
 * Descrizione:
 *
 * Confronta EcuIdentification con le EcuDefinition presenti
 * nel catalogo.
 *
 * Il matcher non comunica con la ECU e non carica dataset.
 *
 * Utilizza esclusivamente le informazioni già disponibili.
 *
 * ****************************************************************************
 */
public class EcuCatalogMatcher {

    /**
     * Punteggio per hardware number esatto.
     */
    private static final int SCORE_HARDWARE =
            40;

    /**
     * Punteggio per software number esatto.
     */
    private static final int SCORE_SOFTWARE =
            30;

    /**
     * Punteggio per part number.
     */
    private static final int SCORE_PART_NUMBER =
            20;

    /**
     * Punteggio per supplier.
     */
    private static final int SCORE_SUPPLIER =
            10;

    /**
     * Soglia per match esatto.
     */
    private static final int EXACT_THRESHOLD =
            80;

    /**
     * Soglia per match probabile.
     */
    private static final int PROBABLE_THRESHOLD =
            40;

    /**
     * Esegue il matching.
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

        EcuDefinition bestDefinition =
                null;

        int bestScore =
                0;

        for (
                EcuDefinition definition :
                definitions
        ) {

            int score =
                    calculateScore(
                            identification,
                            definition
                    );

            if (score > bestScore) {

                bestScore =
                        score;

                bestDefinition =
                        definition;
            }
        }

        if (bestDefinition == null ||
                bestScore <
                        PROBABLE_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    bestScore
            );
        }

        if (bestScore >=
                EXACT_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.EXACT,
                    bestDefinition,
                    bestScore
            );
        }

        return new EcuMatchResult(
                EcuMatchResult.Status.PROBABLE,
                bestDefinition,
                bestScore
        );
    }

    /**
     * Calcola il punteggio di una ECU.
     *
     * Il punteggio utilizza solamente identificativi ECU.
     *
     * Il VIN NON viene utilizzato per dichiarare una ECU
     * esattamente identificata.
     *
     * @param identification identificazione.
     * @param definition ECU catalogata.
     *
     * @return punteggio.
     */
    private int calculateScore(
            @NonNull EcuIdentification identification,
            @NonNull EcuDefinition definition) {

        /*
         * Questa prima versione utilizza i campi esplicitamente
         * presenti in EcuDefinition.
         *
         * Per poter effettuare un matching reale su hardware/software,
         * il passo successivo sarà estendere EcuDefinition con gli
         * identificatori ammessi dal relativo dataset.
         *
         * Per ora evitiamo quindi di attribuire falsi match.
         */

        int score =
                0;

        /*
         * La presenza di un dataset OEM indica solamente
         * che esiste una definizione catalogata.
         *
         * Non è sufficiente per un match.
         */

        if (definition.getEcu() != null &&
                !definition.getEcu().trim().isEmpty()) {

            /*
             * Tentiamo solamente un confronto testuale
             * con hardware number se il nome ECU del catalogo
             * contiene direttamente quel valore.
             */
            if (containsIgnoreCase(
                    definition.getEcu(),
                    identification.getEcuHardwareNumber()
            )) {

                score +=
                        SCORE_HARDWARE;
            }

            /*
             * Part number.
             */
            if (containsIgnoreCase(
                    definition.getEcu(),
                    identification.getEcuPartNumber()
            )) {

                score +=
                        SCORE_PART_NUMBER;
            }

            /*
             * Supplier.
             */
            if (containsIgnoreCase(
                    definition.getEcu(),
                    identification.getSupplier()
            )) {

                score +=
                        SCORE_SUPPLIER;
            }
        }

        return score;
    }

    /**
     * Confronto case-insensitive.
     *
     * @param container testo contenitore.
     * @param value valore.
     *
     * @return true se il valore è presente.
     */
    private boolean containsIgnoreCase(
            @NonNull String container,
            @Nullable String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return false;
        }

        return container
                .toUpperCase(Locale.US)
                .contains(
                        value.trim()
                                .toUpperCase(Locale.US)
                );
    }
}