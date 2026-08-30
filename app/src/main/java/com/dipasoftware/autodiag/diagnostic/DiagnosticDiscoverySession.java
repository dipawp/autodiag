package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * ****************************************************************************
 *
 * Classe.....: DiagnosticDiscoverySession
 *
 * Tipo.......: Service
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta una singola sessione di discovery diagnostica.
 *
 * La sessione condivide lo stesso DiagnosticPidExecutor tra:
 *
 * - VehicleIdentifier;
 * - EcuIdentifier.
 *
 * In questo modo tutti i componenti utilizzano lo stesso:
 *
 * - DiagnosticTransport;
 * - stato del transport;
 * - stato del target;
 * - configurazione dell'adapter.
 *
 * La classe NON esegue direttamente comunicazioni.
 *
 * ****************************************************************************
 */
public class DiagnosticDiscoverySession {

    /**
     * Executor diagnostico condiviso.
     */
    @NonNull
    private final DiagnosticPidExecutor executor;

    /**
     * Identificatore veicolo.
     */
    @NonNull
    private final VehicleIdentifier vehicleIdentifier;

    /**
     * Identificatore ECU.
     */
    @NonNull
    private final EcuIdentifier ecuIdentifier;

    /**
     * Indica se la sessione è stata chiusa.
     */
    private boolean closed;

    /**
     * Costruttore.
     *
     * @param executor executor condiviso.
     */
    public DiagnosticDiscoverySession(
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

        this.closed =
                false;
    }

    /**
     * Identifica il veicolo utilizzando il VIN.
     *
     * @return identificazione veicolo.
     *
     * @throws IllegalStateException sessione chiusa.
     */
    @NonNull
    public VehicleIdentification identifyVehicle() {

        checkOpen();

        return vehicleIdentifier.identify();
    }

    /**
     * Identifica una ECU utilizzando la definizione del catalogo.
     *
     * Il target viene quindi ottenuto dalla EcuDefinition.
     *
     * @param ecuDefinition definizione ECU.
     *
     * @return identificazione ECU.
     *
     * @throws IllegalStateException sessione chiusa.
     */
    @NonNull
    public EcuIdentification identifyEcu(
            @NonNull EcuDefinition ecuDefinition) {

        checkOpen();

        return ecuIdentifier.identify(
                ecuDefinition
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
     * Restituisce il VehicleIdentifier della sessione.
     *
     * Package-private per test e diagnostica.
     *
     * @return identifier.
     */
    @NonNull
    VehicleIdentifier getVehicleIdentifier() {

        return vehicleIdentifier;
    }

    /**
     * Restituisce l'EcuIdentifier della sessione.
     *
     * Package-private per test e diagnostica.
     *
     * @return identifier.
     */
    @NonNull
    EcuIdentifier getEcuIdentifier() {

        return ecuIdentifier;
    }

    /**
     * Chiude la sessione.
     *
     * La chiusura:
     *
     * - non invia comandi diagnostici;
     * - non invia comandi di scrittura;
     * - non forza una riconfigurazione.
     *
     * In questa fase marca semplicemente la sessione come chiusa.
     */
    public void close() {

        closed =
                true;
    }

    /**
     * Indica se la sessione è chiusa.
     *
     * @return true se chiusa.
     */
    public boolean isClosed() {

        return closed;
    }

    /**
     * Indica se la sessione è attiva.
     *
     * @return true se aperta.
     */
    public boolean isOpen() {

        return !closed;
    }

    /**
     * Verifica che la sessione sia aperta.
     *
     * @throws IllegalStateException sessione chiusa.
     */
    private void checkOpen() {

        if (closed) {

            throw new IllegalStateException(
                    "Sessione discovery già chiusa."
            );
        }
    }
}