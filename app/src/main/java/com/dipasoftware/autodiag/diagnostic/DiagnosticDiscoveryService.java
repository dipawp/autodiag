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
 * Coordina la procedura di identificazione automatica.
 *
 * Flusso:
 *
 * 1. lettura VIN;
 * 2. filtro del catalogo tramite VIN;
 * 3. identificazione ECU sulle candidate;
 * 4. matching degli identificativi ECU;
 * 5. produzione di DiagnosticDiscoveryResult.
 *
 * La classe NON implementa la comunicazione direttamente.
 *
 * Tutte le richieste passano da DiagnosticPidExecutor e quindi
 * dalla ReadOnlyDiagnosticPolicy.
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
     * Esecutore diagnostico.
     */
    @NonNull
    private final DiagnosticPidExecutor executor;

    /**
     * Costruttore.
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
     * La procedura utilizza il VIN come primo filtro.
     *
     * Se il VIN non è disponibile oppure non produce candidate,
     * non viene effettuata una scansione indiscriminata di tutte
     * le ECU presenti nel catalogo.
     *
     * In questo caso viene restituito un risultato parziale e
     * l'applicazione potrà successivamente proporre la selezione
     * manuale.
     *
     * @return risultato discovery.
     */
    @NonNull
    public DiagnosticDiscoveryResult discover() {

        VehicleIdentification vehicleIdentification =
                vehicleIdentifier.identify();

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

        List<EcuDefinition> vehicleCandidates =
                vehicleCatalogMatcher.findCandidates(
                        vehicleIdentification,
                        catalog
                );

        /*
         * Senza un VIN non facciamo una scansione cieca
         * di tutte le ECU del catalogo.
         *
         * Questo è intenzionale: il numero di possibili target
         * crescerà enormemente con il catalogo multi-marca.
         */
        if (vehicleCandidates.isEmpty()) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null
            );
        }

        /*
         * Se abbiamo una sola ECU candidata possiamo procedere
         * con la lettura dei relativi identificatori.
         *
         * Se ne abbiamo più di una, per ora non interroghiamo
         * automaticamente tutte le candidate: il target/addressing
         * reale verrà gestito dal transport layer.
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

        EcuIdentifier ecuIdentifier =
                new EcuIdentifier(
                        executor
                );

        EcuIdentification ecuIdentification =
                ecuIdentifier.identify(
                        candidate
                );

        EcuMatchResult matchResult =
                ecuCatalogMatcher.match(
                        ecuIdentification,
                        vehicleCandidates
                );

        return new DiagnosticDiscoveryResult(
                vehicleIdentification,
                vehicleCandidates,
                ecuIdentification,
                matchResult
        );
    }
}