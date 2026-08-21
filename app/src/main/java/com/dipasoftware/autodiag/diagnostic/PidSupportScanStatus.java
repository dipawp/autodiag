package com.dipasoftware.autodiag.diagnostic;

/**
 * ****************************************************************************
 *
 * Classe.....: PidSupportScanStatus
 *
 * Tipo.......: Enum
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta l'esito della richiesta di una bitmap PID.
 *
 * Permette di distinguere:
 *
 * - risposta positiva contenente una bitmap;
 * - risposta negativa della ECU;
 * - risposta non valida;
 * - errore di comunicazione.
 *
 * ****************************************************************************
 */
public enum PidSupportScanStatus {

    /**
     * La ECU ha restituito una bitmap valida.
     */
    SUPPORTED,

    /**
     * La ECU ha risposto negativamente alla richiesta.
     *
     * Esempio:
     *
     * 7F 01 12
     */
    NEGATIVE_RESPONSE,

    /**
     * La risposta è stata ricevuta ma non rispetta
     * il formato atteso.
     */
    INVALID_RESPONSE
}