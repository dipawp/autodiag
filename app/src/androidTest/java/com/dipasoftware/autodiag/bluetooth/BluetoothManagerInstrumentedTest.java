package com.dipasoftware.autodiag.bluetooth;

import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/******************************************************************************
 *
 * Classe.....: BluetoothManagerInstrumentedTest
 *
 * Tipo.......: Android Instrumentation Test
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Verifica BluetoothManager utilizzando un Context Android reale.
 *
 * Il test non apre connessioni Bluetooth e non comunica
 * con l'ELM327.
 *
 ******************************************************************************/
@RunWith(AndroidJUnit4.class)
public class BluetoothManagerInstrumentedTest {

    /**
     * Verifica che BluetoothManager possa essere creato
     * con il Context reale dell'applicazione.
     */
    @Test
    public void testBluetoothManagerCreation() {

        Context context =
                ApplicationProvider.getApplicationContext();

        BluetoothManager manager =
                new BluetoothManager(context);

        assertNotNull(manager);
    }

    /**
     * Verifica la lettura dei dispositivi Bluetooth associati.
     *
     * Il risultato può essere una lista vuota se non sono
     * presenti dispositivi associati.
     *
     * Non consideriamo quindi la lista vuota come errore.
     */
    @Test
    public void testGetPairedDevices() {

        Context context =
                ApplicationProvider.getApplicationContext();

        BluetoothManager manager =
                new BluetoothManager(context);

        List<BluetoothDeviceInfo> devices =
                manager.getPairedDevices();

        assertNotNull(devices);
    }
}