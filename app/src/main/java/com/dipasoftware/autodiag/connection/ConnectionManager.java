package com.dipasoftware.autodiag.connection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/******************************************************************************
 *
 * Classe.....: ConnectionManager
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.connection
 *
 * Descrizione:
 *
 * Mantiene la connessione diagnostica attualmente attiva.
 *
 * Permette alle diverse parti dell'applicazione di utilizzare
 * la stessa connessione senza creare connessioni duplicate.
 *
 ******************************************************************************/
public class ConnectionManager {

    /**
     * Istanza singleton.
     */
    private static final ConnectionManager INSTANCE =
            new ConnectionManager();

    /**
     * Connessione attualmente attiva.
     */
    @Nullable
    private Connection activeConnection;

    /**
     * Listener dello stato della connessione.
     */
    @NonNull
    private final Set<ConnectionStateListener> listeners =
            new HashSet<>();

    /**
     * Costruttore privato.
     */
    private ConnectionManager() {
    }

    /**
     * Restituisce l'istanza globale.
     *
     * @return istanza del manager.
     */
    @NonNull
    public static ConnectionManager getInstance() {

        return INSTANCE;
    }

    /**
     * Registra una connessione come attiva.
     *
     * @param connection connessione.
     */
    public void setConnection(
            @NonNull Connection connection) {

        synchronized (this) {

            activeConnection = connection;
        }

        notifyConnectionStateChanged();
    }

    /**
     * Restituisce la connessione attiva.
     *
     * @return connessione oppure null.
     */
    @Nullable
    public synchronized Connection getConnection() {

        return activeConnection;
    }

    /**
     * Verifica se esiste una connessione attiva.
     *
     * @return true se connessa.
     */
    public synchronized boolean isConnected() {

        return activeConnection != null
                && activeConnection.isConnected();
    }

    /**
     * Rimuove la connessione senza chiuderla.
     */
    public void clearConnection() {

        synchronized (this) {

            activeConnection = null;
        }

        notifyConnectionStateChanged();
    }

    /**
     * Chiude la connessione attiva e la rimuove.
     *
     * @throws IOException errore di chiusura.
     */
    public void disconnect()
            throws IOException {

        Connection connection;

        synchronized (this) {

            connection = activeConnection;
            activeConnection = null;
        }

        if (connection != null) {

            try {

                connection.disconnect();

            } finally {

                notifyConnectionStateChanged();
            }

        } else {

            notifyConnectionStateChanged();
        }
    }

    /**
     * Registra un listener.
     *
     * @param listener listener.
     */
    public void addConnectionStateListener(
            @NonNull ConnectionStateListener listener) {

        boolean connected;

        synchronized (this) {

            listeners.add(listener);

            connected =
                    activeConnection != null
                            && activeConnection.isConnected();
        }

        /*
         * Comunichiamo lo stato fuori dal synchronized.
         */
        listener.onConnectionStateChanged(
                connected
        );
    }

    /**
     * Rimuove un listener.
     *
     * @param listener listener.
     */
    public synchronized void removeConnectionStateListener(
            @NonNull ConnectionStateListener listener) {

        listeners.remove(listener);
    }

    /**
     * Notifica i listener dello stato attuale.
     */
    private void notifyConnectionStateChanged() {

        Set<ConnectionStateListener> listenersCopy;

        boolean connected;

        synchronized (this) {

            listenersCopy =
                    new HashSet<>(listeners);

            connected =
                    activeConnection != null
                            && activeConnection.isConnected();
        }

        /*
         * Le callback vengono eseguite fuori dal lock.
         */
        for (ConnectionStateListener listener :
                listenersCopy) {

            listener.onConnectionStateChanged(
                    connected
            );
        }
    }

    /**
     * Listener dello stato della connessione.
     */
    public interface ConnectionStateListener {

        /**
         * Notifica il cambiamento dello stato.
         *
         * @param connected true se connessa.
         */
        void onConnectionStateChanged(
                boolean connected
        );
    }
}
