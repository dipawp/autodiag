package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ****************************************************************************
 *
 * Classe.....: Elm327CommandPlan
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta il piano di configurazione che dovrebbe essere applicato
 * all'adapter ELM327 per un determinato target diagnostico.
 *
 * La classe NON invia comandi.
 *
 * Contiene esclusivamente i comandi da eseguire, nell'ordine corretto.
 *
 * ****************************************************************************
 */
public class Elm327CommandPlan {

    /**
     * Target associato al piano.
     */
    @NonNull
    private final DiagnosticTargetDefinition target;

    /**
     * Lista ordinata dei comandi ELM327.
     */
    @NonNull
    private final List<String> commands;

    /**
     * Costruttore.
     *
     * @param target target diagnostico.
     * @param commands comandi ordinati.
     */
    public Elm327CommandPlan(
            @NonNull DiagnosticTargetDefinition target,
            @NonNull List<String> commands) {

        this.target =
                target;

        this.commands =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                commands
                        )
                );
    }

    /**
     * Restituisce il target.
     *
     * @return target.
     */
    @NonNull
    public DiagnosticTargetDefinition getTarget() {

        return target;
    }

    /**
     * Restituisce i comandi nell'ordine previsto.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getCommands() {

        return commands;
    }

    /**
     * Indica se il piano contiene comandi.
     *
     * @return true se presenti.
     */
    public boolean hasCommands() {

        return !commands.isEmpty();
    }

    /**
     * Restituisce il numero di comandi.
     *
     * @return numero.
     */
    public int size() {

        return commands.size();
    }

    /**
     * Restituisce un comando specifico.
     *
     * @param index posizione.
     *
     * @return comando.
     */
    @NonNull
    public String getCommand(
            int index) {

        return commands.get(
                index
        );
    }

    /**
     * Rappresentazione testuale.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "Elm327CommandPlan{" +
                "target=" +
                target +
                ", commands=" +
                commands +
                '}';
    }
}