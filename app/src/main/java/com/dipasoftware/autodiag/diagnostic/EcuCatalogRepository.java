package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

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
import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuCatalogRepository
 *
 * Tipo.......: Repository
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Carica e gestisce il catalogo delle ECU diagnostiche.
 *
 * Il catalogo è indipendente dal costruttore.
 *
 * Può contenere ECU di:
 *
 * - Fiat
 * - Alfa Romeo
 * - Lancia
 * - BMW
 * - Volkswagen
 * - Audi
 * - Mercedes-Benz
 * - Ford
 * - Toyota
 * - Honda
 * - ecc.
 *
 * Il repository NON contiene la logica di comunicazione con l'ECU.
 *
 * Il suo compito è esclusivamente:
 *
 * - leggere il catalogo JSON;
 * - creare EcuDefinition;
 * - leggere gli identificativi compatibili;
 * - cercare una ECU;
 * - restituire i dataset associati.
 *
 * ****************************************************************************
 */
public class EcuCatalogRepository {

    /**
     * Percorso del catalogo ECU principale negli assets.
     */
    private static final String CATALOG_ASSET =
            "diagnostic/ecu/ecu_catalog.json";

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
    public EcuCatalogRepository(
            @NonNull Context context) {

        this.context =
                context.getApplicationContext();
    }

    /**
     * Carica tutte le ECU definite nel catalogo.
     *
     * @return lista immutabile delle ECU.
     *
     * @throws IOException errore di lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<EcuDefinition> loadAll()
            throws IOException, JSONException {

        String json =
                readAsset(
                        CATALOG_ASSET
                );

        JSONObject root =
                new JSONObject(
                        json
                );

        JSONArray ecus =
                root.optJSONArray(
                        "ecus"
                );

        if (ecus == null) {

            throw new JSONException(
                    "Campo 'ecus' assente "
                            + "nel catalogo ECU."
            );
        }

        List<EcuDefinition> definitions =
                new ArrayList<>();

        for (int index = 0;
             index < ecus.length();
             index++) {

            JSONObject ecuObject =
                    ecus.optJSONObject(
                            index
                    );

            if (ecuObject == null) {

                continue;
            }

            definitions.add(
                    parseEcu(
                            ecuObject
                    )
            );
        }

        return Collections.unmodifiableList(
                definitions
        );
    }

    /**
     * Cerca una ECU utilizzando marca, modello,
     * motore e identificativo ECU.
     *
     * Il confronto è case-insensitive.
     *
     * @param brand marca.
     * @param model modello.
     * @param engine motore.
     * @param ecu identificativo ECU.
     *
     * @return ECU trovata oppure null.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @Nullable
    public EcuDefinition find(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu)
            throws IOException, JSONException {

        List<EcuDefinition> definitions =
                loadAll();

        String normalizedBrand =
                normalize(
                        brand
                );

        String normalizedModel =
                normalize(
                        model
                );

        String normalizedEngine =
                normalize(
                        engine
                );

        String normalizedEcu =
                normalize(
                        ecu
                );

        for (
                EcuDefinition definition :
                definitions
        ) {

            if (!normalize(
                    definition.getBrand()
            ).equals(
                    normalizedBrand
            )) {

                continue;
            }

            if (!normalize(
                    definition.getModel()
            ).equals(
                    normalizedModel
            )) {

                continue;
            }

            if (!normalize(
                    definition.getEngine()
            ).equals(
                    normalizedEngine
            )) {

                continue;
            }

            if (!normalize(
                    definition.getEcu()
            ).equals(
                    normalizedEcu
            )) {

                continue;
            }

            return definition;
        }

        return null;
    }

    /**
     * Cerca tutte le ECU appartenenti a una determinata marca.
     *
     * @param brand marca.
     *
     * @return ECU trovate.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<EcuDefinition> findByBrand(
            @NonNull String brand)
            throws IOException, JSONException {

        List<EcuDefinition> definitions =
                loadAll();

        String normalizedBrand =
                normalize(
                        brand
                );

        List<EcuDefinition> result =
                new ArrayList<>();

        for (
                EcuDefinition definition :
                definitions
        ) {

            if (normalize(
                    definition.getBrand()
            ).equals(
                    normalizedBrand
            )) {

                result.add(
                        definition
                );
            }
        }

        return Collections.unmodifiableList(
                result
        );
    }

    /**
     * Cerca tutte le ECU associate a un modello.
     *
     * La ricerca viene effettuata anche sulla marca
     * per evitare ambiguità tra modelli con lo stesso nome.
     *
     * @param brand marca.
     * @param model modello.
     *
     * @return ECU trovate.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<EcuDefinition> findByModel(
            @NonNull String brand,
            @NonNull String model)
            throws IOException, JSONException {

        List<EcuDefinition> definitions =
                loadAll();

        String normalizedBrand =
                normalize(
                        brand
                );

        String normalizedModel =
                normalize(
                        model
                );

        List<EcuDefinition> result =
                new ArrayList<>();

        for (
                EcuDefinition definition :
                definitions
        ) {

            if (!normalize(
                    definition.getBrand()
            ).equals(
                    normalizedBrand
            )) {

                continue;
            }

            if (!normalize(
                    definition.getModel()
            ).equals(
                    normalizedModel
            )) {

                continue;
            }

            result.add(
                    definition
            );
        }

        return Collections.unmodifiableList(
                result
        );
    }

    /**
     * Restituisce tutte le ECU che utilizzano
     * un determinato protocollo.
     *
     * @param protocol protocollo.
     *
     * @return ECU trovate.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    @NonNull
    public List<EcuDefinition> findByProtocol(
            @NonNull String protocol)
            throws IOException, JSONException {

        List<EcuDefinition> definitions =
                loadAll();

        String normalizedProtocol =
                normalize(
                        protocol
                );

        List<EcuDefinition> result =
                new ArrayList<>();

        for (
                EcuDefinition definition :
                definitions
        ) {

            if (normalize(
                    definition.getProtocol()
            ).equals(
                    normalizedProtocol
            )) {

                result.add(
                        definition
                );
            }
        }

        return Collections.unmodifiableList(
                result
        );
    }

    /**
     * Restituisce il numero di ECU definite
     * nel catalogo.
     *
     * @return numero ECU.
     *
     * @throws IOException errore lettura.
     * @throws JSONException JSON non valido.
     */
    public int count()
            throws IOException, JSONException {

        return loadAll().size();
    }

    /**
     * Converte un JSONObject in EcuDefinition.
     *
     * @param object oggetto JSON.
     *
     * @return definizione ECU.
     *
     * @throws JSONException dati mancanti o non validi.
     */
    @NonNull
    private EcuDefinition parseEcu(
            @NonNull JSONObject object)
            throws JSONException {

        String brand =
                requireString(
                        object,
                        "brand"
                );

        String model =
                requireString(
                        object,
                        "model"
                );

        String engine =
                requireString(
                        object,
                        "engine"
                );

        String ecu =
                requireString(
                        object,
                        "ecu"
                );

        String protocol =
                requireString(
                        object,
                        "protocol"
                );

        String pidFile =
                object.optString(
                        "pidFile",
                        ""
                ).trim();

        /*
         * ---------------------------------------------------------
         * IDENTIFICATORI ECU
         * ---------------------------------------------------------
         *
         * Il blocco "identifiers" è opzionale.
         *
         * Se non esiste, viene utilizzato un
         * EcuDefinitionIdentifier vuoto.
         */
        JSONObject identifiersObject =
                object.optJSONObject(
                        "identifiers"
                );

        EcuDefinitionIdentifier identifiers =
                parseIdentifiers(
                        identifiersObject
                );


        JSONArray identificationArray =
                object.optJSONArray(
                        "identification"
                );

        List<EcuIdentificationDefinition> identification =
                parseIdentificationDefinitions(
                        identificationArray
                );


        JSONObject targetObject =
                object.optJSONObject(
                        "target"
                );

        DiagnosticTargetDefinition target =
                parseTarget(
                        targetObject,
                        protocol
                );

        /*
         * ---------------------------------------------------------
         * DATASET
         * ---------------------------------------------------------
         */

        JSONArray datasetArray =
                object.optJSONArray(
                        "datasets"
                );

        List<DiagnosticDataset> datasets =
                new ArrayList<>();

        if (datasetArray != null) {

            for (
                    int index = 0;
                    index < datasetArray.length();
                    index++
            ) {

                JSONObject datasetObject =
                        datasetArray.optJSONObject(
                                index
                        );

                if (datasetObject == null) {

                    continue;
                }

                datasets.add(
                        parseDataset(
                                datasetObject
                        )
                );
            }
        }

        return new EcuDefinition(
                brand,
                model,
                engine,
                ecu,
                protocol,
                pidFile,
                identifiers,
                identification,
                target,
                datasets
        );
    }

    /**
     * Converte il blocco JSON "identifiers"
     * in EcuDefinitionIdentifier.
     *
     * Il blocco può essere assente nei vecchi cataloghi.
     *
     * @param object oggetto JSON identifiers oppure null.
     *
     * @return identificatori ECU.
     */
    @NonNull
    private EcuDefinitionIdentifier parseIdentifiers(
            @Nullable JSONObject object) {

        if (object == null) {

            return new EcuDefinitionIdentifier();
        }

        return new EcuDefinitionIdentifier(
                readStringArray(
                        object,
                        "hardwareNumbers"
                ),
                readStringArray(
                        object,
                        "softwareNumbers"
                ),
                readStringArray(
                        object,
                        "partNumbers"
                ),
                readStringArray(
                        object,
                        "suppliers"
                ),
                readStringArray(
                        object,
                        "vinPatterns"
                )
        );
    }

    /**
     * Legge un array JSON di stringhe.
     *
     * I campi null e vuoti vengono ignorati.
     *
     * @param object oggetto JSON.
     * @param key chiave.
     *
     * @return lista delle stringhe.
     */
    @NonNull
    private List<String> readStringArray(
            @NonNull JSONObject object,
            @NonNull String key) {

        JSONArray array =
                object.optJSONArray(
                        key
                );

        if (array == null) {

            return Collections.emptyList();
        }

        List<String> result =
                new ArrayList<>();

        for (
                int index = 0;
                index < array.length();
                index++
        ) {

            String value =
                    array.optString(
                            index,
                            ""
                    ).trim();

            if (!value.isEmpty()) {

                result.add(
                        value
                );
            }
        }

        return result;
    }

    /**
     * Converte un JSONObject in DiagnosticDataset.
     *
     * @param object oggetto JSON.
     *
     * @return dataset.
     *
     * @throws JSONException dati non validi.
     */
    @NonNull
    private DiagnosticDataset parseDataset(
            @NonNull JSONObject object)
            throws JSONException {

        String type =
                requireString(
                        object,
                        "type"
                );

        String filePath =
                requireString(
                        object,
                        "filePath"
                );

        String protocol =
                object.optString(
                        "protocol",
                        ""
                ).trim();

        String description =
                object.optString(
                        "description",
                        ""
                ).trim();

        return new DiagnosticDataset(
                type,
                filePath,
                protocol,
                description
        );
    }

    /**
     * Restituisce un campo JSON obbligatorio.
     *
     * @param object oggetto JSON.
     * @param key chiave.
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
     * Legge un file JSON dagli assets.
     *
     * @param assetPath percorso dell'asset.
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

                content.append(
                        line
                );

                content.append(
                        '\n'
                );
            }

            return content.toString();

        } catch (IOException exception) {

            throw new IOException(
                    "Catalogo ECU non trovato: "
                            + assetPath,
                    exception
            );
        }
    }

    /**
     * Normalizza una stringa per i confronti.
     *
     * @param value valore.
     *
     * @return valore normalizzato.
     */
    @NonNull
    private String normalize(
            @NonNull String value) {

        return value
                .trim()
                .toUpperCase(
                        Locale.US
                );
    }

    /**
     * Converte il blocco JSON "identification"
     * nelle definizioni utilizzate per la discovery ECU.
     *
     * Il blocco è opzionale.
     *
     * @param array array JSON.
     *
     * @return lista delle definizioni.
     *
     * @throws JSONException JSON non valido.
     */
    @NonNull
    private List<EcuIdentificationDefinition>
    parseIdentificationDefinitions(
            @Nullable JSONArray array)
            throws JSONException {

        if (array == null) {

            return Collections.emptyList();
        }

        List<EcuIdentificationDefinition> result =
                new ArrayList<>();

        for (
                int index = 0;
                index < array.length();
                index++
        ) {

            JSONObject object =
                    array.optJSONObject(
                            index
                    );

            if (object == null) {

                continue;
            }

            String service =
                    requireString(
                            object,
                            "service"
                    );

            String did =
                    requireString(
                            object,
                            "did"
                    );

            String field =
                    requireString(
                            object,
                            "field"
                    );

            String decoder =
                    requireString(
                            object,
                            "decoder"
                    );

            boolean required =
                    object.optBoolean(
                            "required",
                            false
                    );

            int byteOffset =
                    object.optInt(
                            "byteOffset",
                            0
                    );

            int byteLength =
                    object.optInt(
                            "byteLength",
                            0
                    );

            result.add(
                    new EcuIdentificationDefinition(
                            service,
                            did,
                            field,
                            decoder,
                            required,
                            byteOffset,
                            byteLength
                    )
            );
        }

        return result;
    }


    /**
     * Converte il blocco JSON "target" in
     * DiagnosticTargetDefinition.
     *
     * Il blocco è opzionale per mantenere compatibilità
     * con i vecchi cataloghi.
     *
     * Se assente, viene utilizzato un target predefinito
     * coerente con il protocollo.
     *
     * @param object oggetto JSON target oppure null.
     * @param protocol protocollo ECU.
     *
     * @return target diagnostico.
     */
    @NonNull
    private DiagnosticTargetDefinition parseTarget(
            @Nullable JSONObject object,
            @NonNull String protocol) {

        if (object == null) {

            return createDefaultTarget(
                    protocol
            );
        }

        String targetProtocol =
                object.optString(
                        "protocol",
                        protocol
                ).trim();

        String requestId =
                object.optString(
                        "requestId",
                        ""
                ).trim();

        String responseId =
                object.optString(
                        "responseId",
                        ""
                ).trim();

        String addressingMode =
                object.optString(
                        "addressingMode",
                        "PHYSICAL"
                ).trim();

        int canIdBits =
                object.optInt(
                        "canIdBits",
                        11
                );

        /*
         * Se il JSON contiene un target ma manca uno degli ID
         * necessari, consideriamo il catalogo non valido.
         */
        if (requestId.isEmpty()) {

            throw new IllegalArgumentException(
                    "target.requestId mancante."
            );
        }

        if (responseId.isEmpty()) {

            throw new IllegalArgumentException(
                    "target.responseId mancante."
            );
        }

        return new DiagnosticTargetDefinition(
                targetProtocol,
                requestId,
                responseId,
                addressingMode,
                canIdBits
        );
    }


    /**
     * Crea il target predefinito per un catalogo
     * che non contiene ancora la sezione "target".
     *
     * @param protocol protocollo.
     *
     * @return target predefinito.
     */
    @NonNull
    private DiagnosticTargetDefinition createDefaultTarget(
            @NonNull String protocol) {

        String normalizedProtocol =
                protocol.trim()
                        .toUpperCase();

        if ("CAN".equals(
                normalizedProtocol
        )
                ||
                "ISO_15765_4_CAN".equals(
                        normalizedProtocol
                )
                ||
                "UDS".equals(
                        normalizedProtocol
                )) {

            return new DiagnosticTargetDefinition(
                    normalizedProtocol,
                    "7E0",
                    "7E8",
                    "PHYSICAL",
                    11
            );
        }

        /*
         * Placeholder esclusivamente per compatibilità
         * con protocolli non-CAN.
         *
         * Non viene utilizzato dal transport layer.
         */
        return new DiagnosticTargetDefinition(
                normalizedProtocol.isEmpty()
                        ? "UNKNOWN"
                        : normalizedProtocol,
                "0",
                "0",
                "PHYSICAL",
                11
        );
    }
}