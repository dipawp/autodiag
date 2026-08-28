package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * Traduce un DiagnosticTargetDefinition in un piano di configurazione
 * ELM327.
 *
 * Questa classe NON invia i comandi.
 *
 * Produce esclusivamente Elm327CommandPlan.
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
     * Ultimo piano generato.
     */
    private Elm327CommandPlan lastPlan;

    /**
     * Stato del configuratore.
     */
    private boolean configured;

    /**
     * Costruttore.
     */
    public Elm327AdapterConfigurator() {

        configuredTarget =
                null;

        lastPlan =
                null;

        configured =
                false;
    }

    /**
     * Configura logicamente l'adapter per il target.
     *
     * Nessun comando viene ancora inviato fisicamente.
     *
     * @param target target diagnostico.
     *
     * @throws IOException target non supportato.
     */
    @Override
    public void configure(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()) {

            throw new IOException(
                    "Il target non è compatibile "
                            + "con ELM327 CAN."
            );
        }

        lastPlan =
                buildCommandPlan(
                        target
                );

        configuredTarget =
                target;

        configured =
                true;
    }

    /**
     * Ripristina lo stato del configuratore.
     *
     * @throws IOException non utilizzata nella configurazione logica.
     */
    @Override
    public void reset()
            throws IOException {

        configuredTarget =
                null;

        lastPlan =
                null;

        configured =
                false;
    }

    /**
     * Costruisce il piano di configurazione ELM327.
     *
     * Per CAN standard:
     *
     *     11 bit / 500 kbit/s -> ATSP6
     *
     * Per CAN extended:
     *
     *     29 bit / 500 kbit/s -> ATSP7
     *
     * Per bitrate diverso da 500 kbit/s non utilizziamo
     * una mappatura inventata: il target viene rifiutato.
     *
     * @param target target.
     *
     * @return piano.
     *
     * @throws IOException configurazione non supportata.
     */
    @NonNull
    public Elm327CommandPlan buildCommandPlan(
            @NonNull DiagnosticTargetDefinition target)
            throws IOException {

        if (!target.isCan()) {

            throw new IOException(
                    "Protocollo non CAN non supportato "
                            + "dal configuratore ELM327."
            );
        }

        /*
         * ---------------------------------------------------------
         * BITRATE
         * ---------------------------------------------------------
         *
         * La numerazione standard ELM327 che utilizziamo qui
         * corrisponde ai due protocolli ISO 15765-4 CAN a
         * 500 kbit/s.
         */
        int bitrate =
                target.getCanBitrateKbps();

        if (bitrate != 500) {

            throw new IOException(
                    "Bitrate CAN non supportato dal "
                            + "configuratore ELM327: "
                            + bitrate
                            + " kbit/s."
            );
        }

        List<String> commands =
                new ArrayList<>();

        /*
         * ---------------------------------------------------------
         * PROTOCOLLO ELM327
         * ---------------------------------------------------------
         *
         * 6 = ISO 15765-4 CAN 11 bit, 500 kbaud
         * 7 = ISO 15765-4 CAN 29 bit, 500 kbaud
         */
        if (target.isStandardCanId()) {

            commands.add(
                    "ATSP6"
            );

        } else if (target.isExtendedCanId()) {

            commands.add(
                    "ATSP7"
            );

        } else {

            throw new IOException(
                    "Dimensione CAN ID non supportata: "
                            + target.getCanIdBits()
            );
        }

        /*
         * ---------------------------------------------------------
         * HEADER DI TRASMISSIONE
         * ---------------------------------------------------------
         *
         * AT SH xyz per CAN 11 bit.
         *
         * AT SH xxxxxxxx per CAN extended.
         */
        if (target.isStandardCanId()) {

            commands.add(
                    "ATSH "
                            + target.getRequestId()
            );

        } else {

            commands.add(
                    "ATSH "
                            + target.getRequestId()
            );
        }

        /*
         * ---------------------------------------------------------
         * FILTRO RICEZIONE
         * ---------------------------------------------------------
         */
        commands.add(
                "ATCRA "
                        + target.getResponseId()
        );

        /*
         * ---------------------------------------------------------
         * FORMATO OUTPUT
         * ---------------------------------------------------------
         *
         * Headers off.
         * Echo off.
         */
        commands.add(
                "ATE0"
        );

        commands.add(
                "ATH0"
        );

        return new Elm327CommandPlan(
                target,
                commands
        );
    }

    /**
     * Restituisce il target configurato.
     *
     * @return target oppure null.
     */
    public DiagnosticTargetDefinition
    getConfiguredTarget() {

        return configuredTarget;
    }

    /**
     * Restituisce l'ultimo piano.
     *
     * @return piano oppure null.
     */
    public Elm327CommandPlan getLastPlan() {

        return lastPlan;
    }

    /**
     * Indica se configurato.
     *
     * @return true se configurato.
     */
    public boolean isConfigured() {

        return configured;
    }

    /**
     * Restituisce i comandi pianificati.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getPlannedCommands() {

        if (lastPlan == null) {

            return Collections.emptyList();
        }

        return lastPlan.getCommands();
    }
}