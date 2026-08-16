package com.dipasoftware.autodiag;

import android.app.Application;
import com.dipasoftware.autodiag.BuildConfig;



/******************************************************************************
 *
 * Classe.....: App
 *
 * Package....: com.dipasoftware.autodiag
 *
 * Autore.....: DiPa Software
 *
 * Data.......: 04/08/2026
 *
 * Descrizione:
 * Classe principale dell'applicazione.
 *
 * Android crea un'unica istanza di questa classe
 * all'avvio dell'applicazione.
 *
 * Tutte le inizializzazioni globali devono essere
 * effettuate qui.
 *
 * Responsabilità:
 *
 * - Inizializzare Timber.
 * - Inizializzare Room (futuro).
 * - Inizializzare SettingsManager (futuro).
 * - Inizializzare eventuali servizi globali.
 *
 ******************************************************************************/
public class App extends Application {

    /**
     * Metodo chiamato automaticamente da Android
     * quando l'applicazione viene creata.
     *
     * Viene eseguito una sola volta.
     */
    @Override
    public void onCreate() {

        super.onCreate();

        initializeLogging();
    }

    /**
     * Inizializza il sistema di logging.
     *
     * In modalità Debug vengono mostrati
     * tutti i log.
     *
     * In modalità Release non verranno
     * visualizzati i log di debug.
     */
    private void initializeLogging() {

        /*if (BuildConfig.DEBUG) {
            DebugTree debugTree = new DebugTree();

            Timber.plant(debugTree);
        }

        Timber.i("==========================================");
        Timber.i("AutoDiag avviata");
        Timber.i("Versione: %s", BuildConfig.VERSION_NAME);
        Timber.i("==========================================");*/
    }

}