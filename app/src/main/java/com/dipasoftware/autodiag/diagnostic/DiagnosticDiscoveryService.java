package com.dipasoftware.autodiag.diagnostic;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
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
 * 2. tentativo di lettura VIN;
 * 3. filtro del catalogo tramite VIN, quando disponibile;
 * 4. fallback alla discovery ECU quando il VIN non è disponibile;
 * 5. identificazione ECU;
 * 6. matching ECU;
 * 7. risultato discovery.
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
     * Se il VIN è disponibile viene utilizzato per restringere
     * il catalogo.
     *
     * Se il VIN non è disponibile, ad esempio perché la vettura
     * restituisce 7F 09 12 a fronte di 0902, viene utilizzato
     * il fallback ECU discovery.
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

            /*
             * Il VIN non è disponibile.
             *
             * Non interrompiamo la discovery:
             * useremo il fallback ECU.
             */
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
         * ---------------------------------------------------------
         * FALLBACK ECU DISCOVERY
         * ---------------------------------------------------------
         *
         * Se il VIN non ha prodotto candidate, non usciamo più
         * immediatamente.
         *
         * Cerchiamo nel catalogo le ECU per le quali esiste
         * almeno una strategia di identificazione.
         *
         * In questo modo EcuIdentifier può interrogare:
         *
         * 22 F190
         * 22 F187
         * 22 F188
         * ...
         *
         * usando il target dichiarato dalla singola ECU.
         */
        boolean usingEcuDiscoveryFallback =
                vehicleCandidates.isEmpty();

        if (usingEcuDiscoveryFallback) {

            vehicleCandidates =
                    findDiscoveryCandidates(
                            catalog
                    );
        }

        /*
         * Nessuna ECU interrogabile.
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
         * ---------------------------------------------------------
         * MULTIPLE CANDIDATES
         * ---------------------------------------------------------
         *
         * Caso normale con VIN:
         *
         * più candidate -> non scegliamo arbitrariamente.
         *
         * Caso fallback ECU:
         *
         * possiamo provare le candidate una alla volta perché
         * questa è precisamente la fase di discovery.
         */
        if (!usingEcuDiscoveryFallback
                &&
                vehicleCandidates.size() != 1) {

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

        if (usingEcuDiscoveryFallback) {

            return discoverEcuWithoutVin(
                    vehicleIdentification,
                    vehicleCandidates
            );
        }

        /*
         * ---------------------------------------------------------
         * IDENTIFICAZIONE ECU CON VIN
         * ---------------------------------------------------------
         */

        EcuDefinition candidate =
                vehicleCandidates.get(0);

        EcuIdentification ecuIdentification;

        try {

            /*
             * EcuIdentifier appartiene alla stessa sessione
             * utilizzata per il VIN.
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
     * Esegue la discovery ECU quando il VIN non è disponibile.
     *
     * Ogni ECU del catalogo viene interrogata utilizzando
     * il proprio target e le proprie identification definitions.
     *
     * Il primo risultato che contiene almeno un identificatore
     * utile viene conservato.
     *
     * IMPORTANTE:
     *
     * non facciamo una selezione arbitraria:
     * tutte le candidate provengono dal catalogo.
     *
     * @param vehicleIdentification identificazione veicolo,
     *                              eventualmente priva di VIN.
     * @param candidates ECU catalogate.
     *
     * @return risultato discovery.
     */
    @NonNull
    private DiagnosticDiscoveryResult discoverEcuWithoutVin(
            @NonNull VehicleIdentification vehicleIdentification,
            @NonNull List<EcuDefinition> candidates) {

        List<EcuDefinition> successfulCandidates =
                new ArrayList<>();

        EcuIdentification firstIdentification =
                null;

        /*
         * Proviamo le ECU una alla volta.
         *
         * L'identificazione è read-only e utilizza esclusivamente
         * le richieste definite dal catalogo.
         */
        for (
                EcuDefinition candidate :
                candidates
        ) {

            try {

                EcuIdentification identification =
                        ecuIdentifier.identify(
                                candidate
                        );

                if (identification == null) {

                    continue;
                }

                /*
                 * Consideriamo utile un'identificazione che
                 * contiene almeno un valore.
                 */
                if (!hasUsefulIdentification(
                        identification
                )) {

                    continue;
                }

                successfulCandidates.add(
                        candidate
                );

                if (firstIdentification == null) {

                    firstIdentification =
                            identification;
                }

            } catch (
                    RuntimeException exception) {

                /*
                 * Una ECU non raggiungibile o non supportata
                 * non deve interrompere la discovery delle altre.
                 */
            }
        }

        /*
         * Nessuna ECU ha fornito dati identificativi.
         */
        if (successfulCandidates.isEmpty()) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    candidates,
                    null,
                    null
            );
        }

        /*
         * Se abbiamo una sola ECU che ha risposto,
         * possiamo restituirla come risultato identificato.
         *
         * Se più ECU hanno risposto, manteniamo tutte le candidate
         * e lasciamo il matcher determinare l'eventuale ambiguità.
         */
        EcuMatchResult matchResult;

        try {

            matchResult =
                    ecuCatalogMatcher.match(
                            firstIdentification,
                            successfulCandidates
                    );

        } catch (
                RuntimeException exception) {

            matchResult =
                    null;
        }

        return new DiagnosticDiscoveryResult(
                vehicleIdentification,
                successfulCandidates,
                firstIdentification,
                matchResult
        );
    }

    /**
     * Seleziona le ECU utilizzabili per la discovery senza VIN.
     *
     * Non usiamo un nuovo campo JSON.
     *
     * La presenza di identification definitions è già
     * l'indicazione che il catalogo sa come interrogare
     * l'ECU per riconoscerla.
     *
     * @param catalog catalogo completo.
     *
     * @return candidate interrogabili.
     */
    @NonNull
    private List<EcuDefinition> findDiscoveryCandidates(
            @NonNull List<EcuDefinition> catalog) {

        List<EcuDefinition> candidates =
                new ArrayList<>();

        for (
                EcuDefinition ecu :
                catalog
        ) {

            /*
             * Una ECU senza identification definitions
             * non è utile per questa fase.
             */
            if (!ecu.hasIdentificationDefinitions()) {

                continue;
            }

            /*
             * Target sempre presente nel modello.
             *
             * Non aggiungiamo quindi una configurazione
             * sintetica qui.
             */
            if (ecu.getTarget() == null) {

                continue;
            }

            candidates.add(
                    ecu
            );
        }

        return candidates;
    }

    /**
     * Determina se un'identificazione ECU contiene
     * almeno un dato utile.
     *
     * @param identification identificazione.
     *
     * @return true se almeno un identificatore è presente.
     */
    private boolean hasUsefulIdentification(
            @NonNull EcuIdentification identification) {

        /*
         * Il VIN è il campo più diretto.
         */
        if (identification.hasVin()) {

            return !identification
                    .getVin()
                    .trim()
                    .isEmpty();
        }

        /*
         * Gli altri identificatori possono essere recuperati
         * dal catalogo e sono sufficienti per continuare
         * la fase di matching.
         */
        if (identification.getEcuPartNumber() != null
                &&
                !identification
                        .getEcuPartNumber()
                        .trim()
                        .isEmpty()) {

            return true;
        }

        if (identification.getEcuSoftwareNumber() != null
                &&
                !identification
                        .getEcuSoftwareNumber()
                        .trim()
                        .isEmpty()) {

            return true;
        }

        if (identification.getEcuHardwareNumber() != null
                &&
                !identification
                        .getEcuHardwareNumber()
                        .trim()
                        .isEmpty()) {

            return true;
        }

        if (identification.getSupplier() != null
                &&
                !identification
                        .getSupplier()
                        .trim()
                        .isEmpty()) {

            return true;
        }

        return false;
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
