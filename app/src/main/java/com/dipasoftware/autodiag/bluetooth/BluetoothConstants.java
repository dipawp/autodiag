package com.dipasoftware.autodiag.bluetooth;

import java.util.UUID;

/******************************************************************************
 *
 * Classe.....: BluetoothConstants
 *
 * Tipo.......: Utility
 *
 * Package....: com.dipasoftware.autodiag.bluetooth
 *
 * Descrizione:
 *
 * Contiene tutte le costanti Bluetooth utilizzate
 * dall'applicazione.
 *
 * Questa classe non contiene logica.
 *
 ******************************************************************************/
public final class BluetoothConstants {

    /**
     * UUID del profilo Serial Port Profile (SPP).
     *
     * Utilizzato dalla quasi totalità degli adattatori
     * ELM327 Bluetooth Classic.
     */
    public static final UUID SPP_UUID =
            UUID.fromString(
                    "00001101-0000-1000-8000-00805F9B34FB"
            );

    /**
     * Timeout di connessione in millisecondi.
     */
    public static final int CONNECTION_TIMEOUT = 10000;

    /**
     * Timeout di lettura in millisecondi.
     */
    public static final int READ_TIMEOUT = 3000;

    /**
     * Carriage Return utilizzato dai comandi ELM327.
     */
    public static final String CR = "\r";

    /**
     * Prompt restituito dall'ELM327.
     */
    public static final char PROMPT = '>';

    /**
     * Costruttore privato.
     */
    private BluetoothConstants() {
    }

}