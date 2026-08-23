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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import java.io.File;
import java.util.Arrays;

/**
 * ------------------------------------------------------------
 *
 * Classe.....: JsonPidRepository
 *
 * Tipo.......: Repository
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Repository responsabile del caricamento del catalogo
 * standard OBD-II dai file JSON presenti nelle risorse
 * raw dell'applicazione.
 *
 * Il repository converte gli oggetti JSON in oggetti
 * PidDefinition.
 *
 * Il repository NON determina se un PID è supportato
 * dall'ECU attualmente collegata.
 *
 * La presenza di un PID nel catalogo indica solamente
 * che il PID è conosciuto dall'applicazione.
 *
 * Il supporto reale dell'ECU viene determinato dalla
 * logica diagnostica tramite le bitmap dei PID supportati.
 *
 * ------------------------------------------------------------
 */
public class JsonPidRepository {

    /**
     * Tag utilizzato per identificare i messaggi di log
     * prodotti dal repository.
     */
    private static final String TAG =
            "JsonPidRepository";

    /**
     * Context applicativo utilizzato per accedere
     * alle risorse raw contenenti i cataloghi JSON.
     */
    @NonNull
    private final Context context;

    /**
     * Costruisce il repository utilizzando il Context
     * dell'applicazione.
     *
     * @param context context Android utilizzato per accedere
     *                alle risorse dell'applicazione.
     */
    public JsonPidRepository(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();
    }

    /**
     * Carica il catalogo standard OBD-II dalla risorsa
     * JSON principale dell'applicazione.
     *
     * La risorsa utilizzata è:
     *
     * res/raw/obd2_standard.json
     *
     * @return lista immutabile delle definizioni PID.
     *
     * @throws IOException errore durante la lettura
     *                     della risorsa.
     *
     * @throws JSONException JSON non valido o struttura
     *                       del catalogo non corretta.
     */
    @NonNull
    public List<PidDefinition> loadStandardPids()
            throws IOException, JSONException {

        return loadFromResource(
                com.dipasoftware.autodiag.R.raw.obd2_standard
        );
    }

    /**
     * Carica un catalogo PID da una risorsa raw.
     *
     * La struttura JSON attesa è:
     *
     * {
     *     "version": "...",
     *     "standard": "...",
     *     "mode": "...",
     *     "description": "...",
     *     "pids": [
     *         { ... }
     *     ]
     * }
     *
     * @param resourceId identificatore della risorsa raw
     *                   contenente il catalogo JSON.
     *
     * @return lista immutabile delle definizioni PID.
     *
     * @throws IOException errore durante la lettura
     *                     della risorsa.
     *
     * @throws JSONException JSON non valido o PID non valido.
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
     * Converte un singolo JSONObject contenente la
     * definizione di un PID in un oggetto PidDefinition.
     *
     * I campi letti dal JSON sono:
     *
     * pid
     * nameKey
     * descriptionKey
     * unit
     * decoder
     * formula
     * bytes
     * mode
     * dataType
     *
     * Il campo "available", se presente nel JSON,
     * viene volutamente ignorato perché la disponibilità
     * reale del PID dipende dall'ECU collegata e non dal
     * catalogo standard.
     *
     * @param object oggetto JSON contenente la definizione
     *               del PID.
     *
     * @return definizione PID convertita dal JSON.
     *
     * @throws JSONException dati obbligatori mancanti
     *                       o non validi.
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
                ).trim();

        String decoder =
                requireString(
                        object,
                        "decoder"
                );

        String formula =
                object.optString(
                        "formula",
                        ""
                ).trim();

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


        String source =
                object.optString(
                        "source",
                        "STANDARD"
                ).trim().toUpperCase(Locale.US);

        if (bytes < 0) {

            throw new JSONException(
                    "Numero byte non valido per PID "
                            + pid
            );
        }

        /*
         * Il campo "available" eventualmente presente
         * nel JSON non viene utilizzato.
         *
         * La disponibilità del PID viene determinata
         * successivamente tramite le bitmap restituite
         * dalla ECU.
         */

        return new PidDefinition(
                pid,
                nameKey,
                descriptionKey,
                unit,
                decoder,
                formula,
                bytes,
                mode,
                dataType,
                source
        );
    }

    /**
     * Restituisce una stringa obbligatoria contenuta
     * nell'oggetto JSON.
     *
     * Il metodo verifica sia la presenza del campo
     * sia che il valore non sia vuoto.
     *
     * @param object oggetto JSON da cui leggere il valore.
     *
     * @param key nome del campo obbligatorio.
     *
     * @return valore testuale normalizzato.
     *
     * @throws JSONException campo assente o vuoto.
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
     * Legge completamente il contenuto testuale di
     * una risorsa raw.
     *
     * La lettura viene effettuata utilizzando UTF-8,
     * coerentemente con il formato dei file JSON
     * dell'applicazione.
     *
     * @param resourceId identificatore della risorsa raw.
     *
     * @return contenuto testuale della risorsa.
     *
     * @throws IOException errore durante l'apertura
     *                     o la lettura della risorsa.
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
     * Cerca una definizione PID nel catalogo standard
     * utilizzando il codice PID completo.
     *
     * Esempi:
     *
     * 010C
     * 0105
     * 010D
     *
     * Il confronto è case-insensitive e viene eseguito
     * dopo la normalizzazione del codice.
     *
     * @param pid codice PID completo da cercare.
     *
     * @return definizione trovata oppure null se il PID
     *         non appartiene al catalogo.
     *
     * @throws JSONException errore nel parsing del JSON.
     *
     * @throws IOException errore durante la lettura
     *                     del catalogo.
     */
    @Nullable
    public PidDefinition findByPid(
            @NonNull String pid)
            throws JSONException, IOException {

        List<PidDefinition> definitions =
                loadStandardPids();

        String normalizedPid =
                pid.trim().toUpperCase(Locale.US);

        for (PidDefinition definition :
                definitions) {

            String definitionPid =
                    definition.getPid()
                            .trim()
                            .toUpperCase(Locale.US);

            if (definitionPid.equals(normalizedPid)) {

                return definition;
            }
        }

        return null;
    }

    /**
     * Carica il catalogo standard PID e lo converte
     * in una mappa indicizzata dal codice PID.
     *
     * La mappa permette di ottenere rapidamente una
     * PidDefinition partendo dal codice PID completo.
     *
     * Esempio:
     *
     * 010C -> PidDefinition del PID RPM
     *
     * 0105 -> PidDefinition temperatura liquido
     *
     * @return mappa PID -> definizione PID.
     *
     * @throws JSONException errore nel parsing del JSON.
     *
     * @throws IOException errore durante la lettura
     *                     del catalogo.
     */
    @NonNull
    public Map<String, PidDefinition> loadStandardPidMap()
            throws JSONException, IOException {

        List<PidDefinition> definitions =
                loadStandardPids();

        Map<String, PidDefinition> map =
                new HashMap<>();

        for (PidDefinition definition :
                definitions) {

            String pid =
                    definition.getPid()
                            .trim()
                            .toUpperCase(Locale.US);

            map.put(
                    pid,
                    definition
            );
        }

        return Collections.unmodifiableMap(
                map
        );
    }



    /**
     * Carica un intero dataset diagnostico dalla risorsa JSON.
     *
     * Il dataset contiene sia i metadati del catalogo sia
     * l'elenco dei PidDefinition.
     *
     * Struttura attesa:
     *
     * {
     *   "version": "...",
     *   "source": "...",
     *   "manufacturer": "...",
     *   "brand": "...",
     *   "model": "...",
     *   "ecu": "...",
     *   "protocol": "...",
     *   "pids": [
     *      ...
     *   ]
     * }
     *
     * @param resourceId identificatore della risorsa raw.
     *
     * @return dataset diagnostico.
     *
     * @throws IOException errore durante la lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadDataset(
            int resourceId)
            throws IOException, JSONException {

        String json =
                readResource(resourceId);

        if (json.trim().isEmpty()) {

            throw new IOException(
                    "Il file JSON del dataset è vuoto."
            );
        }

        JSONObject root =
                new JSONObject(json);

        /*
         * Metadati del dataset.
         */
        String version =
                root.optString(
                        "version",
                        "1.0"
                ).trim();

        String source =
                root.optString(
                        "source",
                        "STANDARD"
                ).trim().toUpperCase(
                        Locale.US
                );

        String manufacturer =
                root.optString(
                        "manufacturer",
                        ""
                ).trim();

        String brand =
                root.optString(
                        "brand",
                        ""
                ).trim();

        String model =
                root.optString(
                        "model",
                        ""
                ).trim();

        String ecu =
                root.optString(
                        "ecu",
                        ""
                ).trim();

        String protocol =
                root.optString(
                        "protocol",
                        ""
                ).trim();

        /*
         * L'elenco dei PID deve essere presente.
         */
        JSONArray pids =
                root.optJSONArray(
                        "pids"
                );

        if (pids == null) {

            throw new JSONException(
                    "Campo 'pids' assente "
                            + "nel dataset JSON."
            );
        }

        List<PidDefinition> definitions =
                new ArrayList<>();

        /*
         * Conversione dei singoli PID.
         */
        for (int i = 0;
             i < pids.length();
             i++) {

            JSONObject pidObject =
                    pids.optJSONObject(i);

            if (pidObject == null) {

                Log.w(
                        TAG,
                        "Elemento PID non valido "
                                + "alla posizione "
                                + i
                );

                continue;
            }

            PidDefinition definition =
                    parsePid(
                            pidObject
                    );

            definitions.add(
                    definition
            );
        }

        return new PidDatasetDefinition(
                version,
                source,
                manufacturer,
                brand,
                model,
                ecu,
                protocol,
                definitions
        );
    }



    /**
     * Carica il dataset standard OBD-II completo.
     *
     * @return dataset standard.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadStandardDataset()
            throws IOException, JSONException {

        return loadDataset(
                com.dipasoftware.autodiag.R.raw.obd2_standard
        );
    }


    /**
     * Carica un dataset JSON da assets.
     *
     * @param assetPath percorso relativo all'interno di assets.
     *
     * @return dataset diagnostico.
     *
     * @throws IOException errore durante la lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadDatasetFromAsset(
            @NonNull String assetPath)
            throws IOException, JSONException {

        String json =
                readAsset(
                        assetPath
                );

        if (json.trim().isEmpty()) {

            throw new IOException(
                    "Il file JSON del dataset è vuoto: "
                            + assetPath
            );
        }

        JSONObject root =
                new JSONObject(json);

        String version =
                root.optString(
                        "version",
                        "1.0"
                ).trim();

        String source =
                root.optString(
                        "source",
                        "OEM"
                ).trim().toUpperCase(
                        Locale.US
                );

        String manufacturer =
                root.optString(
                        "manufacturer",
                        ""
                ).trim();

        String brand =
                root.optString(
                        "brand",
                        ""
                ).trim();

        String model =
                root.optString(
                        "model",
                        ""
                ).trim();

        String ecu =
                root.optString(
                        "ecu",
                        ""
                ).trim();

        String protocol =
                root.optString(
                        "protocol",
                        ""
                ).trim();

        JSONArray pids =
                root.optJSONArray(
                        "pids"
                );

        if (pids == null) {

            throw new JSONException(
                    "Campo 'pids' assente nel dataset: "
                            + assetPath
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
                        "PID non valido nel dataset "
                                + assetPath
                                + " alla posizione "
                                + i
                );

                continue;
            }

            PidDefinition definition =
                    parsePid(
                            pidObject
                    );

            definitions.add(
                    definition
            );
        }

        return new PidDatasetDefinition(
                version,
                source,
                manufacturer,
                brand,
                model,
                ecu,
                protocol,
                definitions
        );
    }



    /**
     * Legge completamente un file presente negli assets.
     *
     * @param assetPath percorso relativo.
     *
     * @return contenuto UTF-8.
     *
     * @throws IOException errore di lettura.
     */
    @NonNull
    private String readAsset(
            @NonNull String assetPath)
            throws IOException {

        try (
                InputStream inputStream =
                        context.getAssets().open(
                                assetPath
                        );

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            StringBuilder content =
                    new StringBuilder();

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                content.append(line);
                content.append('\n');
            }

            return content.toString();

        } catch (IOException exception) {

            throw new IOException(
                    "Asset JSON non trovato o "
                            + "non leggibile: "
                            + assetPath,
                    exception
            );
        }
    }



    /**
     * Carica il dataset OEM di test.
     *
     * Serve esclusivamente per verificare
     * il caricamento dei dataset dagli assets.
     *
     * @return dataset OEM di test.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadTestOemDataset()
            throws IOException, JSONException {

        return loadDatasetFromAsset(
                "pids/test/test_dataset.json"
        );
    }

}