package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
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
 * - identificazioni ECU effettivamente raccolte;
 * - risultato del matching ECU;
 * - observations indipendenti per la discovery multi-ECU.
 *
 * La vecchia identificazione singola viene mantenuta per compatibilità
 * con il codice esistente.
 *
 * La nuova lista ecuObservations rappresenta invece il risultato completo
 * della discovery multi-ECU.
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
     * ECU candidate ottenute dal filtro veicolo.
     */
    @NonNull
    private final List<EcuDefinition> vehicleCandidates;

    /**
     * Identificazione principale della ECU.
     *
     * Mantiene la compatibilità con il modello precedente.
     */
    @Nullable
    private final EcuIdentification ecuIdentification;

    /**
     * Risultato principale del matching ECU.
     *
     * Mantiene la compatibilità con il modello precedente.
     */
    @Nullable
    private final EcuMatchResult ecuMatchResult;

    /**
     * Observation indipendenti ottenute durante la discovery ECU.
     *
     * Ogni elemento mantiene la relazione:
     *
     * candidate -> identification -> match
     */
    @NonNull
    private final List<EcuDiscoveryObservation> ecuObservations;

    /**
     * Costruttore compatibile con il modello precedente.
     *
     * Crea un risultato senza observations esplicite.
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

        this(
                vehicleIdentification,
                vehicleCandidates,
                ecuIdentification,
                ecuMatchResult,
                Collections.emptyList()
        );
    }

    /**
     * Costruttore completo.
     *
     * @param vehicleIdentification identificazione veicolo.
     * @param vehicleCandidates candidate veicolo.
     * @param ecuIdentification identificazione principale ECU.
     * @param ecuMatchResult risultato principale matching ECU.
     * @param ecuObservations observations multi-ECU.
     */
    public DiagnosticDiscoveryResult(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> vehicleCandidates,
            @Nullable EcuIdentification ecuIdentification,
            @Nullable EcuMatchResult ecuMatchResult,
            @NonNull List<EcuDiscoveryObservation> ecuObservations) {

        this.vehicleIdentification =
                vehicleIdentification;

        this.vehicleCandidates =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                vehicleCandidates
                        )
                );

        this.ecuIdentification =
                ecuIdentification;

        this.ecuMatchResult =
                ecuMatchResult;

        this.ecuObservations =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                ecuObservations
                        )
                );
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
     * Restituisce l'identificazione ECU principale.
     *
     * Mantiene la compatibilità con il modello precedente.
     *
     * @return identificazione oppure null.
     */
    @Nullable
    public EcuIdentification getEcuIdentification() {
        return ecuIdentification;
    }

    /**
     * Indica se è stata effettuata un'identificazione ECU principale.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuIdentification() {
        return ecuIdentification != null;
    }

    /**
     * Restituisce il risultato principale del matching ECU.
     *
     * Mantiene la compatibilità con il modello precedente.
     *
     * @return risultato oppure null.
     */
    @Nullable
    public EcuMatchResult getEcuMatchResult() {
        return ecuMatchResult;
    }

    /**
     * Indica se è disponibile un risultato ECU principale.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuMatchResult() {
        return ecuMatchResult != null;
    }

    /**
     * Restituisce tutte le observations ECU.
     *
     * Ogni observation rappresenta una ECU interrogata
     * e conserva separatamente la relativa identificazione
     * e il relativo risultato di matching.
     *
     * @return lista immutabile delle observations.
     */
    @NonNull
    public List<EcuDiscoveryObservation> getEcuObservations() {
        return ecuObservations;
    }

    /**
     * Indica se esistono observations ECU.
     *
     * @return true se almeno una ECU è stata osservata.
     */
    public boolean hasEcuObservations() {
        return !ecuObservations.isEmpty();
    }

    /**
     * Indica se la discovery ha identificato automaticamente
     * una ECU in modo sufficientemente sicuro.
     *
     * Regole:
     *
     * 1. Se esistono observations:
     *
     *      - deve essercene esattamente una;
     *      - deve avere un match;
     *      - il match deve consentire l'autoselezione.
     *
     * 2. Se non esistono observations:
     *
     *      viene mantenuto il comportamento legacy basato
     *      su ecuMatchResult.
     *
     * In particolare, la presenza di più observations impedisce
     * sempre l'autoselezione automatica.
     *
     * @return true se autoselezione possibile.
     */
    public boolean isAutoSelectionSafe() {

        if (!ecuObservations.isEmpty()) {

            if (ecuObservations.size() != 1) {
                return false;
            }

            EcuDiscoveryObservation observation =
                    ecuObservations.get(0);

            EcuMatchResult matchResult =
                    observation.getMatchResult();

            return matchResult != null
                    &&
                    matchResult.isAutoSelectionSafe();
        }

        return ecuMatchResult != null
                &&
                ecuMatchResult.isAutoSelectionSafe();
    }
}
