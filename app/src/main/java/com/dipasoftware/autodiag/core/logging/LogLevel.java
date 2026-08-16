package com.dipasoftware.autodiag.core.logging;

/******************************************************************************
 *
 * Classe.....: LogLevel
 *
 * Package....: com.dipasoftware.autodiag.core.logging
 *
 * Autore.....: DiPa Software
 *
 * Data.......: 04/08/2026
 *
 * Descrizione:
 * Enumerazione che rappresenta il livello di gravità
 * di un messaggio di log.
 *
 * Ogni messaggio registrato nell'applicazione avrà
 * uno di questi livelli.
 *
 ******************************************************************************/
public enum LogLevel {

    /**
     * Informazioni dettagliate utilizzate durante lo sviluppo.
     */
    DEBUG,

    /**
     * Informazioni generali sul normale funzionamento dell'app.
     */
    INFO,

    /**
     * Situazioni anomale ma non bloccanti.
     */
    WARNING,

    /**
     * Errori recuperabili.
     */
    ERROR,

    /**
     * Errori critici che possono compromettere il funzionamento.
     */
    FATAL

}