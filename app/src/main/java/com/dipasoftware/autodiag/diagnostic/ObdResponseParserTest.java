package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: ObdResponseParserTest
 *
 * Tipo.......: Classe di test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica il corretto funzionamento di ObdResponseParser
 * utilizzando risposte OBD-II simulate.
 *
 * Il test NON comunica con l'ELM327.
 *
 * Verifica:
 *
 * - parsing di una risposta positiva;
 * - riconoscimento del PID;
 * - estrazione dei byte dati;
 * - gestione di CR/LF e prompt ELM327;
 * - rifiuto di risposte non valide;
 * - gestione di PID errati;
 * - gestione di risposte senza dati.
 *
 * ****************************************************************************
 */
public class ObdResponseParserTest {

    /**
     * Parser utilizzato dal test.
     */
    @NonNull
    private final ObdResponseParser parser =
            new ObdResponseParser();

    /**
     * Esegue tutti i test.
     *
     * @return risultato formattato.
     */
    @NonNull
    public String runTest() {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "=== OBD RESPONSE PARSER TEST ===\n\n"
        );

        int passed = 0;
        int failed = 0;

        /*
         * Test 1:
         *
         * 010C -> 41 0C 1A F8
         *
         * Dati:
         *
         * 1A F8
         */
        if (testPositiveResponse(
                result,
                "010C",
                "41 0C 1A F8",
                "1A F8"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 2:
         *
         * 010D -> 41 0D 64
         */
        if (testPositiveResponse(
                result,
                "010D",
                "41 0D 64",
                "64"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 3:
         *
         * Risposta ELM327 con CR/LF/prompt.
         */
        if (testPositiveResponse(
                result,
                "010C",
                "41 0C 1A F8\r\r>",
                "1A F8"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 4:
         *
         * Risposta con newline.
         */
        if (testPositiveResponse(
                result,
                "0105",
                "\r\n41 05 5A\r\n>",
                "5A"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 5:
         *
         * Risposta con PID errato.
         *
         * Richiesto 010C.
         * Ricevuto 0D.
         */
        if (testExpectedFailure(
                result,
                "010C",
                "41 0D 64",
                "PID errato"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 6:
         *
         * Risposta non positiva.
         */
        if (testExpectedFailure(
                result,
                "010C",
                "CAN ERROR",
                "Risposta non valida"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 7:
         *
         * Risposta vuota.
         */
        if (testExpectedFailure(
                result,
                "010C",
                "",
                "Risposta vuota"
        )) {

            passed++;

        } else {

            failed++;
        }

        /*
         * Test 8:
         *
         * Risposta positiva ma senza dati.
         */
        if (testExpectedFailure(
                result,
                "010C",
                "41 0C",
                "Dati mancanti"
        )) {

            passed++;

        } else {

            failed++;
        }

        result.append(
                "\n"
        );

        result.append(
                "TEST PASSATI: "
        );

        result.append(
                passed
        );

        result.append(
                "\n"
        );

        result.append(
                "TEST FALLITI: "
        );

        result.append(
                failed
        );

        result.append(
                "\n\n"
        );

        if (failed == 0) {

            result.append(
                    "=== TEST COMPLETATO: OK ==="
            );

        } else {

            result.append(
                    "=== TEST COMPLETATO: ERRORI ==="
            );
        }

        return result.toString();
    }

    /**
     * Verifica una risposta positiva.
     *
     * @param result buffer risultato.
     * @param pid PID richiesto.
     * @param response risposta simulata.
     * @param expectedData dati attesi.
     *
     * @return true se il test passa.
     */
    private boolean testPositiveResponse(
            @NonNull StringBuilder result,
            @NonNull String pid,
            @NonNull String response,
            @NonNull String expectedData) {

        try {

            ObdResponseParser.ObdResponse parsed =
                    parser.parse(
                            response,
                            pid
                    );

            String actualData =
                    parsed.getDataHex();

            boolean success =
                    expectedData.equalsIgnoreCase(
                            actualData
                    );

            result.append(
                    "TEST POSITIVO | "
            );

            result.append(
                    pid
            );

            result.append(
                    " | RX: "
            );

            result.append(
                    response
                            .replace(
                                    "\r",
                                    "\\r"
                            )
                            .replace(
                                    "\n",
                                    "\\n"
                            )
            );

            result.append(
                    "\n"
            );

            result.append(
                    "  ATTESO: "
            );

            result.append(
                    expectedData
            );

            result.append(
                    "\n"
            );

            result.append(
                    "  RICEVUTO: "
            );

            result.append(
                    actualData
            );

            result.append(
                    "\n"
            );

            result.append(
                    success
                            ? "  RISULTATO: OK\n\n"
                            : "  RISULTATO: ERRORE\n\n"
            );

            return success;

        } catch (RuntimeException exception) {

            result.append(
                    "TEST POSITIVO | "
            );

            result.append(
                    pid
            );

            result.append(
                    " | ERRORE: "
            );

            result.append(
                    exception.getMessage()
            );

            result.append(
                    "\n\n"
            );

            return false;
        }
    }

    /**
     * Verifica che una risposta venga rifiutata.
     *
     * @param result buffer.
     * @param pid PID richiesto.
     * @param response risposta.
     * @param description descrizione test.
     *
     * @return true se la risposta viene correttamente rifiutata.
     */
    private boolean testExpectedFailure(
            @NonNull StringBuilder result,
            @NonNull String pid,
            @NonNull String response,
            @NonNull String description) {

        try {

            parser.parse(
                    response,
                    pid
            );

            result.append(
                    "TEST NEGATIVO | "
            );

            result.append(
                    description
            );

            result.append(
                    " | ERRORE: risposta accettata "
                            + "ma doveva essere rifiutata.\n\n"
            );

            return false;

        } catch (IllegalArgumentException exception) {

            result.append(
                    "TEST NEGATIVO | "
            );

            result.append(
                    description
            );

            result.append(
                    " | OK\n"
            );

            result.append(
                    "  Motivo: "
            );

            result.append(
                    exception.getMessage()
            );

            result.append(
                    "\n\n"
            );

            return true;
        }
    }
}
