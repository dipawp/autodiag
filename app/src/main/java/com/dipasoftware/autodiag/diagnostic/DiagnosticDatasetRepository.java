package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDatasetRepository
 *
 * Tipo.......: Repository
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Gestisce il caricamento dei dataset diagnostici associati
 * alle ECU.
 *
 * La classe non conosce il contenuto specifico dei PID.
 *
 * Il caricamento effettivo del JSON viene delegato a
 * JsonPidRepository.
 *
 * Sono supportati:
 *
 * - STANDARD_OBD;
 * - OEM_PID;
 * - UDS_DID;
 * - futuri tipi di dataset.
 *
 * ****************************************************************************
 */
public class DiagnosticDatasetRepository {

    /**
     * Repository responsabile del parsing JSON dei PID.
     */
    @NonNull
    private final JsonPidRepository jsonPidRepository;

    /**
     * Costruttore.
     *
     * @param context context Android.
     */
    public DiagnosticDatasetRepository(
            @NonNull Context context) {

        this.jsonPidRepository =
                new JsonPidRepository(
                        context.getApplicationContext()
                );
    }

    /**
     * Carica un dataset già individuato.
     *
     * @param dataset dataset da caricare.
     *
     * @return definizione completa del dataset.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition load(
            @NonNull DiagnosticDataset dataset)
            throws IOException, JSONException {

        String filePath =
                dataset.getFilePath()
                        .trim();

        if (filePath.isEmpty()) {

            throw new IOException(
                    "Il dataset non contiene "
                            + "un filePath valido."
            );
        }

        return jsonPidRepository
                .loadDatasetFromAsset(
                        filePath
                );
    }

    /**
     * Cerca un dataset associato a una ECU
     * e lo carica direttamente.
     *
     * Il tipo viene confrontato senza distinguere
     * maiuscole e minuscole.
     *
     * Esempi:
     *
     * STANDARD_OBD
     * OEM_PID
     * UDS_DID
     *
     * @param ecu ECU contenente i dataset.
     * @param type tipo dataset.
     *
     * @return dataset caricato oppure null
     *         se il tipo non è presente.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDatasetDefinition load(
            @NonNull EcuDefinition ecu,
            @NonNull String type)
            throws IOException, JSONException {

        DiagnosticDataset dataset =
                ecu.findDataset(
                        type
                );

        if (dataset == null) {

            return null;
        }

        return load(
                dataset
        );
    }

    /**
     * Carica il dataset OBD-II standard associato
     * alla ECU.
     *
     * @param ecu ECU.
     *
     * @return dataset standard oppure null.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDatasetDefinition loadStandard(
            @NonNull EcuDefinition ecu)
            throws IOException, JSONException {

        return load(
                ecu,
                "STANDARD_OBD"
        );
    }

    /**
     * Carica il dataset PID proprietario associato
     * alla ECU.
     *
     * @param ecu ECU.
     *
     * @return dataset OEM oppure null.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDatasetDefinition loadOem(
            @NonNull EcuDefinition ecu)
            throws IOException, JSONException {

        return load(
                ecu,
                "OEM_PID"
        );
    }

    /**
     * Carica il dataset UDS/DID associato
     * alla ECU.
     *
     * @param ecu ECU.
     *
     * @return dataset UDS oppure null.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDatasetDefinition loadUds(
            @NonNull EcuDefinition ecu)
            throws IOException, JSONException {

        return load(
                ecu,
                "UDS_DID"
        );
    }

    /**
     * Cerca una definizione PID all'interno
     * di un dataset specifico.
     *
     * @param dataset dataset.
     * @param pid identificatore PID/DID.
     *
     * @return definizione trovata oppure null.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDefinition findPid(
            @NonNull DiagnosticDataset dataset,
            @NonNull String pid)
            throws IOException, JSONException {

        PidDatasetDefinition definition =
                load(
                        dataset
                );

        String normalizedPid =
                pid.trim()
                        .toUpperCase(Locale.US);

        List<PidDefinition> pids =
                definition.getPids();

        for (
                PidDefinition pidDefinition :
                pids
        ) {

            String currentPid =
                    pidDefinition.getPid()
                            .trim()
                            .toUpperCase(Locale.US);

            if (currentPid.equals(
                    normalizedPid
            )) {

                return pidDefinition;
            }
        }

        return null;
    }

    /**
     * Cerca una definizione PID/DID direttamente
     * all'interno di una ECU e di un tipo dataset.
     *
     * @param ecu ECU.
     * @param datasetType tipo dataset.
     * @param pid identificatore.
     *
     * @return definizione trovata oppure null.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public PidDefinition findPid(
            @NonNull EcuDefinition ecu,
            @NonNull String datasetType,
            @NonNull String pid)
            throws IOException, JSONException {

        DiagnosticDataset dataset =
                ecu.findDataset(
                        datasetType
                );

        if (dataset == null) {

            return null;
        }

        return findPid(
                dataset,
                pid
        );
    }

    /**
     * Verifica se un dataset contiene
     * un determinato PID/DID.
     *
     * @param dataset dataset.
     * @param pid identificatore.
     *
     * @return true se presente.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    public boolean containsPid(
            @NonNull DiagnosticDataset dataset,
            @NonNull String pid)
            throws IOException, JSONException {

        return findPid(
                dataset,
                pid
        ) != null;
    }

    /**
     * Verifica se una ECU espone un determinato
     * PID/DID in uno specifico dataset.
     *
     * @param ecu ECU.
     * @param datasetType tipo dataset.
     * @param pid identificatore.
     *
     * @return true se presente.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    public boolean containsPid(
            @NonNull EcuDefinition ecu,
            @NonNull String datasetType,
            @NonNull String pid)
            throws IOException, JSONException {

        return findPid(
                ecu,
                datasetType,
                pid
        ) != null;
    }
}