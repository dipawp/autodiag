package com.dipasoftware.autodiag.core.logging;

/******************************************************************************
 *
 * Classe.....: LogCategory
 *
 * Package....: com.dipasoftware.autodiag.core.logging
 *
 * Autore.....: DiPa Software
 *
 * Data.......: 04/08/2026
 *
 * Descrizione:
 * Enumerazione che identifica la categoria di un messaggio di log.
 *
 * Lo scopo è rendere immediatamente riconoscibile
 * quale componente dell'applicazione ha generato
 * il messaggio.
 *
 ******************************************************************************/
public enum LogCategory {

    /**
     * Applicazione.
     */
    APP,

    /**
     * Interfaccia utente.
     */
    UI,

    /**
     * Sistema Bluetooth.
     */
    BLUETOOTH,

    /**
     * Comunicazione con l'ELM327.
     */
    ELM327,

    /**
     * Comunicazione OBD.
     */
    OBD,

    /**
     * Motore diagnostico.
     */
    DIAGNOSTIC,

    /**
     * Parametri.
     */
    PARAMETERS,

    /**
     * Modulo DPF.
     */
    DPF,

    /**
     * Codici di errore.
     */
    DTC,

    /**
     * Database.
     */
    DATABASE,

    /**
     * Profili JSON.
     */
    JSON,

    /**
     * Rete.
     */
    NETWORK,

    /**
     * Impostazioni.
     */
    SETTINGS,

    /**
     * Prestazioni.
     */
    PERFORMANCE,

    /**
     * Sicurezza.
     */
    SECURITY

}