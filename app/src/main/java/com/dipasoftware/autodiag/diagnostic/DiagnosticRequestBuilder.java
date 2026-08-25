package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticRequestBuilder
 *
 * Tipo.......: Utility / Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Costruisce la richiesta diagnostica da inviare alla ECU
 * utilizzando i metadati presenti in PidDefinition.
 *
 * La classe non esegue la comunicazione.
 *
 * Regole:
 *
 * 1. Se PidDefinition contiene una request esplicita,
 *    viene utilizzata direttamente.
 *
 * 2. Se la request non è presente, viene costruita
 *    concatenando mode + pid.
 *
 * ****************************************************************************
 */
public class DiagnosticRequestBuilder {

    /**
     * Costruisce una richiesta diagnostica.
     *
     * @param definition definizione del parametro.
     *
     * @return richiesta pronta per il trasporto.
     */
    @NonNull
    public String build(
            @NonNull PidDefinition definition) {

        if (definition.hasExplicitRequest()) {

            return normalizeHex(
                    definition.getRequest()
            );
        }

        String mode =
                definition.getMode()
                        .trim();

        String pid =
                definition.getPid()
                        .trim();

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

        return normalizeHex(
                mode + pid
        );
    }

    /**
     * Normalizza una richiesta esadecimale.
     *
     * @param value richiesta.
     *
     * @return richiesta normalizzata.
     */
    @NonNull
    private String normalizeHex(
            @NonNull String value) {

        String normalized =
                value.trim()
                        .replace(
                                " ",
                                ""
                        )
                        .toUpperCase();

        if (normalized.isEmpty()) {

            throw new IllegalArgumentException(
                    "Richiesta diagnostica vuota."
            );
        }

        if ((normalized.length() % 2) != 0) {

            throw new IllegalArgumentException(
                    "Richiesta diagnostica con "
                            + "numero dispari di caratteri HEX: "
                            + normalized
            );
        }

        for (int index = 0;
             index < normalized.length();
             index++) {

            char character =
                    normalized.charAt(index);

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