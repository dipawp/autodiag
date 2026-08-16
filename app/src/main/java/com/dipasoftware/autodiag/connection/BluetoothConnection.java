package com.dipasoftware.autodiag.connection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/******************************************************************************
 *
 * Classe.....: BluetoothConnection
 *
 * Tipo.......: Implementazione Connection
 *
 * Package....: com.dipasoftware.autodiag.connection
 *
 * Descrizione:
 *
 * Implementa una connessione RFCOMM Bluetooth.
 *
 * Gestisce esclusivamente il trasporto dati.
 *
 ******************************************************************************/
public class BluetoothConnection implements Connection {

    /**
     * UUID standard Bluetooth SPP.
     */
    private static final UUID SPP_UUID =
            UUID.fromString(
                    "00001101-0000-1000-8000-00805F9B34FB"
            );

    /**
     * Timeout massimo per attendere una risposta ELM327.
     */
    private static final long RECEIVE_TIMEOUT_MS = 5000L;

    /**
     * Tempo massimo di attesa iniziale
     * prima che arrivino i primi dati.
     */
    private static final long INITIAL_WAIT_MS = 3000L;

    /**
     * Context applicativo.
     */
    private final Context context;

    /**
     * Adapter Bluetooth.
     */
    private final BluetoothAdapter bluetoothAdapter;

    /**
     * Dispositivo Bluetooth remoto.
     */
    private final BluetoothDevice bluetoothDevice;

    /**
     * Socket RFCOMM.
     */
    @Nullable
    private BluetoothSocket bluetoothSocket;

    /**
     * Stream input.
     */
    @Nullable
    private InputStream inputStream;

    /**
     * Stream output.
     */
    @Nullable
    private OutputStream outputStream;

    /**
     * Costruttore.
     *
     * @param context Context.
     * @param device dispositivo Bluetooth.
     */
    public BluetoothConnection(
            @NonNull Context context,
            @NonNull BluetoothDevice device) {

        this.context =
                context.getApplicationContext();

        BluetoothAdapter adapter =
                BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {

            throw new IllegalStateException(
                    "Bluetooth non disponibile sul dispositivo."
            );
        }

        this.bluetoothAdapter = adapter;
        this.bluetoothDevice = device;
    }

    /**
     * Apre la connessione RFCOMM.
     *
     * @throws IOException errore di connessione.
     */
    @Override
    public synchronized void connect()
            throws IOException {

        checkConnectPermission();

        if (isConnected()) {
            return;
        }

        bluetoothAdapter.cancelDiscovery();

        /*
         * Se esiste un socket precedente,
         * lo chiudiamo prima di crearne uno nuovo.
         */
        closeSocketQuietly();

        bluetoothSocket =
                bluetoothDevice
                        .createRfcommSocketToServiceRecord(
                                SPP_UUID
                        );

        bluetoothSocket.connect();

        inputStream =
                bluetoothSocket.getInputStream();

        outputStream =
                bluetoothSocket.getOutputStream();
    }

    /**
     * Chiude la connessione.
     *
     * @throws IOException errore durante la chiusura.
     */
    @Override
    public synchronized void disconnect()
            throws IOException {

        IOException firstException = null;

        try {

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (IOException exception) {

            firstException = exception;
        }

        try {

            if (outputStream != null) {
                outputStream.close();
            }

        } catch (IOException exception) {

            if (firstException == null) {
                firstException = exception;
            }
        }

        try {

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }

        } catch (IOException exception) {

            if (firstException == null) {
                firstException = exception;
            }
        }

        inputStream = null;
        outputStream = null;
        bluetoothSocket = null;

        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * Verifica lo stato della connessione.
     *
     * @return true se connesso.
     */
    @Override
    public synchronized boolean isConnected() {

        return bluetoothSocket != null
                && bluetoothSocket.isConnected();
    }

    /**
     * Invia una stringa.
     *
     * @param data dati da inviare.
     *
     * @throws IOException errore di scrittura.
     */
    @Override
    public synchronized void send(
            @NonNull String data)
            throws IOException {

        if (!isConnected()) {

            throw new IOException(
                    "Connessione Bluetooth non attiva."
            );
        }

        if (outputStream == null) {

            throw new IOException(
                    "Output stream non disponibile."
            );
        }

        outputStream.write(
                data.getBytes(
                        StandardCharsets.US_ASCII
                )
        );

        outputStream.flush();
    }

    /**
     * Riceve una risposta ELM327 completa.
     *
     * IMPORTANTE:
     *
     * L'ELM327 non deve essere considerato terminato
     * al primo carattere CR.
     *
     * Normalmente una risposta termina con il prompt:
     *
     * >
     *
     * Esempio:
     *
     * ATI
     * ELM327 v1.4
     * >
     *
     * oppure:
     *
     * ATI\rELM327 v1.4\r\r>
     *
     * Il metodo quindi continua a leggere fino al prompt
     * '>' oppure fino al timeout.
     *
     * @return risposta completa.
     *
     * @throws IOException errore di lettura.
     */
    @Override
    @NonNull
    public String receive()
            throws IOException {

        if (!isConnected()) {

            throw new IOException(
                    "Connessione Bluetooth non attiva."
            );
        }

        if (inputStream == null) {

            throw new IOException(
                    "Input stream non disponibile."
            );
        }

        StringBuilder response =
                new StringBuilder();

        long startTime =
                System.currentTimeMillis();

        boolean receivedData = false;

        while (true) {

            /*
             * Se ci sono dati disponibili,
             * li leggiamo immediatamente.
             */
            if (inputStream.available() > 0) {

                int value =
                        inputStream.read();

                if (value == -1) {
                    break;
                }

                char character =
                        (char) value;

                response.append(character);

                receivedData = true;

                /*
                 * Il prompt '>' indica che
                 * l'ELM327 ha terminato la risposta.
                 */
                if (character == '>') {

                    break;
                }

                continue;
            }

            /*
             * Nessun dato disponibile.
             */
            long elapsed =
                    System.currentTimeMillis()
                            - startTime;

            /*
             * Se non è ancora arrivato alcun dato,
             * aspettiamo fino al timeout iniziale.
             */
            if (!receivedData) {

                if (elapsed >= INITIAL_WAIT_MS) {
                    break;
                }

            } else {

                /*
                 * Abbiamo già ricevuto dati ma non
                 * abbiamo ancora ricevuto il prompt.
                 *
                 * Attendiamo fino al timeout totale.
                 */
                if (elapsed >= RECEIVE_TIMEOUT_MS) {
                    break;
                }
            }

            try {

                Thread.sleep(20L);

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                throw new IOException(
                        "Lettura Bluetooth interrotta.",
                        exception
                );
            }
        }

        return response.toString();
    }

    /**
     * Chiude silenziosamente il socket precedente.
     */
    private void closeSocketQuietly() {

        try {

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (IOException ignored) {
        }

        try {

            if (outputStream != null) {
                outputStream.close();
            }

        } catch (IOException ignored) {
        }

        try {

            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }

        } catch (IOException ignored) {
        }

        inputStream = null;
        outputStream = null;
        bluetoothSocket = null;
    }

    /**
     * Verifica il permesso BLUETOOTH_CONNECT.
     */
    private void checkConnectPermission() {

        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S) {

            return;
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED) {

            throw new SecurityException(
                    "Permesso BLUETOOTH_CONNECT non disponibile."
            );
        }
    }
}
