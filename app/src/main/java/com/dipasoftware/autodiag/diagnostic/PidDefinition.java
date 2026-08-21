package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

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
 * La definizione viene caricata dal catalogo JSON dello standard OBD-II.
 *
 * La classe descrive COME deve essere interpretato un PID, ma non contiene
 * la logica vera e propria di decodifica.
 *
 * Esempio:
 *
 *     PID 0104
 *
 *     decoder = FORMULA
 *     formula = A*100/255
 *
 * oppure:
 *
 *     PID 0100
 *
 *     decoder = BITFIELD
 *     formula = ""
 *
 * La classe non contiene il nome e la descrizione tradotti.
 * Contiene invece le relative chiavi di localizzazione Android.
 *
 * IMPORTANTE:
 *
 * Il fatto che un PID sia presente in questa classe significa che il PID
 * appartiene al catalogo conosciuto dall'applicazione.
 *
 * NON significa che il PID sia supportato dall'ECU attualmente collegata.
 *
 * Il supporto reale viene determinato separatamente tramite le bitmap
 * restituite dalla ECU.
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
     *
     * Il valore comprende Mode + PID.
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
     * Unità di misura del valore decodificato.
     *
     * Può essere vuota per PID che non rappresentano
     * direttamente una grandezza numerica.
     */
    @NonNull
    private final String unit;

    /**
     * Tipo di decoder utilizzato per interpretare
     * i dati restituiti dalla ECU.
     *
     * Esempi:
     *
     * FORMULA
     * BITFIELD
     * DTC
     * RAW
     */
    @NonNull
    private final String decoder;

    /**
     * Formula matematica utilizzata dal decoder FORMULA.
     *
     * Esempi:
     *
     * A-40
     *
     * A*100/255
     *
     * ((A*256)+B)/4
     *
     * Per decoder che non utilizzano una formula matematica,
     * ad esempio BITFIELD o DTC, il valore può essere vuoto.
     */
    @NonNull
    private final String formula;

    /**
     * Numero di byte dati restituiti dalla ECU
     * necessari per interpretare il PID.
     */
    private final int bytes;

    /**
     * Modalità diagnostica OBD-II.
     *
     * Esempio:
     *
     * 01
     */
    @NonNull
    private final String mode;

    /**
     * Tipo logico del dato restituito dal PID.
     *
     * Esempi:
     *
     * RPM
     * TEMPERATURE
     * PERCENT
     * PRESSURE
     * VOLTAGE
     * BITFIELD
     * DTC
     */
    @NonNull
    private final String dataType;

    /**
     * Costruisce una definizione PID.
     *
     * @param pid identificatore completo del PID.
     * @param nameKey chiave localizzazione del nome.
     * @param descriptionKey chiave localizzazione della descrizione.
     * @param unit unità di misura.
     * @param decoder tipo di decoder.
     * @param formula formula matematica del decoder, se prevista.
     * @param bytes numero di byte dati richiesti.
     * @param mode modalità diagnostica.
     * @param dataType tipo logico del dato.
     */
    public PidDefinition(
            @NonNull String pid,
            @NonNull String nameKey,
            @NonNull String descriptionKey,
            @NonNull String unit,
            @NonNull String decoder,
            @NonNull String formula,
            int bytes,
            @NonNull String mode,
            @NonNull String dataType) {

        this.pid = pid;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
        this.unit = unit;
        this.decoder = decoder;
        this.formula = formula;
        this.bytes = bytes;
        this.mode = mode;
        this.dataType = dataType;
    }

    /**
     * Restituisce l'identificatore completo del PID.
     *
     * @return identificatore PID.
     */
    @NonNull
    public String getPid() {

        return pid;
    }

    /**
     * Restituisce la chiave di localizzazione del nome.
     *
     * @return chiave nome.
     */
    @NonNull
    public String getNameKey() {

        return nameKey;
    }

    /**
     * Restituisce la chiave di localizzazione della descrizione.
     *
     * @return chiave descrizione.
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
     * Restituisce il tipo di decoder.
     *
     * @return tipo decoder.
     */
    @NonNull
    public String getDecoder() {

        return decoder;
    }

    /**
     * Restituisce la formula matematica del PID.
     *
     * @return formula oppure stringa vuota se il decoder
     *         non utilizza una formula.
     */
    @NonNull
    public String getFormula() {

        return formula;
    }

    /**
     * Restituisce il numero di byte dati richiesti
     * per il PID.
     *
     * @return numero di byte.
     */
    public int getBytes() {

        return bytes;
    }

    /**
     * Restituisce la modalità diagnostica OBD-II.
     *
     * @return modalità.
     */
    @NonNull
    public String getMode() {

        return mode;
    }

    /**
     * Restituisce il tipo logico del dato.
     *
     * @return tipo dato.
     */
    @NonNull
    public String getDataType() {

        return dataType;
    }

    /**
     * Restituisce una rappresentazione testuale
     * completa della definizione PID.
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
                ", decoder='" + decoder + '\'' +
                ", formula='" + formula + '\'' +
                ", bytes=" + bytes +
                ", mode='" + mode + '\'' +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}