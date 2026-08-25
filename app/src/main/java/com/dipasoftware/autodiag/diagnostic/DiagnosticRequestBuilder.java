package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRequestBuilder
 *
 * Tipo.......: Service / Utility
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Costruisce la richiesta diagnostica da inviare all'ECU
 * utilizzando i metadati presenti in PidDefinition.
 *
 * La classe non esegue alcuna comunicazione.
 *
 * Supporta:
 *
 * - PID standard memorizzati come 0C;
 * - PID standard memorizzati come 010C;
 * - DID UDS come F190 con mode 22;
 * - richieste esplicite OEM;
 * - richieste HEX con spazi.
 *
 * Regola:
 *
 * 1. Se esiste una request esplicita, viene utilizzata.
 *
 * 2. Altrimenti, se il PID contiene già il mode
 *    all'inizio, il PID viene utilizzato direttamente.
 *
 * 3. Altrimenti viene costruito:
 *
 *        mode + pid
 *
 * ****************************************************************************
 */
public class DiagnosticRequestBuilder {

    /**
     * Costruisce la richiesta diagnostica.
     *
     * @param definition definizione diagnostica.
     *
     * @return richiesta HEX normalizzata.
     */
    @NonNull
    public String build(
            @NonNull PidDefinition definition) {

        /*
         * ---------------------------------------------------------
         * REQUEST ESPLICITA
         * ---------------------------------------------------------
         */

        if (definition.hasExplicitRequest()) {

            return normalizeHex(
                    definition.getRequest()
            );
        }

        String mode =
                normalizeHex(
                        definition.getMode()
                );

        String pid =
                normalizeHex(
                        definition.getPid()
                );

        /*
         * ---------------------------------------------------------
         * VALIDAZIONE
         * ---------------------------------------------------------
         */

        if (mode.isEmpty()) {

            throw new IllegalArgumentException(
                    "Mode non definito per PID "
                            + definition.getPid()
            );
        }

        if (pid.isEmpty()) {

            throw new IllegalArgumentException(
                    "PID non definito."
            );
        }

        /*
         * ---------------------------------------------------------
         * PID GIÀ COMPLETO
         * ---------------------------------------------------------
         *
         * Esempio:
         *
         * mode = 01
         * pid  = 010C
         *
         * risultato:
         *
         * 010C
         */

        if (pid.startsWith(
                mode
        )) {

            return pid;
        }

        /*
         * ---------------------------------------------------------
         * PID BREVE
         * ---------------------------------------------------------
         *
         * Esempio:
         *
         * mode = 01
         * pid  = 0C
         *
         * risultato:
         *
         * 010C
         */

        return normalizeHex(
                mode + pid
        );
    }

    /**
     * Normalizza una stringa HEX.
     *
     * Rimuove:
     *
     * - spazi;
     * - CR;
     * - LF;
     * - prompt ELM327.
     *
     * Converte tutto in maiuscolo e verifica che
     * tutti i caratteri siano HEX validi.
     *
     * @param value valore HEX.
     *
     * @return valore normalizzato.
     */
    @NonNull
    private String normalizeHex(
            @NonNull String value) {

        String normalized =
                value
                        .replace(
                                " ",
                                ""
                        )
                        .replace(
                                "\r",
                                ""
                        )
                        .replace(
                                "\n",
                                ""
                        )
                        .replace(
                                ">",
                                ""
                        )
                        .trim()
                        .toUpperCase(
                                Locale.US
                        );

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "Valore HEX vuoto."
            );
        }

        if ((normalized.length() % 2) != 0) {

            throw new IllegalArgumentException(
                    "Valore HEX con numero dispari "
                            + "di caratteri: "
                            + normalized
            );
        }

        for (
                int index = 0;
                index < normalized.length();
                index++
        ) {

            char character =
                    normalized.charAt(
                            index
                    );

            boolean hexadecimal =
                    (character >= '0'
                            && character <= '9')
                            ||
                            (character >= 'A'
                                    && character <= 'F');

            if (!hexadecimal) {

                throw new IllegalArgumentException(
                        "Carattere non HEX nella "
                                + "richiesta diagnostica: "
                                + character
                );
            }
        }

        return normalized;
    }
}