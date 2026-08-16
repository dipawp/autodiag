package com.dipasoftware.autodiag.bluetooth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.PackageManager;

import org.junit.Test;

/******************************************************************************
 *
 * Classe.....: BluetoothPermissionManagerTest
 *
 * Tipo.......: Unit Test
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Verifica la gestione dei risultati delle richieste
 * dei permessi Bluetooth.
 *
 ******************************************************************************/
public class BluetoothPermissionManagerTest {

    /**
     * Verifica che un request code diverso venga ignorato.
     */
    @Test
    public void testInvalidRequestCode() {

        BluetoothPermissionManager manager =
                new BluetoothPermissionManager();

        boolean result =
                manager.handlePermissionResult(
                        9999,
                        new int[]{
                                PackageManager.PERMISSION_GRANTED
                        }
                );

        assertFalse(result);
    }

    /**
     * Verifica che una richiesta con permesso concesso
     * venga considerata valida.
     */
    @Test
    public void testPermissionGranted() {

        BluetoothPermissionManager manager =
                new BluetoothPermissionManager();

        boolean result =
                manager.handlePermissionResult(
                        BluetoothPermissionManager
                                .REQUEST_CODE_BLUETOOTH,
                        new int[]{
                                PackageManager.PERMISSION_GRANTED
                        }
                );

        assertTrue(result);
    }

    /**
     * Verifica che una richiesta con permesso negato
     * venga considerata fallita.
     */
    @Test
    public void testPermissionDenied() {

        BluetoothPermissionManager manager =
                new BluetoothPermissionManager();

        boolean result =
                manager.handlePermissionResult(
                        BluetoothPermissionManager
                                .REQUEST_CODE_BLUETOOTH,
                        new int[]{
                                PackageManager.PERMISSION_DENIED
                        }
                );

        assertFalse(result);
    }

    /**
     * Verifica che un array vuoto venga considerato fallito.
     */
    @Test
    public void testEmptyResult() {

        BluetoothPermissionManager manager =
                new BluetoothPermissionManager();

        boolean result =
                manager.handlePermissionResult(
                        BluetoothPermissionManager
                                .REQUEST_CODE_BLUETOOTH,
                        new int[]{}
                );

        assertFalse(result);
    }
}