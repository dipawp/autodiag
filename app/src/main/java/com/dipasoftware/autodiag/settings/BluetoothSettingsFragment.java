package com.dipasoftware.autodiag.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dipasoftware.autodiag.R;
import com.dipasoftware.autodiag.bluetooth.BluetoothDeviceInfo;
import com.dipasoftware.autodiag.bluetooth.BluetoothManager;
import com.dipasoftware.autodiag.connection.BluetoothConnection;
import com.dipasoftware.autodiag.connection.ConnectionManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/******************************************************************************
 *
 * Classe.....: BluetoothSettingsFragment
 *
 * Tipo.......: Fragment
 *
 * Package....: com.dipasoftware.autodiag.settings
 *
 * Descrizione:
 *
 * Visualizza i dispositivi Bluetooth associati e permette
 * di selezionare e collegare un dispositivo.
 *
 ******************************************************************************/
public class BluetoothSettingsFragment extends Fragment {

    /**
     * Nome delle SharedPreferences.
     */
    public static final String PREFS_NAME =
            "autodiag_bluetooth";

    /**
     * Chiave del dispositivo Bluetooth selezionato.
     */
    public static final String PREF_LAST_DEVICE_ADDRESS =
            "last_device_address";

    /**
     * RecyclerView dispositivi.
     */
    private RecyclerView recyclerViewDevices;

    /**
     * Messaggio lista vuota.
     */
    private TextView textViewEmpty;

    /**
     * BluetoothManager.
     */
    private BluetoothManager bluetoothManager;

    /**
     * Adapter.
     */
    private BluetoothDeviceAdapter adapter;

    /**
     * Dispositivo selezionato.
     */
    private BluetoothDeviceInfo selectedDevice;

    /**
     * Connessione Bluetooth.
     */
    @Nullable
    private BluetoothConnection bluetoothConnection;

    /**
     * ConnectionManager.
     */
    private ConnectionManager connectionManager;

    /**
     * Executor connessione.
     */
    private final ExecutorService connectionExecutor =
            Executors.newSingleThreadExecutor();

    /**
     * Costruttore.
     */
    public BluetoothSettingsFragment() {

        super(R.layout.fragment_bluetooth_settings);
    }

    /**
     * Crea la View.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_bluetooth_settings,
                container,
                false
        );
    }

    /**
     * Inizializza la schermata.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        recyclerViewDevices =
                view.findViewById(
                        R.id.recyclerViewBluetoothDevices
                );

        textViewEmpty =
                view.findViewById(
                        R.id.textViewBluetoothEmpty
                );

        bluetoothManager =
                new BluetoothManager(
                        requireContext()
                );

        connectionManager =
                ConnectionManager.getInstance();

        adapter =
                new BluetoothDeviceAdapter();

        recyclerViewDevices.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerViewDevices.setAdapter(
                adapter
        );

        adapter.setBluetoothDeviceClickListener(
                this::onBluetoothDeviceSelected
        );

        loadPairedDevices();
    }

    /**
     * Carica os dispositivos associati.
     */
    private void loadPairedDevices() {

        if (!bluetoothManager.isBluetoothAvailable()) {

            showEmptyMessage(
                    "Bluetooth non disponibile"
            );

            return;
        }

        if (!bluetoothManager.isBluetoothEnabled()) {

            showEmptyMessage(
                    "Bluetooth disattivato"
            );

            return;
        }

        if (!bluetoothManager.hasBluetoothConnectPermission()) {

            showEmptyMessage(
                    "Permesso Bluetooth non disponibile"
            );

            return;
        }

        List<BluetoothDeviceInfo> devices =
                bluetoothManager.getPairedDevices();

        if (devices.isEmpty()) {

            showEmptyMessage(
                    "Nessun dispositivo Bluetooth associato"
            );

            return;
        }

        textViewEmpty.setVisibility(
                View.GONE
        );

        recyclerViewDevices.setVisibility(
                View.VISIBLE
        );

        adapter.setDevices(
                devices
        );
    }

    /**
     * Gestisce la selezione manuale.
     *
     * @param device dispositivo selezionato.
     */
    private void onBluetoothDeviceSelected(
            @NonNull BluetoothDeviceInfo device) {

        selectedDevice = device;

        Toast.makeText(
                requireContext(),
                "Connessione a "
                        + getDeviceName(device)
                        + "...",
                Toast.LENGTH_SHORT
        ).show();

        connectToSelectedDevice();
    }

    /**
     * Apre la connessione.
     */
    private void connectToSelectedDevice() {

        if (selectedDevice == null) {
            return;
        }

        if (connectionManager.isConnected()) {

            showConnectionResult(
                    true,
                    "Dispositivo già connesso"
            );

            return;
        }

        final BluetoothDeviceInfo device =
                selectedDevice;

        connectionExecutor.execute(() -> {

            BluetoothConnection connection = null;

            try {

                connection =
                        bluetoothManager.createConnection(
                                device
                        );

                connection.connect();

                /*
                 * Registra la connessione globale.
                 */
                connectionManager.setConnection(
                        connection
                );

                bluetoothConnection =
                        connection;

                /*
                 * Salva l'indirizzo MAC.
                 */
                saveLastDeviceAddress(
                        device.getAddress()
                );

                showConnectionResult(
                        true,
                        "Connesso a "
                                + getDeviceName(device)
                );

            } catch (IOException exception) {

                if (connection != null) {

                    try {

                        connection.disconnect();

                    } catch (IOException ignored) {
                    }
                }

                showConnectionResult(
                        false,
                        "Connessione fallita: "
                                + exception.getMessage()
                );

            } catch (SecurityException exception) {

                showConnectionResult(
                        false,
                        "Permesso Bluetooth non disponibile"
                );

            } catch (RuntimeException exception) {

                showConnectionResult(
                        false,
                        "Errore Bluetooth: "
                                + exception.getMessage()
                );
            }
        });
    }

    /**
     * Salva il MAC dell'ultimo dispositivo utilizzato.
     *
     * @param address indirizzo MAC.
     */
    private void saveLastDeviceAddress(
            @NonNull String address) {

        requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        PREF_LAST_DEVICE_ADDRESS,
                        address
                )
                .apply();
    }

    /**
     * Restituisce il nome del dispositivo.
     *
     * @param device dispositivo.
     *
     * @return nome.
     */
    @NonNull
    private String getDeviceName(
            @NonNull BluetoothDeviceInfo device) {

        String name =
                device.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            return "Dispositivo Bluetooth";
        }

        return name;
    }

    /**
     * Visualizza il risultato della connessione.
     *
     * @param success successo.
     * @param message messaggio.
     */
    private void showConnectionResult(
            boolean success,
            @NonNull String message) {

        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {

            if (!isAdded()) {
                return;
            }

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    /**
     * Restituisce il dispositivo selezionato.
     *
     * @return dispositivo oppure null.
     */
    @Nullable
    public BluetoothDeviceInfo getSelectedDevice() {

        return selectedDevice;
    }

    /**
     * Restituisce la connessione Bluetooth.
     *
     * @return connessione oppure null.
     */
    @Nullable
    public BluetoothConnection getBluetoothConnection() {

        return bluetoothConnection;
    }

    /**
     * Chiude il Fragment.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        connectionExecutor.shutdownNow();

        /*
         * NON chiudiamo la connessione qui se è registrata
         * nel ConnectionManager.
         *
         * La connessione deve rimanere disponibile
         * anche dopo aver lasciato Settings.
         */
        bluetoothConnection = null;
    }

    /**
     * Visualizza messaggio lista vuota.
     *
     * @param message messaggio.
     */
    private void showEmptyMessage(
            @NonNull String message) {

        textViewEmpty.setText(
                message
        );

        textViewEmpty.setVisibility(
                View.VISIBLE
        );

        recyclerViewDevices.setVisibility(
                View.GONE
        );
    }
}
