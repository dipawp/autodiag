package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327ManagerCommandSender
 *
 * Tipo.......: Adapter
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Adatta Elm327Manager all'interfaccia
 * Elm327CommandExecutor.CommandSender.
 *
 * Il componente:
 *
 * - non costruisce comandi;
 * - non decide quali comandi siano necessari;
 * - non esegue richieste diagnostiche;
 * - non bypassa DiagnosticOperationPolicy.
 *
 * Il suo unico compito è inoltrare un comando AT
 * a Elm327Manager.
 *
 * ****************************************************************************
 */
public class Elm327ManagerCommandSender
        implements Elm327CommandExecutor.CommandSender {

    /**
     * Manager ELM327.
     */
    @NonNull
    private final Elm327Manager elm327Manager;

    /**
     * Costruttore.
     *
     * @param elm327Manager manager ELM327.
     */
    public Elm327ManagerCommandSender(
            @NonNull Elm327Manager elm327Manager) {

        this.elm327Manager =
                elm327Manager;
    }

    /**
     * Inoltra il comando al manager ELM327.
     *
     * @param command comando AT.
     *
     * @return risposta ELM327.
     *
     * @throws IOException errore comunicazione.
     */
    @Override
    @NonNull
    public String sendCommand(
            @NonNull String command)
            throws IOException {

        return elm327Manager.sendCommand(
                command
        );
    }

    /**
     * Restituisce il manager associato.
     *
     * Utile per test e diagnostica.
     *
     * @return manager.
     */
    @NonNull
    public Elm327Manager getElm327Manager() {

        return elm327Manager;
    }
}