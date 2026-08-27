package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

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
 * Confronta EcuIdentification, cioè le informazioni realmente lette
 * dalla vettura, con gli identificatori dichiarati nelle EcuDefinition
 * del catalogo.
 *
 * Il matcher:
 *
 * - NON comunica con la ECU;
 * - NON carica dataset;
 * - NON modifica dati;
 * - NON decide quale protocollo utilizzare.
 *
 * Produce esclusivamente un risultato di matching.
 *
 * Strategia:
 *
 * Hardware number   = 50 punti
 * Software number   = 30 punti
 * Part number       = 20 punti
 * Supplier          = 10 punti
 * VIN pattern       = 10 punti
 *
 * EXACT:
 *
 * - almeno un identificatore ECU forte;
 * - punteggio >= 80.
 *
 * PROBABLE:
 *
 * - punteggio >= 40;
 * - oppure identificatore forte presente ma punteggio inferiore
 *   alla soglia EXACT.
 *
 * NONE:
 *
 * - nessuna corrispondenza significativa.
 *
 * ****************************************************************************
 */
public class EcuCatalogMatcher {

    /**
     * Punteggio hardware number.
     */
    private static final int SCORE_HARDWARE =
            50;

    /**
     * Punteggio software number.
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
     * Esegue il matching dell'identificazione contro
     * tutte le ECU del catalogo.
     *
     * @param identification identificazione letta dalla ECU.
     * @param definitions ECU disponibili nel catalogo.
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

        boolean bestHasStrongIdentifier =
                false;

        for (
                EcuDefinition definition :
                definitions
        ) {

            EcuDefinitionIdentifier identifiers =
                    definition.getIdentifiers();

            int score =
                    0;

            boolean hasStrongIdentifier =
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

                score +=
                        SCORE_HARDWARE;

                hasStrongIdentifier =
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

                score +=
                        SCORE_SOFTWARE;

                hasStrongIdentifier =
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

                score +=
                        SCORE_PART_NUMBER;

                hasStrongIdentifier =
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

                score +=
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

                score +=
                        SCORE_VIN;
            }

            /*
             * ---------------------------------------------------------
             * MIGLIOR RISULTATO
             * ---------------------------------------------------------
             */

            if (score > bestScore) {

                bestScore =
                        score;

                bestDefinition =
                        definition;

                bestHasStrongIdentifier =
                        hasStrongIdentifier;
            }
        }

        /*
         * Nessun candidato.
         */
        if (bestDefinition == null) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    0
            );
        }

        /*
         * Nessuna corrispondenza significativa.
         */
        if (bestScore <
                PROBABLE_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.NONE,
                    null,
                    bestScore
            );
        }

        /*
         * Match ESATTO.
         *
         * Un semplice supplier o VIN non è sufficiente.
         */
        if (bestHasStrongIdentifier
                &&
                bestScore >= EXACT_THRESHOLD) {

            return new EcuMatchResult(
                    EcuMatchResult.Status.EXACT,
                    bestDefinition,
                    bestScore
            );
        }

        /*
         * Match PROBABILE.
         */
        return new EcuMatchResult(
                EcuMatchResult.Status.PROBABLE,
                bestDefinition,
                bestScore
        );
    }
}