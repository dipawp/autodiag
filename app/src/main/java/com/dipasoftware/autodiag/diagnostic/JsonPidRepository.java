package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: JsonPidRepository
 *
 * Tipo.......: Repository
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Carica le definizioni dei PID diagnostici dai file JSON
 * presenti nelle risorse raw dell'applicazione.
 *
 * Il repository separa il caricamento dei dati dalla logica
 * diagnostica vera e propria.
 *
 * ****************************************************************************
 */
public class JsonPidRepository {

    /**
     * Tag utilizzato per il logging.
     */
    private static final String TAG =
            "JsonPidRepository";

    /**
     * Context applicativo.
     */
    @NonNull
    private final Context context;

    /**
     * Costruttore.
     *
     * @param context context dell'applicazione.
     */
    public JsonPidRepository(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();
    }

    /**
     * Carica il dataset OBD-II standard.
     *
     * Il file viene cercato nella risorsa:
     *
     * res/raw/obd2_standard.json
     *
     * @return lista delle definizioni PID.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<PidDefinition> loadStandardPids()
            throws IOException, JSONException {

        return loadFromResource(
                com.dipasoftware.autodiag.R.raw.obd2_standard
        );
    }

    /**
     * Carica un dataset JSON da una risorsa raw.
     *
     * @param resourceId ID della risorsa raw.
     *
     * @return lista PID.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<PidDefinition> loadFromResource(
            int resourceId)
            throws IOException, JSONException {

        String json =
                readResource(resourceId);

        if (json.trim().isEmpty()) {

            throw new IOException(
                    "Il file JSON dei PID è vuoto."
            );
        }

        JSONObject root =
                new JSONObject(json);

        JSONArray pids =
                root.optJSONArray("pids");

        if (pids == null) {

            throw new JSONException(
                    "Campo 'pids' assente nel dataset JSON."
            );
        }

        List<PidDefinition> definitions =
                new ArrayList<>();

        for (int i = 0;
             i < pids.length();
             i++) {

            JSONObject pidObject =
                    pids.optJSONObject(i);

            if (pidObject == null) {

                Log.w(
                        TAG,
                        "Elemento PID non valido alla posizione "
                                + i
                );

                continue;
            }

            PidDefinition definition =
                    parsePid(pidObject);

            definitions.add(definition);
        }

        return Collections.unmodifiableList(
                definitions
        );
    }

    /**
     * Converte un JSONObject in PidDefinition.
     *
     * @param object oggetto JSON.
     *
     * @return definizione PID.
     *
     * @throws JSONException dati mancanti o non validi.
     */
    @NonNull
    private PidDefinition parsePid(
            @NonNull JSONObject object)
            throws JSONException {

        String pid =
                requireString(
                        object,
                        "pid"
                );

        String nameKey =
                requireString(
                        object,
                        "nameKey"
                );

        String descriptionKey =
                requireString(
                        object,
                        "descriptionKey"
                );

        String unit =
                object.optString(
                        "unit",
                        ""
                );

        String formula =
                requireString(
                        object,
                        "formula"
                );

        int bytes =
                object.optInt(
                        "bytes",
                        0
                );

        String mode =
                requireString(
                        object,
                        "mode"
                );

        String dataType =
                requireString(
                        object,
                        "dataType"
                );

        boolean available =
                object.optBoolean(
                        "available",
                        true
                );

        if (bytes < 0) {

            throw new JSONException(
                    "Numero byte non valido per PID "
                            + pid
            );
        }

        return new PidDefinition(
                pid,
                nameKey,
                descriptionKey,
                unit,
                formula,
                bytes,
                mode,
                dataType,
                available
        );
    }

    /**
     * Restituisce una stringa obbligatoria
     * dall'oggetto JSON.
     *
     * @param object oggetto JSON.
     * @param key chiave.
     *
     * @return valore.
     *
     * @throws JSONException valore mancante.
     */
    @NonNull
    private String requireString(
            @NonNull JSONObject object,
            @NonNull String key)
            throws JSONException {

        if (!object.has(key)) {

            throw new JSONException(
                    "Campo obbligatorio mancante: "
                            + key
            );
        }

        String value =
                object.optString(
                        key,
                        ""
                ).trim();

        if (value.isEmpty()) {

            throw new JSONException(
                    "Campo obbligatorio vuoto: "
                            + key
            );
        }

        return value;
    }

    /**
     * Legge completamente una risorsa raw.
     *
     * @param resourceId ID risorsa.
     *
     * @return contenuto testuale.
     *
     * @throws IOException errore di lettura.
     */
    @NonNull
    private String readResource(
            int resourceId)
            throws IOException {

        Resources resources =
                context.getResources();

        try (InputStream inputStream =
                     resources.openRawResource(
                             resourceId
                     );

             BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            StringBuilder content =
                    new StringBuilder();

            String line;

            while ((line = reader.readLine())
                    != null) {

                content.append(line);
                content.append('\n');
            }

            return content.toString();

        } catch (Resources.NotFoundException exception) {

            throw new IOException(
                    "Risorsa JSON non trovata: "
                            + resourceId,
                    exception
            );
        }
    }



    /**
     * Cerca una definizione PID nel dataset OBD-II standard.
     *
     * Il confronto viene effettuato sul codice PID completo,
     * ad esempio:
     *
     * 010C
     * 0105
     * 010D
     *
     * @param pid codice PID.
     *
     * @return definizione trovata oppure null.
     *
     * @throws IOException errore nella lettura del JSON.
     * @throws JSONException errore nel parsing del JSON.
     */
    @Nullable
    public PidDefinition findByPid(
            @NonNull String pid)
            throws IOException, JSONException {

        String normalizedPid =
                pid.trim().toUpperCase();

        List<PidDefinition> definitions =
                loadStandardPids();

        for (PidDefinition definition :
                definitions) {

            if (definition.getPid()
                    .equalsIgnoreCase(normalizedPid)) {

                return definition;
            }
        }

        return null;
    }
}
