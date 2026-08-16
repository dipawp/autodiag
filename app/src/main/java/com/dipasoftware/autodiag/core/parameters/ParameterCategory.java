package com.dipasoftware.autodiag.core.parameters;

/******************************************************************************
 *
 * Classe.....: ParameterCategory
 *
 * Tipo.......: Enum
 *
 * Package....: com.dipasoftware.autodiag.core.parameters
 *
 * Descrizione:
 *
 * Identifica la categoria logica di un parametro diagnostico.
 *
 * La categoria viene utilizzata esclusivamente per organizzare
 * i parametri nell'applicazione.
 *
 ******************************************************************************/
public enum ParameterCategory {

    /**
     * Parametri del motore.
     */
    ENGINE,

    /**
     * Parametri del DPF.
     */
    DPF,

    /**
     * Parametri del turbo.
     */
    TURBO,

    /**
     * Parametri dell'alimentazione.
     */
    FUEL,

    /**
     * Parametri relativi alle emissioni.
     */
    EMISSIONS,

    /**
     * Parametri della trasmissione.
     */
    TRANSMISSION,

    /**
     * Parametri dell'impianto frenante.
     */
    BRAKES,

    /**
     * Parametri ABS.
     */
    ABS,

    /**
     * Parametri Airbag.
     */
    AIRBAG,

    /**
     * Parametri della batteria.
     */
    BATTERY,

    /**
     * Parametri di comfort.
     */
    COMFORT,

    /**
     * Informazioni veicolo.
     */
    VEHICLE,

    /**
     * Diagnostica generale.
     */
    DIAGNOSTIC
}