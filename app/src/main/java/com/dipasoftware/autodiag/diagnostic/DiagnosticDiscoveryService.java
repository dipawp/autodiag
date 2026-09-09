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
 * e delle ECU utilizzando una singola DiagnosticDiscoverySession.
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
 * La discovery ECU senza VIN conserva ora una EcuDiscoveryObservation
 * indipendente per ogni ECU interrogata.
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
     * Se il VIN non è disponibile viene utilizzato il fallback
     * ECU discovery.
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
                    null,
                    Collections.emptyList()
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
         * Se il VIN non ha prodotto candidate, non usciamo.
         *
         * Cerchiamo nel catalogo le ECU per le quali esiste
         * almeno una strategia di identificazione e un target.
         *
         * Ogni candidata verrà interrogata separatamente.
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
         * ---------------------------------------------------------
         * NESSUNA ECU INTERROGABILE
         * ---------------------------------------------------------
         */

        if (vehicleCandidates.isEmpty()) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null,
                    Collections.emptyList()
            );
        }

        /*
         * ---------------------------------------------------------
         * MULTIPLE CANDIDATES
         * ---------------------------------------------------------
         *
         * Con VIN:
         *
         * più candidate -> non scegliamo arbitrariamente.
         *
         * Senza VIN:
         *
         * possiamo invece provarle una alla volta perché questa
         * è precisamente la fase di discovery.
         */

        if (!usingEcuDiscoveryFallback
                &&
                vehicleCandidates.size() != 1) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    vehicleCandidates,
                    null,
                    null,
                    Collections.emptyList()
            );
        }

        /*
         * ---------------------------------------------------------
         * IDENTIFICAZIONE ECU SENZA VIN
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
                    null,
                    Collections.emptyList()
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

        /*
         * Anche il percorso VIN mantiene una observation.
         *
         * In questo caso esiste una sola ECU candidata perché
         * il codice precedente richiede esattamente una candidate.
         */
        List<EcuDiscoveryObservation> observations =
                new ArrayList<>();

        observations.add(
                new EcuDiscoveryObservation(
                        candidate,
                        ecuIdentification,
                        ecuMatchResult
                )
        );

        return new DiagnosticDiscoveryResult(
                vehicleIdentification,
                vehicleCandidates,
                ecuIdentification,
                ecuMatchResult,
                observations
        );
    }

    /**
     * Esegue la discovery ECU quando il VIN non è disponibile.
     *
     * Ogni ECU del catalogo viene interrogata utilizzando
     * il proprio target e le proprie identification definitions.
     *
     * Ogni identificazione valida viene conservata insieme
     * alla relativa ECU candidata.
     *
     * Il matching viene eseguito per ogni singola observation.
     *
     * Questo evita di associare l'identificazione della prima ECU
     * a tutte le ECU che hanno risposto.
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

        /*
         * Candidate che hanno fornito una identificazione utile.
         */
        List<EcuDefinition> successfulCandidates =
                new ArrayList<>();

        /*
         * Una observation indipendente per ogni ECU.
         */
        List<EcuDiscoveryObservation> observations =
                new ArrayList<>();

        /*
         * ---------------------------------------------------------
         * PROBE ECU
         * ---------------------------------------------------------
         *
         * Ogni candidate viene interrogata separatamente.
         *
         * EcuIdentifier utilizza il target e le identification
         * definitions appartenenti alla candidate stessa.
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

                /*
                 * Nessuna identificazione.
                 */
                if (identification == null) {

                    continue;
                }

                /*
                 * Nessun dato identificativo utile.
                 */
                if (!hasUsefulIdentification(
                        identification
                )) {

                    continue;
                }

                /*
                 * La candidate ha risposto con dati utili.
                 */
                successfulCandidates.add(
                        candidate
                );

                /*
                 * -------------------------------------------------
                 * MATCHING DELLA SINGOLA OBSERVATION
                 * -------------------------------------------------
                 *
                 * In questa fase non mischiamo le identificazioni
                 * provenienti da ECU differenti.
                 *
                 * La candidate interrogata viene confrontata
                 * con la propria identificazione.
                 */
                EcuMatchResult matchResult;

                try {

                    matchResult =
                            ecuCatalogMatcher.match(
                                    identification,
                                    Collections.singletonList(
                                            candidate
                                    )
                            );

                } catch (
                        RuntimeException exception) {

                    matchResult =
                            null;
                }

                /*
                 * Conserviamo la relazione:
                 *
                 * candidate
                 *      +
                 * identification
                 *      +
                 * match
                 */
                observations.add(
                        new EcuDiscoveryObservation(
                                candidate,
                                identification,
                                matchResult
                        )
                );

            } catch (
                    RuntimeException exception) {

                /*
                 * Una ECU non raggiungibile o non supportata
                 * non deve interrompere la discovery delle altre.
                 */
            }
        }

        /*
         * ---------------------------------------------------------
         * NESSUNA ECU HA RISPOSTO
         * ---------------------------------------------------------
         */

        if (observations.isEmpty()) {

            return new DiagnosticDiscoveryResult(
                    vehicleIdentification,
                    candidates,
                    null,
                    null,
                    Collections.emptyList()
            );
        }

        /*
         * ---------------------------------------------------------
         * RISULTATO PRINCIPALE
         * ---------------------------------------------------------
         *
         * Manteniamo il vecchio modello compatibile:
         *
         * getEcuIdentification()
         * getEcuMatchResult()
         *
         * restituiscono la prima observation.
         *
         * Il risultato completo è invece disponibile tramite:
         *
         * getEcuObservations()
         */

        EcuDiscoveryObservation firstObservation =
                observations.get(0);

        EcuIdentification firstIdentification =
                firstObservation.getIdentification();

        EcuMatchResult firstMatchResult =
                firstObservation.getMatchResult();

        return new DiagnosticDiscoveryResult(
                vehicleIdentification,
                successfulCandidates,
                firstIdentification,
                firstMatchResult,
                observations
        );
    }

    /**
     * Seleziona le ECU utilizzabili per la discovery senza VIN.
     *
     * La presenza di identification definitions indica che
     * il catalogo sa come interrogare l'ECU per riconoscerla.
     *
     * Inoltre il target deve essere disponibile.
     *
     * Non viene creato alcun target sintetico durante la discovery.
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
             * Il target deve essere dichiarato dal catalogo.
             *
             * Non creiamo configurazioni sintetiche.
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
     * Utilizza direttamente il modello EcuIdentification
     * per mantenere la stessa semantica usata dal resto
     * dell'engine.
     *
     * @param identification identificazione.
     *
     * @return true se almeno un identificatore è presente.
     */
    private boolean hasUsefulIdentification(
            @NonNull EcuIdentification identification) {

        return identification.hasUsefulIdentification();
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