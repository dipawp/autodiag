package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327AdapterConfigurator
 *
 * Tipo.......: Adapter
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Implementazione del configuratore per un adapter ELM327.
 *
 * ATTENZIONE:
 *
 * Questa prima versione NON invia ancora comandi AT.
 *
 * Memorizza soltanto la configurazione richiesta.
 *
 * Questo ci permette di separare:
 *
 * - target diagnostico;
 * - configurazione adapter;
 * - richiesta diagnostica.
 *
 * La traduzione in comandi AT verrà effettuata in un passaggio
 * successivo, dopo aver verificato esattamente il comportamento
 * dell'ELM327 utilizzato dal progetto.
 *
 * ****************************************************************************
 */
public class Elm327AdapterConfigurator
        implements DiagnosticAdapterConfigurator {

    /**
     * Target configurato.
     */
    private DiagnosticTargetDefinition configuredTarget;

    /**
     * Indica se l'adapter risulta configurato.
     */
    private boolean configured;

    /**
     * Costruttore.
     */
    public Elm327AdapterConfigurator() {

        configuredTarget =
                null;

        configured =
                false;
    }

    /**
     * Registra il target richiesto.
     *
     * In questa versione non vengono ancora inviati comandi
     * all'ELM327.
     *
     * @param target target.
     *
     * @throws IOException se il target non è valido.
     */
    @Override
    public void configure(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()) {

            throw new IOException(
                    "Il target non è compatibile con "
                            + "la configurazione ELM327 CAN."
            );
        }

        configuredTarget =
                target;

        configured =
                true;
    }

    /**
     * Rimuove il target corrente.
     *
     * @throws IOException non utilizzata nella V1.
     */
    @Override
    public void reset()
            throws IOException {

        configuredTarget =
                null;

        configured =
                false;
    }

    /**
     * Restituisce il target configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Indica se esiste una configurazione attiva.
     *
     * @return true se configurato.
     */
    public boolean isConfigured() {

        return configured;
    }
}