package com.dipasoftware.autodiag.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/******************************************************************************
 *
 * Classe.....: BluetoothPermissionManager
 *
 * Tipo.......: Classe di servizio
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Gestisce esclusivamente i permessi Android necessari
 * alle operazioni Bluetooth.
 *
 * La classe NON:
 *
 * - gestisce dispositivi Bluetooth;
 * - apre connessioni;
 * - gestisce ELM327;
 * - gestisce OBD;
 * - gestisce PID;
 * - gestisce ECU.
 *
 ******************************************************************************/
public class BluetoothPermissionManager {

    /**
     * Request code utilizzato per la richiesta dei permessi.
     */
    public static final int REQUEST_CODE_BLUETOOTH = 1001;

    /**
     * Restituisce i permessi necessari in base alla versione
     * Android.
     *
     * Android 12+:
     *
     * - BLUETOOTH_SCAN
     * - BLUETOOTH_CONNECT
     *
     * Android 11 e precedenti:
     *
     * - ACCESS_FINE_LOCATION
     *
     * @return array dei permessi necessari.
     */
    @NonNull
    public String[] getRequiredPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }

        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        };
    }

    /**
     * Verifica se tutti i permessi Bluetooth necessari
     * sono già stati concessi.
     *
     * @param activity Activity utilizzata per il controllo.
     *
     * @return true se tutti i permessi sono disponibili.
     */
    public boolean hasRequiredPermissions(
            @NonNull Activity activity) {

        String[] permissions =
                getRequiredPermissions();

        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }

        return true;
    }

    /**
     * Richiede all'utente i permessi Bluetooth necessari.
     *
     * Se i permessi sono già disponibili non viene eseguita
     * alcuna richiesta.
     *
     * @param activity Activity che riceverà il risultato.
     */
    public void requestPermissions(
            @NonNull Activity activity) {

        if (hasRequiredPermissions(activity)) {
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                getRequiredPermissions(),
                REQUEST_CODE_BLUETOOTH
        );
    }

    /**
     * Verifica il risultato della richiesta dei permessi.
     *
     * @param requestCode codice della richiesta.
     * @param grantResults risultati restituiti da Android.
     *
     * @return true se la richiesta corrisponde a quella
     * gestita da questa classe e tutti i permessi sono stati
     * concessi.
     */
    public boolean handlePermissionResult(
            int requestCode,
            @Nullable int[] grantResults) {

        if (requestCode != REQUEST_CODE_BLUETOOTH) {
            return false;
        }

        if (grantResults == null
                || grantResults.length == 0) {

            return false;
        }

        for (int result : grantResults) {

            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}