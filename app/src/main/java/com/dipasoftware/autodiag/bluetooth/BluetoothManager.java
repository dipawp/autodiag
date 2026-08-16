package com.dipasoftware.autodiag.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.dipasoftware.autodiag.connection.BluetoothConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/******************************************************************************
 *
 * Classe.....: BluetoothManager
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Gestisce le funzionalità Bluetooth utilizzate da AutoDiag.
 *
 * Questa classe:
 *
 * - verifica la disponibilità del Bluetooth;
 * - verifica lo stato del Bluetooth;
 * - verifica i permessi;
 * - restituisce i dispositivi associati;
 * - crea una BluetoothConnection per un dispositivo.
 *
 * NON gestisce:
 *
 * - ELM327;
 * - OBD;
 * - PID;
 * - ECU;
 * - DTC;
 * - formule diagnostiche.
 *
 ******************************************************************************/
public class BluetoothManager {

    /**
     * Context applicativo.
     */
    private final Context context;

    /**
     * Adapter Bluetooth del dispositivo Android.
     */
    @Nullable
    private final BluetoothAdapter bluetoothAdapter;

    /**
     * Costruttore.
     *
     * @param context Context dell'applicazione.
     */
    public BluetoothManager(@NonNull Context context) {

        this.context = context.getApplicationContext();

        this.bluetoothAdapter =
                BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Verifica se il dispositivo dispone di Bluetooth.
     *
     * @return true se il Bluetooth è disponibile.
     */
    public boolean isBluetoothAvailable() {

        return bluetoothAdapter != null;
    }

    /**
     * Verifica se il Bluetooth è attivo.
     *
     * @return true se il Bluetooth è attivo.
     */
    public boolean isBluetoothEnabled() {

        if (bluetoothAdapter == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!hasBluetoothConnectPermission()) {
                return false;
            }
        }

        return bluetoothAdapter.isEnabled();
    }

    /**
     * Verifica il permesso BLUETOOTH_CONNECT.
     *
     * Su Android precedenti alla versione 12 il permesso
     * non è necessario.
     *
     * @return true se il permesso è disponibile.
     */
    public boolean hasBluetoothConnectPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica il permesso BLUETOOTH_SCAN.
     *
     * @return true se il permesso è disponibile.
     */
    public boolean hasBluetoothScanPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Restituisce i dispositivi Bluetooth già associati.
     *
     * Non esegue una scansione.
     *
     * @return lista dei dispositivi associati.
     */
    @NonNull
    public List<BluetoothDeviceInfo> getPairedDevices() {

        if (bluetoothAdapter == null) {
            return Collections.emptyList();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {

                return Collections.emptyList();
            }
        }

        Set<BluetoothDevice> bondedDevices =
                bluetoothAdapter.getBondedDevices();

        if (bondedDevices == null
                || bondedDevices.isEmpty()) {

            return Collections.emptyList();
        }

        List<BluetoothDeviceInfo> devices =
                new ArrayList<>();

        for (BluetoothDevice device : bondedDevices) {

            String name = device.getName();

            String address = device.getAddress();

            devices.add(
                    new BluetoothDeviceInfo(
                            name,
                            address
                    )
            );
        }

        return devices;
    }

    /**
     * Crea una connessione Bluetooth verso il dispositivo
     * indicato.
     *
     * La connessione non viene ancora aperta.
     *
     * @param deviceInfo informazioni del dispositivo.
     *
     * @return oggetto BluetoothConnection.
     *
     * @throws IllegalArgumentException se il dispositivo
     * non viene trovato tra quelli associati.
     */
    @NonNull
    public BluetoothConnection createConnection(
            @NonNull BluetoothDeviceInfo deviceInfo) {

        if (bluetoothAdapter == null) {

            throw new IllegalStateException(
                    "Bluetooth non disponibile."
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {

                throw new SecurityException(
                        "Permesso BLUETOOTH_CONNECT non disponibile."
                );
            }
        }

        BluetoothDevice device =
                bluetoothAdapter.getRemoteDevice(
                        deviceInfo.getAddress()
                );

        if (device == null) {

            throw new IllegalArgumentException(
                    "Dispositivo Bluetooth non trovato: "
                            + deviceInfo.getAddress()
            );
        }

        return new BluetoothConnection(
                context,
                device
        );
    }
}