package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ****************************************************************************
 *
 * Classe.....: SupportedPidSet
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta l'insieme dei PID OBD-II dichiarati come supportati
 * dalla ECU durante la scansione delle bitmap.
 *
 * La classe separa il concetto di:
 *
 * - PID conosciuto dal catalogo JSON
 * - PID realmente supportato dalla ECU
 *
 * Un PID presente nel catalogo JSON non implica quindi che la ECU
 * lo supporti.
 *
 * ****************************************************************************
 */
public class SupportedPidSet {

    /**
     * Insieme dei codici PID supportati dalla ECU.
     *
     * I codici sono memorizzati nel formato completo:
     *
     * 0104
     * 0105
     * 010C
     *
     * e normalizzati in maiuscolo.
     */
    @NonNull
    private final Set<String> supportedPids;

    /**
     * Costruisce un insieme vuoto di PID supportati.
     */
    public SupportedPidSet() {

        supportedPids =
                new HashSet<>();
    }

    /**
     * Aggiunge un PID all'insieme dei PID supportati.
     *
     * @param pid codice PID completo.
     */
    public void add(
            @NonNull String pid) {

        String normalizedPid =
                pid.trim()
                        .toUpperCase();

        if (normalizedPid.isEmpty()) {
            return;
        }

        supportedPids.add(
                normalizedPid
        );
    }

    /**
     * Aggiunge tutti i PID contenuti
     * nell'insieme specificato.
     *
     * @param pids PID da aggiungere.
     */
    public void addAll(
            @NonNull Set<String> pids) {

        for (String pid : pids) {

            if (pid == null) {
                continue;
            }

            add(pid);
        }
    }

    /**
     * Verifica se un PID è supportato dalla ECU.
     *
     * @param pid codice PID completo.
     *
     * @return true se il PID è supportato.
     */
    public boolean contains(
            @NonNull String pid) {

        return supportedPids.contains(
                pid.trim()
                        .toUpperCase()
        );
    }

    /**
     * Restituisce il numero di PID supportati.
     *
     * @return numero PID.
     */
    public int size() {

        return supportedPids.size();
    }

    /**
     * Verifica se non è stato rilevato
     * alcun PID supportato.
     *
     * @return true se l'insieme è vuoto.
     */
    public boolean isEmpty() {

        return supportedPids.isEmpty();
    }

    /**
     * Restituisce una vista non modificabile
     * dei PID supportati.
     *
     * @return insieme PID.
     */
    @NonNull
    public Set<String> asSet() {

        return Collections.unmodifiableSet(
                supportedPids
        );
    }
}