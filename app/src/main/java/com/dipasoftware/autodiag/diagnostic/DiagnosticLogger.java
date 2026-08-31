package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticLogger
 *
 * Tipo.......: Servizio
 *
 * Descrizione:
 *
 * Salva i log diagnostici dell'applicazione in file TXT.
 *
 * I log vengono salvati nella cartella privata
 * dell'applicazione.
 *
 * Non richiede permessi di storage.
 *
 * ****************************************************************************
 */
public class DiagnosticLogger {

    /**
     * Context applicativo.
     */
    @NonNull
    private final Context context;

    /**
     * Costruttore.
     *
     * @param context context applicativo.
     */
    public DiagnosticLogger(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Salva un log diagnostico in un nuovo file.
     *
     * @param content contenuto del log.
     *
     * @return file creato.
     *
     * @throws IOException errore di scrittura.
     */
    @NonNull
    public File saveLog(@NonNull String content) throws IOException {

        /*
         * Cartella:
         *
         * Android/data/<package>/files/diagnostic_logs
         */
        File directory = new File(context.getExternalFilesDir(null),"diagnostic_logs");

        if (!directory.exists()) {
            if (!directory.mkdirs() && !directory.exists()) {
                throw new IOException("Impossibile creare la cartella log.");
            }
        }

        /*
         * Timestamp del test.
         *
         * Esempio:
         *
         * 20260817_151530
         */
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        File file = new File(directory, "diagnostic_" + timestamp + ".txt");
        FileWriter writer = new FileWriter(file,false);

        try {
            writer.write(content);
        } finally {
            writer.close();
        }
        return file;
    }
}