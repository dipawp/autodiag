package com.dipasoftware.autodiag.connection;

import java.io.IOException;

/******************************************************************************
 *
 * Classe.....: Connection
 *
 * Tipo.......: Interfaccia
 *
 * Package....: com.dipasoftware.autodiag.connection
 *
 * Descrizione:
 *
 * Definisce l'interfaccia comune per una connessione fisica.
 *
 * La diagnostica non conosce se la connessione utilizza:
 *
 * - Bluetooth;
 * - Wi-Fi;
 * - USB;
 * - altro tipo di trasporto.
 *
 * Questa interfaccia rappresenta quindi il livello astratto
 * della connessione.
 *
 ******************************************************************************/
public interface Connection {

    /**
     * Apre la connessione.
     *
     * @throws IOException se la connessione non può essere aperta.
     */
    void connect() throws IOException;

    /**
     * Chiude la connessione.
     *
     * @throws IOException se si verifica un errore durante
     * la chiusura.
     */
    void disconnect() throws IOException;

    /**
     * Verifica se la connessione è attualmente attiva.
     *
     * @return true se connessa.
     */
    boolean isConnected();

    /**
     * Invia dati attraverso la connessione.
     *
     * @param data dati da inviare.
     *
     * @throws IOException se si verifica un errore di comunicazione.
     */
    void send(String data) throws IOException;

    /**
     * Riceve dati dalla connessione.
     *
     * @return dati ricevuti.
     *
     * @throws IOException se si verifica un errore di comunicazione.
     */
    String receive() throws IOException;
}