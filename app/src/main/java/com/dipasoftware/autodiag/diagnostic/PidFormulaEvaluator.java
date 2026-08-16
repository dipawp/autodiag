package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: PidFormulaEvaluator
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Interpreta ed esegue le formule definite nei dataset JSON.
 *
 * Sono supportate:
 *
 * - formule matematiche;
 * - variabili A/B/C/D;
 * - numeri interi;
 * - numeri decimali;
 * - operatori + - * /;
 * - parentesi;
 * - segni unari;
 * - formula speciale BITFIELD.
 *
 * La classe NON contiene formule specifiche per singoli PID.
 *
 * ****************************************************************************
 */
public class PidFormulaEvaluator {

    /**
     * Formula speciale utilizzata dal dataset
     * per indicare dati organizzati come bitfield.
     */
    private static final String FORMULA_BITFIELD =
            "BITFIELD";

    /**
     * Valuta la formula associata alla definizione PID.
     *
     * @param definition definizione PID.
     * @param data byte grezzi ricevuti dall'ECU.
     *
     * @return valore convertito.
     *
     * @throws IllegalArgumentException formula o dati non validi.
     */
    public double evaluate(
            @NonNull PidDefinition definition,
            @NonNull byte[] data) {

        String formula =
                definition.getFormula();

        if (formula == null ||
                formula.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Formula non definita per PID "
                            + definition.getPid()
            );
        }

        String normalizedFormula =
                formula
                        .trim()
                        .toUpperCase(Locale.US);

        /*
         * BITFIELD non è una formula matematica.
         *
         * Non deve quindi essere passato
         * al FormulaParser.
         *
         * Per ora restituiamo il bitfield
         * come valore numerico unsigned.
         *
         * La decodifica dettagliata dei bit
         * verrà gestita separatamente.
         */
        if (FORMULA_BITFIELD.equals(
                normalizedFormula)) {

            return evaluateBitfield(
                    definition,
                    data
            );
        }

        /*
         * Tutte le altre formule vengono
         * interpretate dal parser matematico.
         */
        FormulaParser parser =
                new FormulaParser(
                        formula,
                        data
                );

        return parser.parse();
    }

    /**
     * Valuta una formula BITFIELD.
     *
     * Un bitfield OBD può contenere fino a
     * quattro byte:
     *
     * A B C D
     *
     * Per evitare di perdere informazione,
     * i byte vengono composti in un unico
     * valore unsigned a 32 bit.
     *
     * Esempio:
     *
     * 98 3B 00 11
     *
     * diventa:
     *
     * 0x983B0011
     *
     * NOTA:
     *
     * questo metodo NON decide ancora quali
     * PID siano supportati.
     *
     * Restituisce solamente il bitfield grezzo.
     *
     * @param definition definizione PID.
     * @param data dati grezzi.
     *
     * @return bitfield unsigned rappresentato
     *         come double.
     */
    private double evaluateBitfield(
            @NonNull PidDefinition definition,
            @NonNull byte[] data) {

        if (data.length == 0) {

            throw new IllegalArgumentException(
                    "BITFIELD senza dati per PID "
                            + definition.getPid()
            );
        }

        if (data.length > 4) {

            throw new IllegalArgumentException(
                    "BITFIELD non supportato oltre "
                            + "4 byte. Byte ricevuti: "
                            + data.length
            );
        }

        long value = 0;

        for (byte current :
                data) {

            value =
                    (value << 8)
                            | (current & 0xFFL);
        }

        return (double) value;
    }

    /**
     * Converte un byte Java signed in unsigned.
     *
     * @param value byte.
     *
     * @return valore compreso tra 0 e 255.
     */
    private static int unsignedByte(
            byte value) {

        return value & 0xFF;
    }

    /**
     * Parser matematico interno.
     *
     * Supporta:
     *
     * - A
     * - B
     * - C
     * - D
     * - numeri interi
     * - numeri decimali
     * - +
     * - -
     * - *
     * - /
     * - parentesi
     *
     * Le lettere rappresentano i byte
     * del PID.
     */
    private static class FormulaParser {

        /**
         * Formula.
         */
        @NonNull
        private final String expression;

        /**
         * Dati grezzi.
         */
        @NonNull
        private final byte[] data;

        /**
         * Posizione corrente.
         */
        private int position;

        /**
         * Costruttore.
         *
         * @param expression formula.
         * @param data byte ricevuti.
         */
        FormulaParser(
                @NonNull String expression,
                @NonNull byte[] data) {

            this.expression =
                    expression.trim();

            this.data =
                    data;

            this.position = 0;
        }

        /**
         * Avvia il parsing.
         *
         * @return risultato numerico.
         */
        double parse() {

            skipSpaces();

            if (expression.isEmpty()) {

                throw error(
                        "Formula vuota."
                );
            }

            double result =
                    parseExpression();

            skipSpaces();

            if (position <
                    expression.length()) {

                throw error(
                        "Carattere inatteso: "
                                + expression.charAt(
                                position
                        )
                );
            }

            return result;
        }

        /**
         * Gestisce addizione e sottrazione.
         *
         * @return risultato.
         */
        private double parseExpression() {

            double result =
                    parseTerm();

            while (true) {

                skipSpaces();

                if (match('+')) {

                    result +=
                            parseTerm();

                } else if (match('-')) {

                    result -=
                            parseTerm();

                } else {

                    break;
                }
            }

            return result;
        }

        /**
         * Gestisce moltiplicazione e divisione.
         *
         * @return risultato.
         */
        private double parseTerm() {

            double result =
                    parseFactor();

            while (true) {

                skipSpaces();

                if (match('*')) {

                    result *=
                            parseFactor();

                } else if (match('/')) {

                    double divisor =
                            parseFactor();

                    if (divisor == 0.0) {

                        throw error(
                                "Divisione per zero."
                        );
                    }

                    result /=
                            divisor;

                } else {

                    break;
                }
            }

            return result;
        }

        /**
         * Gestisce:
         *
         * - numeri;
         * - variabili A/B/C/D;
         * - parentesi;
         * - segno unario.
         *
         * @return valore.
         */
        private double parseFactor() {

            skipSpaces();

            /*
             * Segno positivo.
             */
            if (match('+')) {

                return parseFactor();
            }

            /*
             * Segno negativo.
             */
            if (match('-')) {

                return -parseFactor();
            }

            /*
             * Espressione tra parentesi.
             */
            if (match('(')) {

                double result =
                        parseExpression();

                skipSpaces();

                if (!match(')')) {

                    throw error(
                            "Parentesi chiusa mancante."
                    );
                }

                return result;
            }

            /*
             * Variabile byte.
             */
            if (position <
                    expression.length()) {

                char character =
                        Character.toUpperCase(
                                expression.charAt(
                                        position
                                )
                        );

                if (character >= 'A' &&
                        character <= 'D') {

                    position++;

                    return getByteValue(
                            character
                    );
                }
            }

            /*
             * Numero.
             */
            return parseNumber();
        }

        /**
         * Legge un numero dalla formula.
         *
         * Supporta:
         *
         * 100
         * 255
         * 0.5
         * 3.14
         *
         * @return numero.
         */
        private double parseNumber() {

            skipSpaces();

            int start =
                    position;

            boolean decimalPointFound =
                    false;

            while (position <
                    expression.length()) {

                char character =
                        expression.charAt(
                                position
                        );

                if (Character.isDigit(
                        character)) {

                    position++;

                    continue;
                }

                if (character == '.' &&
                        !decimalPointFound) {

                    decimalPointFound =
                            true;

                    position++;

                    continue;
                }

                break;
            }

            if (start == position) {

                throw error(
                        "Numero atteso."
                );
            }

            String number =
                    expression.substring(
                            start,
                            position
                    );

            try {

                return Double.parseDouble(
                        number
                );

            } catch (NumberFormatException exception) {

                throw error(
                        "Numero non valido: "
                                + number
                );
            }
        }

        /**
         * Restituisce il valore del byte
         * associato alla variabile.
         *
         * A = data[0]
         * B = data[1]
         * C = data[2]
         * D = data[3]
         *
         * @param variable variabile.
         *
         * @return valore unsigned.
         */
        private double getByteValue(
                char variable) {

            int index =
                    variable - 'A';

            if (index < 0 ||
                    index >= data.length) {

                throw error(
                        "Byte "
                                + variable
                                + " non disponibile. "
                                + "Byte ricevuti: "
                                + data.length
                );
            }

            return unsignedByte(
                    data[index]
            );
        }

        /**
         * Verifica e consuma un carattere.
         *
         * @param expected carattere.
         *
         * @return true se trovato.
         */
        private boolean match(
                char expected) {

            if (position <
                    expression.length()
                    &&
                    expression.charAt(
                            position
                    ) == expected) {

                position++;

                return true;
            }

            return false;
        }

        /**
         * Ignora gli spazi.
         */
        private void skipSpaces() {

            while (position <
                    expression.length()
                    &&
                    Character.isWhitespace(
                            expression.charAt(
                                    position
                            )
                    )) {

                position++;
            }
        }

        /**
         * Crea un errore con la posizione
         * nella formula.
         *
         * @param message messaggio.
         *
         * @return eccezione.
         */
        @NonNull
        private IllegalArgumentException error(
                @NonNull String message) {

            return new IllegalArgumentException(
                    String.format(
                            Locale.US,
                            "%s Posizione: %d. Formula: %s",
                            message,
                            position,
                            expression
                    )
            );
        }
    }
}