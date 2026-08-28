package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: VehicleCatalogMatcher
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Utilizza le informazioni del veicolo già lette dalla vettura
 * per restringere il catalogo ECU alle definizioni compatibili.
 *
 * In questa fase il matcher utilizza il VIN come filtro.
 *
 * IMPORTANTE:
 *
 * Il VIN NON identifica direttamente una ECU.
 *
 * Il risultato di questa classe è quindi un insieme di ECU candidate.
 *
 * La successiva identificazione della ECU verrà effettuata tramite
 * EcuIdentifier utilizzando gli identificatori hardware/software/
 * part-number dichiarati nella relativa EcuDefinition.
 *
 * ****************************************************************************
 */
public class VehicleCatalogMatcher {

    /**
     * Cerca tutte le ECU il cui profilo di catalogo contiene
     * un VIN pattern compatibile.
     *
     * @param vehicleIdentification identificazione veicolo.
     * @param definitions catalogo ECU.
     *
     * @return ECU candidate ordinate nell'ordine del catalogo.
     */
    @NonNull
    public List<EcuDefinition> findCandidates(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> definitions) {

        if (!vehicleIdentification.hasVin()) {

            return Collections.emptyList();
        }

        String vin =
                vehicleIdentification.getVin();

        List<EcuDefinition> result =
                new ArrayList<>();

        for (
                EcuDefinition definition :
                definitions
        ) {

            if (definition.getIdentifiers()
                    .matchesVin(
                            vin
                    )) {

                result.add(
                        definition
                );
            }
        }

        return Collections.unmodifiableList(
                result
        );
    }

    /**
     * Cerca una singola ECU quando il VIN produce
     * una sola corrispondenza.
     *
     * Questo metodo NON seleziona arbitrariamente una ECU
     * quando esistono più candidate.
     *
     * @param vehicleIdentification identificazione veicolo.
     * @param definitions catalogo ECU.
     *
     * @return ECU unica oppure null.
     */
    public EcuDefinition findUniqueCandidate(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> definitions) {

        List<EcuDefinition> candidates =
                findCandidates(
                        vehicleIdentification,
                        definitions
                );

        if (candidates.size() != 1) {

            return null;
        }

        return candidates.get(0);
    }

    /**
     * Indica se il VIN identifica una sola candidate
     * nel catalogo.
     *
     * @param vehicleIdentification identificazione veicolo.
     * @param definitions catalogo.
     *
     * @return true se esiste una sola candidate.
     */
    public boolean hasUniqueCandidate(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> definitions) {

        return findCandidates(
                vehicleIdentification,
                definitions
        ).size() == 1;
    }
}