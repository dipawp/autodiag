package com.dipasoftware.autodiag.profiles.loader;

import com.dipasoftware.autodiag.profiles.dto.ProfileDto;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;


import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/******************************************************************************
 *
 * Classe: ProfileLoader
 *
 * Package:
 * com.dipasoftware.autodiag.profiles.loader
 *
 * Tipo:
 * Classe di servizio
 *
 * Descrizione:
 *
 * Si occupa della lettura dei file JSON contenuti nella cartella
 * assets del progetto.
 *
 * In questa prima versione la classe restituisce semplicemente
 * il contenuto del file come String.
 *
 * Nei prossimi step utilizzerà Moshi per convertire il JSON nei
 * rispettivi DTO.
 *
 ******************************************************************************/
public class ProfileLoader {

    /**
     * Context dell'applicazione.
     *
     * Necessario per accedere agli assets.
     */
    private final Context context;


    /**
     * Istanza di Moshi utilizzata per deserializzare
     * i file JSON dei profili.
     */
    private final Moshi moshi;

    /**
     * Costruttore.
     *
     * @param context Context dell'applicazione.
     */
    public ProfileLoader(Context context) {
        this.context = context.getApplicationContext();
        this.moshi = new Moshi.Builder()
                .build();
    }

    /**
     * Legge un file presente nella cartella assets.
     *
     * Esempio:
     *
     * profiles/fiat/bravo/1_9_mjet/bosch_edc16c39/profile.json
     *
     * @param assetPath percorso del file all'interno della cartella assets.
     *
     * @return contenuto del file.
     *
     * @throws IOException se il file non esiste oppure non può essere letto.
     */
    public String loadAssetAsString(String assetPath) throws IOException {

        StringBuilder builder = new StringBuilder();

        InputStream inputStream = context.getAssets().open(assetPath);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream)
        );

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        reader.close();

        return builder.toString();
    }


    /**
     * Legge un file profile.json e lo converte
     * in un oggetto ProfileDto.
     *
     * @param assetPath percorso del file negli assets.
     *
     * @return ProfileDto letto dal JSON.
     *
     * @throws IOException se il file non esiste oppure
     *                     il JSON non è valido.
     */
    public ProfileDto loadProfile(String assetPath)
            throws IOException {

        String json = loadAssetAsString(assetPath);

        JsonAdapter<ProfileDto> adapter =
                moshi.adapter(ProfileDto.class);

        ProfileDto profile = adapter.fromJson(json);

        if (profile == null) {

            throw new IOException(
                    "Impossibile leggere il profilo: " + assetPath
            );

        }

        return profile;

    }

}