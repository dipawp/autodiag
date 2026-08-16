package com.dipasoftware.autodiag.core.parameters;

/******************************************************************************
 *
 * Classe.....: ParameterId
 *
 * Package....: com.dipasoftware.autodiag.core.parameters
 *
 * Autore.....: DiPa Software
 *
 * Data.......: 04/08/2026
 *
 * Descrizione:
 * Identifica in modo univoco un parametro diagnostico.
 *
 * Questa enumerazione rappresenta solamente il nome logico
 * del parametro e NON contiene alcuna informazione relativa
 * alla centralina, ai PID, ai servizi diagnostici o alle
 * formule di conversione.
 *
 * Tutte queste informazioni saranno contenute nei profili
 * ECU caricati dai file JSON.
 *
 ******************************************************************************/
public enum ParameterId {

    /**********************************************************************
     * PARAMETRI DPF
     **********************************************************************/

    DPF_SOOT_MASS,

    DPF_SOOT_MASS_MEASURED,

    DPF_CLOGGING_PERCENTAGE,

    DPF_DIFFERENTIAL_PRESSURE,

    DPF_TEMPERATURE_INLET,

    DPF_TEMPERATURE_OUTLET,

    DPF_TEMPERATURE_CATALYST,

    DPF_REGENERATION_STATUS,

    DPF_REGENERATION_REQUEST,

    DPF_REGENERATION_COUNTER,

    DPF_DISTANCE_FROM_LAST_REGEN,

    DPF_TIME_FROM_LAST_REGEN,

    DPF_OIL_DILUTION,

    DPF_ASH_LOAD,

    /**********************************************************************
     * PARAMETRI MOTORE
     **********************************************************************/

    ENGINE_RPM,

    VEHICLE_SPEED,

    COOLANT_TEMPERATURE,

    ENGINE_LOAD,

    BATTERY_VOLTAGE,

    BOOST_PRESSURE,

    RAIL_PRESSURE,

    MAF,

    MAP,

    EGR_POSITION,

    THROTTLE_POSITION,

    ACCELERATOR_POSITION,

    FUEL_TEMPERATURE,

    AIR_TEMPERATURE

}