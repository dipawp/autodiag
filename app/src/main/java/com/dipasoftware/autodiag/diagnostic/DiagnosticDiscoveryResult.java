package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryResult
 *
 * Tipo.......: Model
 *
 * Descrizione:
 *
 * Rappresenta il risultato complessivo della procedura di discovery.
 *
 * La discovery distingue:
 *
 * - identificazione veicolo;
 * - ECU candidate ottenute dal catalogo tramite VIN;
 * - identificazione ECU effettivamente raccolta;
 * - risultato finale del matching ECU.
 *
 * ****************************************************************************
 */
public class DiagnosticDiscoveryResult {

    /**
     * VIN rilevato.
     */
    @NonNull
    private final VehicleIdentification vehicleIdentification;

    /**
     * ECU candidate ottenute dal catalogo.
     */
    @NonNull
    private final List<EcuDefinition> vehicleCandidates;

    /**
     * Identificazione letta dalla ECU.
     *
     * Può essere null quando nessuna ECU è stata interrogata.
     */
    @Nullable
    private final EcuIdentification ecuIdentification;

    /**
     * Risultato del matching ECU.
     *
     * Può essere null quando nessuna ECU è stata interrogata.
     */
    @Nullable
    private final EcuMatchResult ecuMatchResult;

    /**
     * Costruttore.
     *
     * @param vehicleIdentification identificazione veicolo.
     * @param vehicleCandidates candidate veicolo.
     * @param ecuIdentification identificazione ECU.
     * @param ecuMatchResult risultato matching ECU.
     */
    public DiagnosticDiscoveryResult(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> vehicleCandidates,
            @Nullable EcuIdentification ecuIdentification,
            @Nullable EcuMatchResult ecuMatchResult) {

        this.vehicleIdentification =
                vehicleIdentification;

        this.vehicleCandidates =
                Collections.unmodifiableList(
                        vehicleCandidates
                );

        this.ecuIdentification =
                ecuIdentification;

        this.ecuMatchResult =
                ecuMatchResult;
    }

    /**
     * Restituisce l'identificazione veicolo.
     *
     * @return identificazione.
     */
    @NonNull
    public VehicleIdentification getVehicleIdentification() {

        return vehicleIdentification;
    }

    /**
     * Restituisce le ECU candidate dal filtro veicolo.
     *
     * @return candidate.
     */
    @NonNull
    public List<EcuDefinition> getVehicleCandidates() {

        return vehicleCandidates;
    }

    /**
     * Indica se esistono candidate ECU dal VIN.
     *
     * @return true se presenti.
     */
    public boolean hasVehicleCandidates() {

        return !vehicleCandidates.isEmpty();
    }

    /**
     * Restituisce l'identificazione ECU.
     *
     * @return identificazione oppure null.
     */
    @Nullable
    public EcuIdentification getEcuIdentification() {

        return ecuIdentification;
    }

    /**
     * Indica se è stata effettuata l'identificazione ECU.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuIdentification() {

        return ecuIdentification != null;
    }

    /**
     * Restituisce il risultato del matching ECU.
     *
     * @return risultato oppure null.
     */
    @Nullable
    public EcuMatchResult getEcuMatchResult() {

        return ecuMatchResult;
    }

    /**
     * Indica se è disponibile un risultato ECU.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuMatchResult() {

        return ecuMatchResult != null;
    }

    /**
     * Indica se la discovery ha identificato automaticamente
     * una ECU in modo sufficientemente sicuro.
     *
     * @return true se autoselezione possibile.
     */
    public boolean isAutoSelectionSafe() {

        return ecuMatchResult != null
                &&
                ecuMatchResult.isAutoSelectionSafe();
    }
}