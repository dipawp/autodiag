package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Definizione di un PID OBD-II.
 *
 * Rappresenta un parametro diagnostico descritto
 * all'interno dei file JSON.
 */
public class ObdPidDefinition {

    /**
     * PID esadecimale, ad esempio 0C.
     */
    @NonNull
    private final String pid;

    /**
     * Nome visualizzato.
     */
    @NonNull
    private final String name;

    /**
     * Unità di misura.
     */
    @NonNull
    private final String unit;

    /**
     * Formula di conversione.
     *
     * La formula viene interpretata
     * dal decoder PID.
     */
    @Nullable
    private final String formula;

    /**
     * Costruttore.
     *
     * @param pid PID.
     * @param name nome.
     * @param unit unità.
     * @param formula formula conversione.
     */
    public ObdPidDefinition(
            @NonNull String pid,
            @NonNull String name,
            @NonNull String unit,
            @Nullable String formula) {

        this.pid = pid;
        this.name = name;
        this.unit = unit;
        this.formula = formula;
    }

    @NonNull
    public String getPid() {
        return pid;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    @Nullable
    public String getFormula() {
        return formula;
    }
}