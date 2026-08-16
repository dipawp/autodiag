package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

/**
 * ****************************************************************************
 *
 * Classe.....: PidFormulaEvaluatorTest
 *
 * Tipo.......: Classe di test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Verifica che le formule dei PID vengano:
 *
 * - caricate dal dataset JSON;
 * - associate correttamente a PidDefinition;
 * - interpretate da PidFormulaEvaluator;
 * - applicate ai byte ricevuti dall'ECU.
 *
 * IMPORTANTE:
 *
 * Questo test NON contiene le formule dei PID nella logica.
 *
 * Le formule utilizzate vengono lette direttamente
 * dalle definizioni caricate dal JSON.
 *
 * ****************************************************************************
 */
public class PidFormulaEvaluatorTest {

    /**
     * Repository PID.
     */
    @NonNull
    private final JsonPidRepository repository;

    /**
     * Evaluator delle formule.
     */
    @NonNull
    private final PidFormulaEvaluator evaluator;

    /**
     * Costruttore.
     *
     * @param context context applicazione.
     */
    public PidFormulaEvaluatorTest(
            @NonNull Context context) {

        repository =
                new JsonPidRepository(context);

        evaluator =
                new PidFormulaEvaluator();
    }

    /**
     * Esegue il test delle formule dei PID standard.
     *
     * @return risultato formattato.
     */
    @NonNull
    public String runTest() {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "=== PID FORMULA TEST ===\n\n"
        );

        try {

            List<PidDefinition> pids =
                    repository.loadStandardPids();

            if (pids.isEmpty()) {

                result.append(
                        "ERRORE:\n"
                                + "Nessun PID caricato dal JSON."
                );

                return result.toString();
            }

            result.append(
                    "DATASET: OBD-II STANDARD\n"
            );

            result.append(
                    "PID CARICATI: "
            );

            result.append(
                    pids.size()
            );

            result.append(
                    "\n\n"
            );

            /*
             * Eseguiamo alcuni PID standard
             * per verificare che la formula
             * venga realmente letta dal JSON.
             */
            testPid(
                    result,
                    pids,
                    "010C",
                    new byte[]{
                            (byte) 0x1A,
                            (byte) 0xF8
                    },
                    "rpm"
            );

            testPid(
                    result,
                    pids,
                    "0105",
                    new byte[]{
                            (byte) 0x5A
                    },
                    "°C"
            );

            testPid(
                    result,
                    pids,
                    "010D",
                    new byte[]{
                            (byte) 0x64
                    },
                    "km/h"
            );

            testPid(
                    result,
                    pids,
                    "0104",
                    new byte[]{
                            (byte) 0x80
                    },
                    "%"
            );

            testPid(
                    result,
                    pids,
                    "010B",
                    new byte[]{
                            (byte) 0x64
                    },
                    "kPa"
            );

            result.append(
                    "\n=== TEST COMPLETATO ==="
            );

        } catch (IOException exception) {

            result.append(
                    "ERRORE LETTURA JSON\n\n"
            );

            result.append(
                    exception.getMessage()
            );

        } catch (JSONException exception) {

            result.append(
                    "ERRORE JSON\n\n"
            );

            result.append(
                    exception.getMessage()
            );

        } catch (RuntimeException exception) {

            result.append(
                    "ERRORE\n\n"
            );

            result.append(
                    exception.getMessage()
            );
        }

        return result.toString();
    }

    /**
     * Esegue il test di un singolo PID.
     *
     * La formula NON viene specificata qui.
     *
     * Viene recuperata direttamente da PidDefinition,
     * che a sua volta è stata caricata dal JSON.
     *
     * @param result buffer risultato.
     * @param pids lista PID.
     * @param pidCode codice PID.
     * @param data byte simulati ECU.
     * @param expectedUnit unità attesa.
     */
    private void testPid(
            @NonNull StringBuilder result,
            @NonNull List<PidDefinition> pids,
            @NonNull String pidCode,
            @NonNull byte[] data,
            @NonNull String expectedUnit) {

        PidDefinition definition =
                findPid(
                        pids,
                        pidCode
                );

        result.append(
                pidCode
        );

        result.append(
                " | "
        );

        if (definition == null) {

            result.append(
                    "ERRORE: PID non trovato\n"
            );

            return;
        }

        result.append(
                definition.getNameKey()
        );

        result.append(
                " | "
        );

        result.append(
                "FORMULA JSON: "
        );

        String formula =
                definition.getFormula();

        if (formula == null ||
                formula.trim().isEmpty()) {

            result.append(
                    "(nessuna)"
            );

            result.append(
                    "\n"
            );

            return;
        }

        result.append(
                formula
        );

        result.append(
                "\n"
        );

        result.append(
                "BYTE: "
        );

        appendBytes(
                result,
                data
        );

        result.append(
                "\n"
        );

        try {

            double value =
                    evaluator.evaluate(
                            definition,
                            data
                    );

            result.append(
                    "VALORE: "
            );

            result.append(
                    formatValue(value)
            );

            result.append(
                    " "
            );

            result.append(
                    definition.getUnit()
            );

            result.append(
                    "\n"
            );

            /*
             * Verifica solamente che l'unità
             * della definizione corrisponda
             * a quella prevista dal test.
             *
             * Il valore numerico viene prodotto
             * esclusivamente dalla formula JSON.
             */
            if (!expectedUnit.equals(
                    definition.getUnit())) {

                result.append(
                        "ATTENZIONE: unità inattesa. "
                                + "Attesa: "
                                + expectedUnit
                                + ", ricevuta: "
                                + definition.getUnit()
                );

                result.append(
                        "\n"
                );
            }

        } catch (IllegalArgumentException exception) {

            result.append(
                    "ERRORE EVALUATION: "
            );

            result.append(
                    exception.getMessage()
            );

            result.append(
                    "\n"
            );
        }
    }

    /**
     * Cerca un PID nella lista.
     *
     * @param pids lista PID.
     * @param pidCode codice PID.
     *
     * @return definizione oppure null.
     */
    private PidDefinition findPid(
            @NonNull List<PidDefinition> pids,
            @NonNull String pidCode) {

        for (PidDefinition pid :
                pids) {

            if (pidCode.equalsIgnoreCase(
                    pid.getPid()
            )) {

                return pid;
            }
        }

        return null;
    }

    /**
     * Visualizza i byte in formato esadecimale.
     *
     * @param result buffer.
     * @param data dati.
     */
    private void appendBytes(
            @NonNull StringBuilder result,
            @NonNull byte[] data) {

        for (int index = 0;
             index < data.length;
             index++) {

            if (index > 0) {

                result.append(
                        " "
                );
            }

            result.append(
                    String.format(
                            java.util.Locale.US,
                            "%02X",
                            data[index] & 0xFF
                    )
            );
        }
    }

    /**
     * Formatta il valore numerico.
     *
     * Se il valore è intero viene mostrato
     * senza decimali.
     *
     * @param value valore.
     *
     * @return valore formattato.
     */
    @NonNull
    private String formatValue(
            double value) {

        if (value == Math.rint(value)) {

            return String.format(
                    java.util.Locale.US,
                    "%.0f",
                    value
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }
}