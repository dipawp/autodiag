package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository dei cataloghi PID OEM.
 *
 * Responsabilità:
 *
 * - individuare i dataset OEM presenti negli assets;
 * - caricare un dataset OEM tramite JsonPidRepository;
 * - mantenere separato il catalogo OEM dal catalogo OBD-II standard.
 *
 * Struttura prevista:
 *
 * assets/
 *     pids/
 *         oem/
 *             fiat/
 *             alfa_romeo/
 *             lancia/
 *
 * Il repository NON interpreta direttamente i PID.
 * Il parsing rimane responsabilità di JsonPidRepository.
 */
public class OemPidRepository {

    /**
     * Directory principale dei dataset OEM.
     */
    private static final String OEM_ROOT =
            "pids/oem";

    /**
     * Context Android.
     */
    @NonNull
    private final Context context;

    /**
     * Repository JSON sottostante.
     */
    @NonNull
    private final JsonPidRepository jsonRepository;

    /**
     * Asset manager.
     */
    @NonNull
    private final AssetManager assetManager;

    /**
     * Costruttore.
     *
     * @param context context Android.
     */
    public OemPidRepository(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();

        this.jsonRepository =
                new JsonPidRepository(
                        this.context
                );

        this.assetManager =
                this.context.getAssets();
    }

    /**
     * Restituisce le marche OEM disponibili.
     *
     * Esempio:
     *
     * [
     *     "fiat",
     *     "alfa_romeo",
     *     "lancia"
     * ]
     *
     * @return elenco ordinato delle marche.
     *
     * @throws IOException errore accesso assets.
     */
    @NonNull
    public List<String> getManufacturers()
            throws IOException {

        return listDirectories(
                OEM_ROOT
        );
    }

    /**
     * Restituisce i dataset presenti
     * per una determinata marca.
     *
     * Esempio:
     *
     * fiat/
     *     panda/
     *     punto/
     *     giulietta/
     *
     * @param manufacturer marca.
     *
     * @return elenco modelli/directory.
     *
     * @throws IOException errore accesso assets.
     */
    @NonNull
    public List<String> getModels(
            @NonNull String manufacturer)
            throws IOException {

        String path =
                OEM_ROOT
                        + "/"
                        + normalizePath(
                        manufacturer
                );

        return listDirectories(
                path
        );
    }

    /**
     * Restituisce le ECU presenti
     * per una determinata marca e modello.
     *
     * @param manufacturer marca.
     * @param model modello.
     *
     * @return elenco ECU.
     *
     * @throws IOException errore accesso assets.
     */
    @NonNull
    public List<String> getEcus(
            @NonNull String manufacturer,
            @NonNull String model)
            throws IOException {

        String path =
                OEM_ROOT
                        + "/"
                        + normalizePath(
                        manufacturer
                )
                        + "/"
                        + normalizePath(
                        model
                );

        return listDirectories(
                path
        );
    }

    /**
     * Carica il dataset PID di una ECU.
     *
     * Struttura prevista:
     *
     * pids/oem/
     *     fiat/
     *         modello/
     *             ecu/
     *                 pids.json
     *
     * @param manufacturer marca.
     * @param model modello.
     * @param ecu ECU.
     *
     * @return dataset PID.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadDataset(
            @NonNull String manufacturer,
            @NonNull String model,
            @NonNull String ecu)
            throws IOException, JSONException {

        String assetPath =
                OEM_ROOT
                        + "/"
                        + normalizePath(
                        manufacturer
                )
                        + "/"
                        + normalizePath(
                        model
                )
                        + "/"
                        + normalizePath(
                        ecu
                )
                        + "/pids.json";

        return jsonRepository
                .loadDatasetFromAsset(
                        assetPath
                );
    }

    /**
     * Verifica se esiste un dataset
     * per la combinazione indicata.
     *
     * @param manufacturer marca.
     * @param model modello.
     * @param ecu ECU.
     *
     * @return true se il dataset esiste.
     */
    public boolean datasetExists(
            @NonNull String manufacturer,
            @NonNull String model,
            @NonNull String ecu) {

        String assetPath =
                OEM_ROOT
                        + "/"
                        + normalizePath(
                        manufacturer
                )
                        + "/"
                        + normalizePath(
                        model
                )
                        + "/"
                        + normalizePath(
                        ecu
                )
                        + "/pids.json";

        try {

            assetManager.open(
                    assetPath
            ).close();

            return true;

        } catch (IOException exception) {

            return false;
        }
    }

    /**
     * Elenca le directory presenti
     * all'interno di un percorso assets.
     *
     * @param path percorso.
     *
     * @return directory trovate.
     *
     * @throws IOException errore accesso assets.
     */
    @NonNull
    private List<String> listDirectories(
            @NonNull String path)
            throws IOException {

        String[] entries =
                assetManager.list(
                        path
                );

        if (entries == null ||
                entries.length == 0) {

            return Collections.emptyList();
        }

        List<String> directories =
                new ArrayList<>();

        for (String entry : entries) {

            if (entry == null ||
                    entry.trim().isEmpty()) {

                continue;
            }

            String childPath =
                    path
                            + "/"
                            + entry;

            String[] children =
                    assetManager.list(
                            childPath
                    );

            /*
             * Una directory assets normalmente
             * restituisce il proprio contenuto.
             *
             * Un file JSON invece non viene
             * trattato come directory.
             */
            if (children != null) {

                directories.add(
                        entry
                );
            }
        }

        Collections.sort(
                directories,
                String.CASE_INSENSITIVE_ORDER
        );

        return directories;
    }

    /**
     * Normalizza un componente del percorso.
     *
     * Evita slash iniziali/finali e impedisce
     * di costruire accidentalmente percorsi
     * fuori dalla directory prevista.
     *
     * @param value valore.
     *
     * @return valore normalizzato.
     */
    @NonNull
    private String normalizePath(
            @NonNull String value) {

        String result =
                value.trim()
                        .replace(
                                '\\',
                                '/'
                        );

        while (result.startsWith("/")) {

            result =
                    result.substring(1);
        }

        while (result.endsWith("/")) {

            result =
                    result.substring(
                            0,
                            result.length() - 1
                    );
        }

        if (result.contains("..")) {

            throw new IllegalArgumentException(
                    "Percorso OEM non valido: "
                            + value
            );
        }

        return result;
    }
}