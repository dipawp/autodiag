package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Interfaccia...: ObdCommandExecutor
 *
 * Tipo..........: Abstraction
 *
 * Package.......: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Definisce il meccanismo utilizzato dal livello diagnostico
 * per inviare un comando OBD-II e ricevere la risposta dall'ECU.
 *
 * L'implementazione concreta sarà collegata al livello ELM327
 * e alla connessione Bluetooth.
 *
 * In questo modo PidSupportScanner non deve conoscere:
 *
 * - Bluetooth
 * - socket
 * - InputStream
 * - OutputStream
 * - ELM327
 *
 * Deve solamente poter inviare una richiesta OBD-II.
 *
 * ****************************************************************************
 */
public interface ObdCommandExecutor {

    /**
     * Invia un comando OBD-II all'ECU.
     *
     * @param command comando senza necessariamente il terminatore
     *                utilizzato dal livello ELM327.
     *
     * @return risposta testuale ricevuta.
     *
     * @throws Exception errore di comunicazione.
     */
    @NonNull
    String execute(
            @NonNull String command)
            throws Exception;
}