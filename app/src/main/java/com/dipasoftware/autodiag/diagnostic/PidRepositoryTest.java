package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

/**
 * ****************************************************************************
 *
 * Classe.....: PidRepositoryTest
 *
 * Tipo.......: Classe di test
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Esegue un test del caricamento del database PID.
 *
 * Verifica:
 *
 * - caricamento del file JSON;
 * - parsing del JSON;
 * - creazione degli oggetti PidDefinition;
 * - numero di PID caricati;
 * - contenuto delle definizioni.
 *
 * Questa classe non comunica con l'ELM327.
 *
 * ****************************************************************************
 */
public class PidRepositoryTest {

    /**
     * Repository utilizzato per il test.
     */
    @NonNull
    private final JsonPidRepository repository;

    /**
     * Costruttore.
     *
     * @param context context dell'applicazione.
     */
    public PidRepositoryTest(
            @NonNull Context context) {

        repository =
                new JsonPidRepository(context);
    }

    /**
     * Esegue il test del database PID standard.
     *
     * @return risultato formattato del test.
     */
    @NonNull
    public String runTest() {

        StringBuilder result =
                new StringBuilder();

        result.append(
                "=== PID DATABASE TEST ===\n\n"
        );

        try {

            List<PidDefinition> pids =
                    repository.loadStandardPids();

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

            if (pids.isEmpty()) {

                result.append(
                        "ATTENZIONE:\n"
                );

                result.append(
                        "Nessun PID presente nel dataset.\n"
                );

                return result.toString();
            }

            for (PidDefinition pid :
                    pids) {

                result.append(
                        pid.getPid()
                );

                result.append(
                        " | "
                );

                result.append(
                        pid.getNameKey()
                );

                result.append(
                        " | "
                );

                result.append(
                        pid.getDataType()
                );

                result.append(
                        " | "
                );

                result.append(
                        pid.getUnit()
                );

                result.append(
                        "\n"
                );
            }

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
}
