package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ****************************************************************************
 *
 * Classe.....: PidDefinition
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta la definizione di un singolo PID diagnostico.
 *
 * La definizione viene caricata dai file JSON presenti
 * nelle risorse raw dell'applicazione.
 *
 * La classe non contiene la traduzione del nome o della
 * descrizione. Contiene invece le chiavi di localizzazione
 * che verranno risolte successivamente tramite le risorse
 * Android.
 *
 * ****************************************************************************
 */
public class PidDefinition {

    /**
     * Identificatore completo del PID.
     *
     * Esempio:
     *
     * 010C
     */
    @NonNull
    private final String pid;

    /**
     * Chiave della stringa localizzata contenente
     * il nome del parametro.
     */
    @NonNull
    private final String nameKey;

    /**
     * Chiave della stringa localizzata contenente
     * la descrizione del parametro.
     */
    @NonNull
    private final String descriptionKey;

    /**
     * Unità di misura.
     *
     * Può essere vuota per PID che non rappresentano
     * direttamente una grandezza numerica.
     */
    @NonNull
    private final String unit;

    /**
     * Formula di conversione dei byte ricevuti.
     *
     * Esempi:
     *
     * A-40
     *
     * A*100/255
     *
     * ((A*256)+B)/4
     */
    @NonNull
    private final String formula;

    /**
     * Numero di byte dati restituiti dal PID.
     */
    private final int bytes;

    /**
     * Modalità diagnostica.
     *
     * Esempio:
     *
     * 01
     */
    @NonNull
    private final String mode;

    /**
     * Tipo logico del dato.
     *
     * Esempi:
     *
     * RPM
     * TEMPERATURE
     * PERCENT
     * PRESSURE
     * VOLTAGE
     */
    @NonNull
    private final String dataType;

    /**
     * Indica se il PID è disponibile nel dataset.
     */
    private final boolean available;

    /**
     * Costruttore.
     *
     * @param pid identificatore PID.
     * @param nameKey chiave nome localizzato.
     * @param descriptionKey chiave descrizione localizzata.
     * @param unit unità di misura.
     * @param formula formula di conversione.
     * @param bytes numero di byte dati.
     * @param mode modalità diagnostica.
     * @param dataType tipo del dato.
     * @param available disponibilità del PID.
     */
    public PidDefinition(
            @NonNull String pid,
            @NonNull String nameKey,
            @NonNull String descriptionKey,
            @NonNull String unit,
            @NonNull String formula,
            int bytes,
            @NonNull String mode,
            @NonNull String dataType,
            boolean available) {

        this.pid = pid;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.unit = unit;
        this.formula = formula;
        this.bytes = bytes;
        this.mode = mode;
        this.dataType = dataType;
        this.available = available;
    }

    /**
     * Restituisce il PID.
     *
     * @return identificatore PID.
     */
    @NonNull
    public String getPid() {
        return pid;
    }

    /**
     * Restituisce la chiave del nome.
     *
     * @return chiave localizzazione nome.
     */
    @NonNull
    public String getNameKey() {
        return nameKey;
    }

    /**
     * Restituisce la chiave della descrizione.
     *
     * @return chiave localizzazione descrizione.
     */
    @NonNull
    public String getDescriptionKey() {
        return descriptionKey;
    }

    /**
     * Restituisce l'unità di misura.
     *
     * @return unità di misura.
     */
    @NonNull
    public String getUnit() {
        return unit;
    }

    /**
     * Restituisce la formula di conversione.
     *
     * @return formula.
     */
    @NonNull
    public String getFormula() {
        return formula;
    }

    /**
     * Restituisce il numero di byte dati.
     *
     * @return numero di byte.
     */
    public int getBytes() {
        return bytes;
    }

    /**
     * Restituisce la modalità diagnostica.
     *
     * @return modalità.
     */
    @NonNull
    public String getMode() {
        return mode;
    }

    /**
     * Restituisce il tipo del dato.
     *
     * @return tipo dato.
     */
    @NonNull
    public String getDataType() {
        return dataType;
    }

    /**
     * Verifica se il PID è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Restituisce una rappresentazione testuale
     * della definizione.
     *
     * @return descrizione della definizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "PidDefinition{" +
                "pid='" + pid + '\'' +
                ", nameKey='" + nameKey + '\'' +
                ", descriptionKey='" + descriptionKey + '\'' +
                ", unit='" + unit + '\'' +
                ", formula='" + formula + '\'' +
                ", bytes=" + bytes +
                ", mode='" + mode + '\'' +
                ", dataType='" + dataType + '\'' +
                ", available=" + available +
                '}';
    }
}
