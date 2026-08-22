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
     * Tipo di risultato prodotto dalla decodifica di un PID.
     */
    public enum ResultType {
        NUMERIC,
        MULTI_VALUE
    }


    /**
     * Risultato generico della decodifica di un PID.
     *
     * Permette di mantenere compatibile evaluate(), che restituisce
     * un double, ma consente ai nuovi decoder di restituire più valori.
     */
    public static class EvaluationResult {

        @NonNull
        private final ResultType type;

        private final double numericValue;

        @NonNull
        private final java.util.Map<String, Double> values;

        private EvaluationResult(
                @NonNull ResultType type,
                double numericValue,
                @NonNull java.util.Map<String, Double> values) {

            this.type = type;
            this.numericValue = numericValue;
            this.values = values;
        }

        @NonNull
        public static EvaluationResult numeric(
                double value) {

            return new EvaluationResult(
                    ResultType.NUMERIC,
                    value,
                    java.util.Collections.emptyMap()
            );
        }

        @NonNull
        public static EvaluationResult multiValue(
                @NonNull java.util.Map<String, Double> values) {

            return new EvaluationResult(
                    ResultType.MULTI_VALUE,
                    0.0,
                    new java.util.LinkedHashMap<>(values)
            );
        }

        @NonNull
        public ResultType getType() {
            return type;
        }

        public boolean isNumeric() {
            return type == ResultType.NUMERIC;
        }

        public boolean isMultiValue() {
            return type == ResultType.MULTI_VALUE;
        }

        public double getNumericValue() {
            if (!isNumeric()) {
                throw new IllegalStateException(
                        "Il risultato non è numerico."
                );
            }

            return numericValue;
        }

        @NonNull
        public java.util.Map<String, Double> getValues() {
            return values;
        }
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
     * - signed16(A,B)
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
         * - segno unario;
         * - funzioni speciali.
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
             * Funzione signed16(A,B).
             */
            if (startsWithIdentifier("signed16")) {

                return parseSigned16Function();
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
         * Verifica se nella posizione corrente
         * inizia l'identificatore indicato.
         *
         * @param identifier identificatore.
         *
         * @return true se presente.
         */
        private boolean startsWithIdentifier(
                @NonNull String identifier) {

            skipSpaces();

            int length =
                    identifier.length();

            if (position + length >
                    expression.length()) {

                return false;
            }

            return expression.regionMatches(
                    true,
                    position,
                    identifier,
                    0,
                    length
            );
        }

        /**
         * Valuta una funzione signed16(A,B).
         *
         * Interpreta A e B come intero signed
         * a 16 bit in formato big-endian.
         *
         * Esempio:
         *
         * signed16(A,B)
         *
         * @return valore signed.
         */
        private double parseSigned16Function() {

            int start =
                    position;

            if (!matchIdentifier(
                    "signed16")) {

                position =
                        start;

                throw error(
                        "Funzione signed16 attesa."
                );
            }

            skipSpaces();

            if (!match('(')) {

                throw error(
                        "Parentesi aperta attesa "
                                + "dopo signed16."
                );
            }

            skipSpaces();

            if (position >=
                    expression.length()
                    ||
                    Character.toUpperCase(
                            expression.charAt(
                                    position
                            )
                    ) != 'A') {

                throw error(
                        "signed16 richiede "
                                + "il byte A."
                );
            }

            position++;

            skipSpaces();

            if (!match(',')) {

                throw error(
                        "Virgola attesa "
                                + "in signed16(A,B)."
                );
            }

            skipSpaces();

            if (position >=
                    expression.length()
                    ||
                    Character.toUpperCase(
                            expression.charAt(
                                    position
                            )
                    ) != 'B') {

                throw error(
                        "signed16 richiede "
                                + "il byte B."
                );
            }

            position++;

            skipSpaces();

            if (!match(')')) {

                throw error(
                        "Parentesi chiusa attesa "
                                + "dopo signed16(A,B)."
                );
            }

            if (data.length < 2) {

                throw error(
                        "signed16 richiede "
                                + "almeno 2 byte."
                );
            }

            int high =
                    unsignedByte(
                            data[0]
                    );

            int low =
                    unsignedByte(
                            data[1]
                    );

            int value =
                    (high << 8) | low;

            if ((value & 0x8000) != 0) {

                value -=
                        0x10000;
            }

            return value;
        }

        /**
         * Verifica la presenza di un identificatore
         * e lo consuma.
         *
         * @param identifier identificatore.
         *
         * @return true se trovato.
         */
        private boolean matchIdentifier(
                @NonNull String identifier) {

            skipSpaces();

            int length =
                    identifier.length();

            if (position + length >
                    expression.length()) {

                return false;
            }

            String candidate =
                    expression.substring(
                            position,
                            position + length
                    );

            if (!identifier.equalsIgnoreCase(
                    candidate)) {

                return false;
            }

            position +=
                    length;

            return true;
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
         * Restituisce il valore unsigned
         * del byte associato alla variabile.
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
         * Converte un byte Java signed
         * nel relativo valore unsigned.
         *
         * @param value byte.
         *
         * @return valore 0..255.
         */
        private int unsignedByte(
                byte value) {

            return value & 0xFF;
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


    /**
     * Valuta un PID restituendo un risultato generico.
     *
     * I PID FORMULA e BITFIELD producono un risultato numerico.
     * I PID MULTI_VALUE possono produrre più valori nominati.
     *
     * @param definition definizione PID.
     * @param data byte ricevuti dall'ECU.
     *
     * @return risultato della decodifica.
     */
    @NonNull
    public EvaluationResult evaluateResult(
            @NonNull PidDefinition definition,
            @NonNull byte[] data) {

        String decoder =
                definition.getDecoder();

        if (decoder == null) {
            throw new IllegalArgumentException(
                    "Decoder non definito per PID "
                            + definition.getPid()
            );
        }

        String normalizedDecoder =
                decoder.trim().toUpperCase(Locale.US);

        switch (normalizedDecoder) {

            case "FORMULA":
            case "SIGNED_FORMULA":
            case "BITFIELD":

                return EvaluationResult.numeric(
                        evaluate(definition, data)
                );

            case "MULTI_VALUE":

                return evaluateMultiValue(
                        definition,
                        data
                );

            default:

                throw new IllegalArgumentException(
                        "Decoder non supportato da "
                                + "PidFormulaEvaluator: "
                                + decoder
                                + " per PID "
                                + definition.getPid()
                );
        }
    }


    /**
     * Valuta una formula MULTI_VALUE.
     *
     * La formula nel JSON può contenere più espressioni
     * separate da ';', ad esempio:
     *
     * voltage=A/200;trim=(B*100/128)-100
     *
     * oppure:
     *
     * lambda=((A*256)+B)*2/65536;voltage=((C*256)+D)*8/65536
     *
     * Ogni espressione deve avere la forma:
     *
     * nome=espressione
     *
     * @param definition definizione PID.
     * @param data byte grezzi ricevuti dall'ECU.
     *
     * @return risultato contenente i valori nominati.
     *
     * @throws IllegalArgumentException formula non valida.
     */
    @NonNull
    private EvaluationResult evaluateMultiValue(
            @NonNull PidDefinition definition,
            @NonNull byte[] data) {

        String formula = definition.getFormula();

        if (formula == null ||
                formula.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Formula MULTI_VALUE non definita per PID "
                            + definition.getPid()
            );
        }

        String[] expressions =
                formula.split(";");

        java.util.Map<String, Double> values =
                new java.util.LinkedHashMap<>();

        for (String expression :
                expressions) {

            String item =
                    expression.trim();

            if (item.isEmpty()) {
                continue;
            }

            int separator =
                    item.indexOf('=');

            if (separator <= 0 ||
                    separator >= item.length() - 1) {

                throw new IllegalArgumentException(
                        "Espressione MULTI_VALUE non valida "
                                + "per PID "
                                + definition.getPid()
                                + ": "
                                + item
                );
            }

            String name =
                    item.substring(
                            0,
                            separator
                    ).trim();

            String valueExpression =
                    item.substring(
                            separator + 1
                    ).trim();

            if (name.isEmpty()) {

                throw new IllegalArgumentException(
                        "Nome valore MULTI_VALUE vuoto "
                                + "per PID "
                                + definition.getPid()
                );
            }

            if (valueExpression.isEmpty()) {

                throw new IllegalArgumentException(
                        "Espressione MULTI_VALUE vuota "
                                + "per valore "
                                + name
                                + " del PID "
                                + definition.getPid()
                );
            }

            FormulaParser parser =
                    new FormulaParser(
                            valueExpression,
                            data
                    );

            double value =
                    parser.parse();

            values.put(
                    name,
                    value
            );
        }

        if (values.isEmpty()) {

            throw new IllegalArgumentException(
                    "Nessun valore MULTI_VALUE definito "
                            + "per PID "
                            + definition.getPid()
            );
        }

        return EvaluationResult.multiValue(
                values
        );
    }




}