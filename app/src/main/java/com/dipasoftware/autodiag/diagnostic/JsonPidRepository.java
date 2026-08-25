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
 * Repository responsabile del caricamento dei cataloghi
 * PID diagnostici dell'applicazione.
 *
 * Supporta:
 *
 * - catalogo OBD-II standard;
 * - dataset OEM;
 * - dataset specifici per marca/modello/ECU;
 * - caricamento da res/raw;
 * - caricamento da assets.
 *
 * Il repository converte gli oggetti JSON in oggetti
 * PidDefinition e PidDatasetDefinition.
 *
 * Il repository NON determina se un PID è realmente
 * supportato dall'ECU collegata.
 *
 * La presenza di un PID nel catalogo indica solamente
 * che il PID è conosciuto dall'applicazione.
 *
 * Il supporto reale dell'ECU viene determinato dalla
 * logica diagnostica.
 *
 * ------------------------------------------------------------
 */
public class JsonPidRepository {

    /**
     * Tag utilizzato per i messaggi di log.
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
     * @param context context Android.
     */
    public JsonPidRepository(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();
    }

    /**
     * Carica il catalogo standard OBD-II.
     *
     * La risorsa utilizzata è:
     *
     * res/raw/obd2_standard.json
     *
     * @return lista PID standard.
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
     * Carica un catalogo PID da una risorsa raw.
     *
     * La risorsa contiene una struttura JSON del tipo:
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
     * I PID caricati da questa funzione sono considerati
     * STANDARD salvo indicazione esplicita nel singolo PID.
     *
     * @param resourceId identificatore risorsa.
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

        /*
         * Il catalogo caricato da res/raw è il catalogo
         * standard OBD-II.
         */
        String defaultSource =
                root.optString(
                                "source",
                                "STANDARD"
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        if (defaultSource.isEmpty()) {

            defaultSource =
                    "STANDARD";
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
                    parsePid(
                            pidObject,
                            defaultSource
                    );

            definitions.add(
                    definition
            );
        }

        return Collections.unmodifiableList(
                definitions
        );
    }

    /**
     * Converte un singolo JSONObject in PidDefinition.
     *
     * Il source viene ricavato dal campo del PID se presente.
     * In caso contrario viene utilizzato defaultSource.
     *
     * Campi supportati:
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
     * source
     * signed
     * endianness
     * byteOffset
     * bitOffset
     * bitLength
     *
     * @param object oggetto JSON PID.
     * @param defaultSource source predefinito del dataset.
     *
     * @return definizione PID.
     *
     * @throws JSONException dati non validi.
     */
    @NonNull
    private PidDefinition parsePid(
            @NonNull JSONObject object,
            @NonNull String defaultSource)
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

        /*
         * Se il PID specifica esplicitamente source,
         * questo prevale sul source del dataset.
         *
         * Altrimenti viene utilizzato il source del dataset.
         */
        String source =
                object.optString(
                                "source",
                                defaultSource
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        if (source.isEmpty()) {

            source =
                    defaultSource
                            .trim()
                            .toUpperCase(Locale.US);
        }

        /*
         * Nuovi metadati di decoding.
         *
         * Default compatibili con i PID standard esistenti.
         */
        boolean signed =
                object.optBoolean(
                        "signed",
                        false
                );

        String endianness =
                object.optString(
                                "endianness",
                                "BIG_ENDIAN"
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        if (endianness.isEmpty()) {

            endianness =
                    "BIG_ENDIAN";
        }

        int byteOffset =
                object.optInt(
                        "byteOffset",
                        0
                );

        int bitOffset =
                object.optInt(
                        "bitOffset",
                        0
                );

        int bitLength =
                object.optInt(
                        "bitLength",
                        0
                );


        /**
         * Richiesta diagnostica esplicita.
         *
         * Se non presente nel JSON, la richiesta verrà
         * eventualmente costruita dal layer diagnostico
         * usando mode + pid.
         */
        String request =
                object.optString(
                                "request",
                                ""
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        /**
        * Service atteso nella risposta positiva.
        *
        * Esempi:
        *
        * 41 = risposta positiva Mode 01.
        * 62 = risposta positiva UDS 0x22.
        */
        String responseService =
                object.optString(
                                "responseService",
                                ""
                        )
                        .trim()
                        .toUpperCase(Locale.US);

       /**
        * Offset del primo byte dati nella risposta.
        */
        int responseDataOffset =
                object.optInt(
                        "responseDataOffset",
                        0
                );

        /*
         * Validazione dei dati numerici.
         */
        if (bytes < 0) {

            throw new JSONException(
                    "Numero byte non valido per PID "
                            + pid
            );
        }

        if (byteOffset < 0) {

            throw new JSONException(
                    "byteOffset non valido per PID "
                            + pid
            );
        }

        if (bitOffset < 0) {

            throw new JSONException(
                    "bitOffset non valido per PID "
                            + pid
            );
        }

        if (bitLength < 0) {

            throw new JSONException(
                    "bitLength non valido per PID "
                            + pid
            );
        }

        if (responseDataOffset < 0) {

            throw new JSONException(
                    "responseDataOffset non valido per PID "
                            + pid
            );
        }


        /*
         * Validazione dell'endianness.
         */
        if (!"BIG_ENDIAN".equals(
                endianness)
                &&
                !"LITTLE_ENDIAN".equals(
                        endianness)) {

            throw new JSONException(
                    "Endianness non supportato per PID "
                            + pid
                            + ": "
                            + endianness
            );
        }

        /*
         * Il campo "available", eventualmente presente
         * nel JSON, viene volutamente ignorato.
         *
         * La disponibilità reale dipende dall'ECU.
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
                source,
                signed,
                endianness,
                byteOffset,
                bitOffset,
                bitLength,
                request,
                responseService,
                responseDataOffset
        );
    }

    /**
     * Versione semplificata di parsePid.
     *
     * Mantiene il comportamento utilizzato internamente
     * dal catalogo standard.
     *
     * @param object oggetto JSON.
     *
     * @return definizione PID.
     *
     * @throws JSONException JSON non valido.
     */
    @NonNull
    private PidDefinition parsePid(
            @NonNull JSONObject object)
            throws JSONException {

        return parsePid(
                object,
                "STANDARD"
        );
    }

    /**
     * Restituisce una stringa obbligatoria.
     *
     * @param object oggetto JSON.
     * @param key nome campo.
     *
     * @return valore.
     *
     * @throws JSONException campo mancante o vuoto.
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
     * Legge una risorsa raw completamente.
     *
     * @param resourceId identificatore risorsa.
     *
     * @return contenuto UTF-8.
     *
     * @throws IOException errore lettura.
     */
    @NonNull
    private String readResource(
            int resourceId)
            throws IOException {

        Resources resources =
                context.getResources();

        try (
                InputStream inputStream =
                        resources.openRawResource(
                                resourceId
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

                content.append(
                        line
                );

                content.append(
                        '\n'
                );
            }

            return content.toString();

        } catch (
                Resources.NotFoundException exception) {

            throw new IOException(
                    "Risorsa JSON non trovata: "
                            + resourceId,
                    exception
            );
        }
    }

    /**
     * Cerca una definizione PID nel catalogo standard.
     *
     * @param pid codice PID.
     *
     * @return definizione oppure null.
     *
     * @throws JSONException JSON non valido.
     * @throws IOException errore lettura.
     */
    @Nullable
    public PidDefinition findByPid(
            @NonNull String pid)
            throws JSONException, IOException {

        List<PidDefinition> definitions =
                loadStandardPids();

        String normalizedPid =
                pid.trim()
                        .toUpperCase(Locale.US);

        for (
                PidDefinition definition :
                definitions
        ) {

            String definitionPid =
                    definition.getPid()
                            .trim()
                            .toUpperCase(Locale.US);

            if (definitionPid.equals(
                    normalizedPid
            )) {

                return definition;
            }
        }

        return null;
    }

    /**
     * Carica il catalogo standard e crea una mappa
     * indicizzata per PID.
     *
     * @return mappa PID -> definizione.
     *
     * @throws JSONException JSON non valido.
     * @throws IOException errore lettura.
     */
    @NonNull
    public Map<String, PidDefinition> loadStandardPidMap()
            throws JSONException, IOException {

        List<PidDefinition> definitions =
                loadStandardPids();

        Map<String, PidDefinition> map =
                new HashMap<>();

        for (
                PidDefinition definition :
                definitions
        ) {

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
     * Carica un intero dataset diagnostico da JSON.
     *
     * Struttura prevista:
     *
     * {
     *   "version": "...",
     *   "source": "OEM",
     *   "manufacturer": "...",
     *   "brand": "...",
     *   "model": "...",
     *   "ecu": "...",
     *   "protocol": "...",
     *   "pids": [...]
     * }
     *
     * Il source del dataset viene utilizzato come
     * source predefinito per tutti i PID che non
     * lo specificano individualmente.
     *
     * @param resourceId risorsa raw.
     *
     * @return dataset.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public PidDatasetDefinition loadDataset(
            int resourceId)
            throws IOException, JSONException {

        String json =
                readResource(
                        resourceId
                );

        if (json.trim().isEmpty()) {

            throw new IOException(
                    "Il file JSON del dataset è vuoto."
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
                                "STANDARD"
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        if (source.isEmpty()) {

            source =
                    "STANDARD";
        }

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
                    "Campo 'pids' assente "
                            + "nel dataset JSON."
            );
        }

        List<PidDefinition> definitions =
                new ArrayList<>();

        for (
                int i = 0;
                i < pids.length();
                i++
        ) {

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
                            pidObject,
                            source
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
     * @throws IOException errore lettura.
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
     * Esempio:
     *
     * assets/pids/fiat/...
     *
     * Il source root del dataset viene propagato
     * ai singoli PID che non lo specificano.
     *
     * @param assetPath percorso asset.
     *
     * @return dataset diagnostico.
     *
     * @throws IOException errore lettura.
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
                        )
                        .trim()
                        .toUpperCase(Locale.US);

        if (source.isEmpty()) {

            source =
                    "OEM";
        }

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

        for (
                int i = 0;
                i < pids.length();
                i++
        ) {

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
                            pidObject,
                            source
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
     * @throws IOException errore lettura.
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

                content.append(
                        line
                );

                content.append(
                        '\n'
                );
            }

            return content.toString();

        } catch (
                IOException exception) {

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
     * Serve per verificare il caricamento
     * dei dataset dagli assets.
     *
     * @return dataset OEM di test.
     *
     * @throws IOException errore lettura.
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