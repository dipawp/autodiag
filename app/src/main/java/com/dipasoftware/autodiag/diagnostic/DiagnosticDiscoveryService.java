package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoveryService
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Coordina la procedura di identificazione automatica del veicolo
 * e della ECU.
 *
 * Flusso:
 *
 * 1. lettura VIN;
 * 2. filtro del catalogo tramite VIN;
 * 3. identificazione ECU sulle candidate;
 * 4. matching degli identificativi ECU;
 * 5. produzione del risultato finale.
 *
 * VehicleIdentifier ed EcuIdentifier condividono lo stesso
 * DiagnosticPidExecutor quando viene utilizzato il percorso
 * catalog-driven.
 *
 * ****************************************************************************
 */
public class DiagnosticDiscoveryService {

    /**
     * Identificatore veicolo.
     */
    @NonNull
    private final VehicleIdentifier vehicleIdentifier;

    /**
     * Repository catalogo ECU.
     */
    @NonNull
    private final EcuCatalogRepository ecuCatalogRepository;

    /**
     * Matcher veicolo.
     */
    @NonNull
    private final VehicleCatalogMatcher vehicleCatalogMatcher;

    /**
     * Matcher ECU.
     */
    @NonNull
    private final EcuCatalogMatcher ecuCatalogMatcher;

    /**
     * Executor diagnostico condiviso.
     */
    @NonNull
    private final DiagnosticPidExecutor executor;

    /**
     * Identificatore ECU.
     */
    @NonNull
    private final EcuIdentifier ecuIdentifier;

    /**
     * Costruttore compatibile.
     *
     * Crea VehicleIdentifier ed EcuIdentifier utilizzando
     * lo stesso DiagnosticPidExecutor.
     *
     * @param context context Android.
     * @param executor executor diagnostico.
     */
    public DiagnosticDiscoveryService(
            @NonNull Context context,
            @NonNull DiagnosticPidExecutor executor) {

        this.executor =
                executor;

        this.vehicleIdentifier =
                new VehicleIdentifier(
                        executor
                );

        this.ecuIdentifier =
                new EcuIdentifier(
                        executor
                );

        this.ecuCatalogRepository =
                new EcuCatalogRepository(
                        context
                );

        this.vehicleCatalogMatcher =
                new VehicleCatalogMatcher();

        this.ecuCatalogMatcher =
                new EcuCatalogMatcher();
    }

    /**
     * Costruttore con EcuIdentifier iniettato.
     *
     * Mantenuto per compatibilità con il codice già esistente
     * e per test specifici.
     *
     * IMPORTANTE:
     *
     * Il chiamante deve fornire un EcuIdentifier costruito
     * con lo stesso DiagnosticPidExecutor passato come parametro.
     *
     * @param context context Android.
     * @param executor executor diagnostico.
     * @param ecuIdentifier identificatore ECU.
     */
    public DiagnosticDiscoveryService(
            @NonNull Context context,
            @NonNull DiagnosticPidExecutor executor,
            @NonNull EcuIdentifier ecuIdentifier) {

        this.executor =
                executor;

        this.vehicleIdentifier =
                new VehicleIdentifier(
                        executor
                );

        this.ecuIdentifier =
                ecuIdentifier;

        this.ecuCatalogRepository =
                new EcuCatalogRepository(
                        context
                );

        this.vehicleCatalogMatcher =
                new VehicleCatalogMatcher();

        this.ecuCatalogMatcher =
                new EcuCatalogMatcher();
    }

    /**
     * Esegue la discovery automatica.
     *
     * @return risultato discovery.
     */
    @NonNull
    public DiagnosticDiscoveryResult discover() {

        VehicleIdentification vehicleIdentification;

        try {

            vehicleIdentification =
                    vehicleIdentifier.identify();

        } catch (
                RuntimeException exception) {

            vehicleIdentification =
                    new VehicleIdentification(
                            ""
                    );
        }

        List<EcuDefinition> catalog;

        try {

            catalog =
                    ecuCatalogRepository.loadAll();

        } catch (
                Exception exception) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    Collections.emptyList(),
                    null,
                    null
            );
        }

        List<EcuDefinition> vehicleCandidates;

        try {

            vehicleCandidates =
                    vehicleCatalogMatcher.findCandidates(
                            vehicleIdentification,
                            catalog
                    );

        } catch (
                RuntimeException exception) {

            vehicleCandidates =
                    Collections.emptyList();
        }

        if (vehicleCandidates.isEmpty()) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null
            );
        }

        /*
         * Se rimangono più ECU candidate, non eseguiamo ancora
         * una scansione indiscriminata.
         */
        if (vehicleCandidates.size() != 1) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null
            );
        }

        EcuDefinition candidate =
                vehicleCandidates.get(0);

        EcuIdentification ecuIdentification;

        try {

            ecuIdentification =
                    ecuIdentifier.identify(
                            candidate
                    );

        } catch (
                RuntimeException exception) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null
            );
        }

        EcuMatchResult ecuMatchResult;

        try {

            ecuMatchResult =
                    ecuCatalogMatcher.match(
                            ecuIdentification,
                            vehicleCandidates
                    );

        } catch (
                RuntimeException exception) {

            ecuMatchResult =
                    null;
        }

        return new DiagnosticDiscoveryResult(
                vehicleIdentification,
                vehicleCandidates,
                ecuIdentification,
                ecuMatchResult
        );
    }

    /**
     * Restituisce l'executor condiviso.
     *
     * Package-private per test e diagnostica.
     *
     * @return executor.
     */
    @NonNull
    DiagnosticPidExecutor getExecutor() {

        return executor;
    }

    /**
     * Restituisce l'EcuIdentifier utilizzato.
     *
     * Package-private per test e diagnostica.
     *
     * @return identifier ECU.
     */
    @NonNull
    EcuIdentifier getEcuIdentifier() {

        return ecuIdentifier;
    }
}