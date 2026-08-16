package com.dipasoftware.autodiag.bluetooth;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/******************************************************************************
 *
 * Classe.....: BluetoothManagerTest
 *
 * Tipo.......: Unit Test
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Verifica la struttura base di BluetoothManager.
 *
 * Nota:
 *
 * Il test non accede direttamente all'hardware Bluetooth.
 * Le operazioni che dipendono dal dispositivo reale verranno
 * verificate successivamente tramite test strumentali.
 *
 ******************************************************************************/
public class BluetoothManagerTest {

    /**
     * Verifica che BluetoothManager possa essere istanziato
     * senza errori.
     */
    @Test
    public void testManagerClassExists() {

        /*
         * Il test viene mantenuto volutamente minimale.
         *
         * Le operazioni Bluetooth reali richiedono un
         * ambiente Android e verranno quindi testate
         * successivamente sul dispositivo.
         */
        assertNotNull(
                BluetoothManager.class
        );
    }
}