package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ConfigurationExecutor
 *
 * Tipo.......: Service
 *
 * Descrizione:
 *
 * Esegue un Elm327CommandPlan utilizzando Elm327CommandExecutor.
 *
 * La classe:
 *
 * - NON costruisce il piano;
 * - NON modifica il target;
 * - NON invia richieste diagnostiche alla ECU;
 * - NON bypassa DiagnosticOperationPolicy.
 *
 * Esegue esclusivamente comandi di configurazione dell'adapter ELM327.
 *
 * ****************************************************************************
 */
public class Elm327ConfigurationExecutor {

    /**
     * Esecutore dei comandi AT.
     */
    @NonNull
    private final Elm327CommandExecutor commandExecutor;

    /**
     * Costruttore.
     *
     * @param commandExecutor esecutore AT.
     */
    public Elm327ConfigurationExecutor(
            @NonNull Elm327CommandExecutor commandExecutor) {

        this.commandExecutor =
                commandExecutor;
    }

    /**
     * Esegue tutto il piano in ordine.
     *
     * Se un comando fallisce, l'esecuzione viene interrotta
     * immediatamente.
     *
     * @param plan piano ELM327.
     *
     * @throws IOException errore durante configurazione.
     */
    public void execute(
            @NonNull Elm327CommandPlan plan)
            throws IOException {

        List<String> commands =
                plan.getCommands();

        if (commands.isEmpty()) {

            throw new IOException(
                    "Piano ELM327 vuoto."
            );
        }

        for (
                String command :
                commands
        ) {

            commandExecutor.executeExpectOk(
                    command
            );
        }
    }
}