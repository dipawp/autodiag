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
 * e della ECU utilizzando una singola DiagnosticDiscoverySession.
 *
 * Flusso:
 *
 * 1. apertura contesto discovery;
 * 2. lettura VIN;
 * 3. filtro del catalogo tramite VIN;
 * 4. identificazione ECU;
 * 5. matching ECU;
 * 6. risultato discovery.
 *
 * VehicleIdentifier ed EcuIdentifier condividono lo stesso
 * DiagnosticPidExecutor e quindi lo stesso DiagnosticTransport.
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
     * Sessione discovery condivisa.
     */
    @NonNull
    private final DiagnosticDiscoverySession session;

    /**
     * Costruttore compatibile.
     *
     * Crea una nuova sessione utilizzando l'executor fornito.
     *
     * @param context context Android.
     * @param executor executor diagnostico.
     */
    public DiagnosticDiscoveryService(
            @NonNull Context context,
            @NonNull DiagnosticPidExecutor executor) {

        this(
                context,
                new DiagnosticDiscoverySession(
                        executor
                )
        );
    }

    /**
     * Costruttore basato direttamente sulla sessione.
     *
     * Questo è il percorso principale della nuova architettura.
     *
     * @param context context Android.
     * @param session sessione discovery.
     */
    public DiagnosticDiscoveryService(
            @NonNull Context context,
            @NonNull DiagnosticDiscoverySession session) {

        this.session =
                session;

        this.executor =
                session.getExecutor();

        this.vehicleIdentifier =
                session.getVehicleIdentifier();

        this.ecuIdentifier =
                session.getEcuIdentifier();

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
     * Costruttore compatibile con il percorso precedente che
     * iniettava direttamente EcuIdentifier.
     *
     * IMPORTANTE:
     *
     * Viene mantenuto per non rompere codice e test esistenti.
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

        this.session =
                new DiagnosticDiscoverySession(
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
     * Esegue la discovery completa.
     *
     * @return risultato discovery.
     */
    @NonNull
    public DiagnosticDiscoveryResult discover() {

        /*
         * ---------------------------------------------------------
         * SESSIONE
         * ---------------------------------------------------------
         */

        if (session.isClosed()) {

            throw new IllegalStateException(
                    "Sessione discovery già chiusa."
            );
        }

        /*
         * ---------------------------------------------------------
         * IDENTIFICAZIONE VEICOLO
         * ---------------------------------------------------------
         */

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

        /*
         * ---------------------------------------------------------
         * CATALOGO
         * ---------------------------------------------------------
         */

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

        /*
         * ---------------------------------------------------------
         * MATCHING VEICOLO
         * ---------------------------------------------------------
         */

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

        /*
         * Nessuna ECU compatibile con il VIN.
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
         * Più candidate:
         *
         * non eseguiamo ancora una scansione indiscriminata.
         */
        if (vehicleCandidates.size() != 1) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null
            );
        }

        /*
         * ---------------------------------------------------------
         * IDENTIFICAZIONE ECU
         * ---------------------------------------------------------
         */

        EcuDefinition candidate =
                vehicleCandidates.get(0);

        EcuIdentification ecuIdentification;

        try {

            /*
             * L'EcuIdentifier appartiene alla stessa sessione
             * utilizzata per il VIN.
             *
             * Questo garantisce lo stesso executor/transport.
             */
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

        /*
         * ---------------------------------------------------------
         * MATCHING ECU
         * ---------------------------------------------------------
         */

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
     * @return executor.
     */
    @NonNull
    DiagnosticPidExecutor getExecutor() {

        return executor;
    }

    /**
     * Restituisce l'EcuIdentifier utilizzato dalla discovery.
     *
     * @return identifier ECU.
     */
    @NonNull
    EcuIdentifier getEcuIdentifier() {

        return ecuIdentifier;
    }

    /**
     * Restituisce la sessione discovery.
     *
     * @return sessione.
     */
    @NonNull
    DiagnosticDiscoverySession getSession() {

        return session;
    }

    /**
     * Chiude la sessione discovery.
     *
     * Nessun comando diagnostico viene inviato.
     */
    public void close() {

        session.close();
    }

    /**
     * Indica se la discovery è ancora aperta.
     *
     * @return true se aperta.
     */
    public boolean isOpen() {

        return session.isOpen();
    }

    /**
     * Indica se la discovery è stata chiusa.
     *
     * @return true se chiusa.
     */
    public boolean isClosed() {

        return session.isClosed();
    }
}