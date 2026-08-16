package com.dipasoftware.autodiag.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/******************************************************************************
 *
 * Classe.....: BluetoothDeviceInfo
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Rappresenta le informazioni pubbliche di un dispositivo
 * Bluetooth associato.
 *
 * La classe evita di esporre direttamente l'oggetto
 * Android BluetoothDevice al resto dell'applicazione.
 *
 ******************************************************************************/
public class BluetoothDeviceInfo {

    /**
     * Nome del dispositivo Bluetooth.
     */
    @Nullable
    private final String name;

    /**
     * Indirizzo MAC del dispositivo Bluetooth.
     */
    @NonNull
    private final String address;

    /**
     * Costruttore.
     *
     * @param name nome del dispositivo.
     * @param address indirizzo MAC.
     */
    public BluetoothDeviceInfo(
            @Nullable String name,
            @NonNull String address) {

        this.name = name;
        this.address = address;
    }

    /**
     * Restituisce il nome del dispositivo.
     *
     * @return nome oppure null se non disponibile.
     */
    @Nullable
    public String getName() {

        return name;
    }

    /**
     * Restituisce l'indirizzo MAC.
     *
     * @return indirizzo MAC.
     */
    @NonNull
    public String getAddress() {

        return address;
    }

    /**
     * Restituisce il nome da visualizzare.
     *
     * Se il nome non è disponibile viene utilizzato
     * l'indirizzo MAC.
     *
     * @return descrizione del dispositivo.
     */
    @NonNull
    public String getDisplayName() {

        if (name != null && !name.trim().isEmpty()) {
            return name;
        }

        return address;
    }

    /**
     * Rappresentazione testuale del dispositivo.
     *
     * @return nome e indirizzo.
     */
    @NonNull
    @Override
    public String toString() {

        return getDisplayName() + " [" + address + "]";
    }
}