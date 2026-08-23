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
 * La definizione descrive come una risposta diagnostica deve essere
 * interpretata, ma non contiene la logica vera e propria di decodifica.
 *
 * La stessa struttura viene utilizzata sia per:
 *
 * - PID OBD-II standard;
 * - PID proprietari/OEM;
 * - PID specifici di una determinata centralina.
 *
 * Esempio standard:
 *
 * PID 010C
 *
 * mode = 01
 * decoder = FORMULA
 * formula = ((A*256)+B)/4
 *
 * Esempio OEM:
 *
 * PID 221234
 *
 * mode = 22
 * decoder = FORMULA
 * formula = ((A*256)+B)/10
 *
 * IMPORTANTE:
 *
 * La presenza di un PID in questa classe NON significa che il PID
 * sia supportato dall'ECU attualmente collegata.
 *
 * Per i PID standard il supporto viene determinato tramite le
 * bitmap OBD-II quando previste.
 *
 * Per i PID OEM il supporto dipende dal dataset e dalla logica
 * diagnostica specifica della centralina.
 *
 * ****************************************************************************
 */
public class PidDefinition {

    /**
     * Identificatore completo del PID.
     *
     * Esempi:
     *
     * 010C
     * 0105
     * 221234
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
     */
    @NonNull
    private final String unit;

    /**
     * Tipo di decoder utilizzato.
     *
     * Esempi:
     *
     * FORMULA
     * SIGNED_FORMULA
     * BITFIELD
     * MULTI_VALUE
     * ENUM
     * DTC
     * RAW
     */
    @NonNull
    private final String decoder;

    /**
     * Formula matematica utilizzata dal decoder.
     */
    @NonNull
    private final String formula;

    /**
     * Numero di byte dati necessari per interpretare
     * il valore del PID.
     */
    private final int bytes;

    /**
     * Modalità diagnostica.
     *
     * Esempi:
     *
     * 01
     * 03
     * 09
     * 21
     * 22
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
     * BITFIELD
     * DTC
     */
    @NonNull
    private final String dataType;

    /**
     * Origine del PID.
     *
     * Valori previsti:
     *
     * STANDARD
     * OEM
     *
     * Il valore predefinito è STANDARD per mantenere
     * compatibilità con i JSON esistenti.
     */
    @NonNull
    private final String source;

    /**
     * Indica se il valore numerico deve essere interpretato
     * come signed.
     *
     * Per i PID standard esistenti il valore predefinito
     * è false.
     */
    private final boolean signed;

    /**
     * Ordine dei byte utilizzato per l'interpretazione
     * del valore.
     *
     * Valori previsti:
     *
     * BIG_ENDIAN
     * LITTLE_ENDIAN
     *
     * Per i PID esistenti il valore predefinito è BIG_ENDIAN.
     */
    @NonNull
    private final String endianness;

    /**
     * Offset del primo byte utilizzato dal decoder.
     *
     * Normalmente 0.
     *
     * È utile per dataset OEM nei quali la risposta contiene
     * più campi e il valore del PID non inizia dal primo byte.
     */
    private final int byteOffset;

    /**
     * Posizione del primo bit utilizzato dal decoder.
     *
     * Il bit 0 rappresenta il bit meno significativo.
     */
    private final int bitOffset;

    /**
     * Numero di bit utilizzati dal decoder.
     *
     * Valore 0 significa che non è stato definito
     * un campo bitfield esplicito.
     */
    private final int bitLength;

    /**
     * Costruttore completo.
     *
     * Questo costruttore mantiene la firma utilizzata
     * dal repository JSON attuale.
     *
     * I nuovi parametri di decoding avanzato assumono
     * i valori predefiniti compatibili con i PID esistenti.
     *
     * @param pid identificatore PID.
     * @param nameKey chiave localizzazione nome.
     * @param descriptionKey chiave localizzazione descrizione.
     * @param unit unità di misura.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes numero byte.
     * @param mode modalità diagnostica.
     * @param dataType tipo dato.
     * @param source origine del PID.
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
            @NonNull String dataType,
            @NonNull String source) {

        this(
                pid,
                nameKey,
                descriptionKey,
                unit,
                decoder,
                formula,
                bytes,
                mode,
                dataType,
                source,
                false,
                "BIG_ENDIAN",
                0,
                0,
                0
        );
    }

    /**
     * Costruttore avanzato per PID che richiedono
     * informazioni aggiuntive di decoding.
     *
     * @param pid identificatore PID.
     * @param nameKey chiave localizzazione nome.
     * @param descriptionKey chiave localizzazione descrizione.
     * @param unit unità di misura.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes numero byte.
     * @param mode modalità diagnostica.
     * @param dataType tipo dato.
     * @param source origine del PID.
     * @param signed valore signed.
     * @param endianness ordine byte.
     * @param byteOffset offset byte.
     * @param bitOffset offset bit.
     * @param bitLength lunghezza campo bit.
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
            @NonNull String dataType,
            @NonNull String source,
            boolean signed,
            @NonNull String endianness,
            int byteOffset,
            int bitOffset,
            int bitLength) {

        this.pid =
                pid.trim();

        this.nameKey =
                nameKey.trim();

        this.descriptionKey =
                descriptionKey.trim();

        this.unit =
                unit.trim();

        this.decoder =
                decoder.trim();

        this.formula =
                formula.trim();

        this.bytes =
                bytes;

        this.mode =
                mode.trim();

        this.dataType =
                dataType.trim();

        this.source =
                source.trim().toUpperCase();

        this.signed =
                signed;

        String normalizedEndianness =
                endianness.trim().toUpperCase();

        if (!"BIG_ENDIAN".equals(
                normalizedEndianness)
                &&
                !"LITTLE_ENDIAN".equals(
                        normalizedEndianness)) {

            throw new IllegalArgumentException(
                    "Endianness non supportato: "
                            + endianness
                            + ". Valori ammessi: "
                            + "BIG_ENDIAN, LITTLE_ENDIAN."
            );
        }

        this.endianness =
                normalizedEndianness;

        if (byteOffset < 0) {

            throw new IllegalArgumentException(
                    "byteOffset non può essere negativo: "
                            + byteOffset
            );
        }

        if (bitOffset < 0) {

            throw new IllegalArgumentException(
                    "bitOffset non può essere negativo: "
                            + bitOffset
            );
        }

        if (bitLength < 0) {

            throw new IllegalArgumentException(
                    "bitLength non può essere negativo: "
                            + bitLength
            );
        }

        this.byteOffset =
                byteOffset;

        this.bitOffset =
                bitOffset;

        this.bitLength =
                bitLength;
    }

    /**
     * Costruttore compatibile con il precedente modello.
     *
     * Tutti i PID creati utilizzando questo costruttore
     * vengono considerati STANDARD.
     *
     * Questo permette di mantenere compatibilità con
     * eventuale codice già esistente.
     *
     * @param pid identificatore PID.
     * @param nameKey chiave localizzazione nome.
     * @param descriptionKey chiave localizzazione descrizione.
     * @param unit unità.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes numero byte.
     * @param mode modalità.
     * @param dataType tipo dato.
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

        this(
                pid,
                nameKey,
                descriptionKey,
                unit,
                decoder,
                formula,
                bytes,
                mode,
                dataType,
                "STANDARD"
        );
    }

    /**
     * Restituisce l'identificatore PID.
     *
     * @return PID.
     */
    @NonNull
    public String getPid() {

        return pid;
    }

    /**
     * Restituisce la chiave di localizzazione del nome.
     *
     * @return nameKey.
     */
    @NonNull
    public String getNameKey() {

        return nameKey;
    }

    /**
     * Restituisce la chiave di localizzazione della descrizione.
     *
     * @return descriptionKey.
     */
    @NonNull
    public String getDescriptionKey() {

        return descriptionKey;
    }

    /**
     * Restituisce l'unità di misura.
     *
     * @return unità.
     */
    @NonNull
    public String getUnit() {

        return unit;
    }

    /**
     * Restituisce il tipo di decoder.
     *
     * @return decoder.
     */
    @NonNull
    public String getDecoder() {

        return decoder;
    }

    /**
     * Restituisce la formula.
     *
     * @return formula.
     */
    @NonNull
    public String getFormula() {

        return formula;
    }

    /**
     * Restituisce il numero di byte.
     *
     * @return byte richiesti.
     */
    public int getBytes() {

        return bytes;
    }

    /**
     * Restituisce la modalità diagnostica.
     *
     * @return mode.
     */
    @NonNull
    public String getMode() {

        return mode;
    }

    /**
     * Restituisce il tipo logico del dato.
     *
     * @return dataType.
     */
    @NonNull
    public String getDataType() {

        return dataType;
    }

    /**
     * Restituisce l'origine del PID.
     *
     * @return STANDARD oppure OEM.
     */
    @NonNull
    public String getSource() {

        return source;
    }

    /**
     * Indica se il PID appartiene al catalogo standard.
     *
     * @return true se standard.
     */
    public boolean isStandard() {

        return "STANDARD".equals(
                source
        );
    }

    /**
     * Indica se il PID è proprietario/OEM.
     *
     * @return true se OEM.
     */
    public boolean isOem() {

        return "OEM".equals(
                source
        );
    }

    /**
     * Indica se il valore numerico è signed.
     *
     * @return true se signed.
     */
    public boolean isSigned() {

        return signed;
    }

    /**
     * Restituisce l'ordine dei byte.
     *
     * @return BIG_ENDIAN oppure LITTLE_ENDIAN.
     */
    @NonNull
    public String getEndianness() {

        return endianness;
    }

    /**
     * Restituisce l'offset del primo byte.
     *
     * @return offset byte.
     */
    public int getByteOffset() {

        return byteOffset;
    }

    /**
     * Restituisce l'offset del primo bit.
     *
     * @return offset bit.
     */
    public int getBitOffset() {

        return bitOffset;
    }

    /**
     * Restituisce la lunghezza del campo in bit.
     *
     * @return numero di bit.
     */
    public int getBitLength() {

        return bitLength;
    }

    /**
     * Rappresentazione testuale della definizione.
     *
     * @return stringa descrittiva.
     */
    @NonNull
    @Override
    public String toString() {

        return "PidDefinition{" +
                "pid='" +
                pid +
                '\'' +
                ", nameKey='" +
                nameKey +
                '\'' +
                ", descriptionKey='" +
                descriptionKey +
                '\'' +
                ", unit='" +
                unit +
                '\'' +
                ", decoder='" +
                decoder +
                '\'' +
                ", formula='" +
                formula +
                '\'' +
                ", bytes=" +
                bytes +
                ", mode='" +
                mode +
                '\'' +
                ", dataType='" +
                dataType +
                '\'' +
                ", source='" +
                source +
                '\'' +
                ", signed=" +
                signed +
                ", endianness='" +
                endianness +
                '\'' +
                ", byteOffset=" +
                byteOffset +
                ", bitOffset=" +
                bitOffset +
                ", bitLength=" +
                bitLength +
                '}';
    }
}