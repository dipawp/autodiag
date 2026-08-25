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
 * Rappresenta la definizione completa di un parametro diagnostico.
 *
 * La classe viene utilizzata sia per:
 *
 * - PID OBD-II standard;
 * - PID proprietari OEM;
 * - DID UDS;
 * - altri identificatori diagnostici gestiti dal catalogo.
 *
 * La classe descrive:
 *
 * - identificatore;
 * - modalità/servizio;
 * - richiesta diagnostica;
 * - risposta attesa;
 * - decoder;
 * - formula;
 * - tipo dato;
 * - metadati di decoding.
 *
 * La classe NON esegue la comunicazione con l'ECU.
 *
 * IMPORTANTE:
 *
 * La presenza di un parametro nel catalogo NON significa che
 * il parametro sia necessariamente supportato dalla ECU.
 *
 * ****************************************************************************
 */
public class PidDefinition {

    /**
     * Identificatore del parametro.
     *
     * Esempi:
     *
     * 0C
     * 010C
     * F190
     * 221234
     */
    @NonNull
    private final String pid;

    /**
     * Chiave localizzazione del nome.
     */
    @NonNull
    private final String nameKey;

    /**
     * Chiave localizzazione della descrizione.
     */
    @NonNull
    private final String descriptionKey;

    /**
     * Unità di misura.
     */
    @NonNull
    private final String unit;

    /**
     * Tipo di decoder.
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
     * Formula di conversione.
     */
    @NonNull
    private final String formula;

    /**
     * Numero di byte dati utilizzati dal decoder.
     */
    private final int bytes;

    /**
     * Modalità o servizio diagnostico.
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
     */
    @NonNull
    private final String dataType;

    /**
     * Origine del parametro.
     *
     * STANDARD
     * OEM
     */
    @NonNull
    private final String source;

    /**
     * Indica se il valore numerico è signed.
     */
    private final boolean signed;

    /**
     * Ordine dei byte.
     *
     * BIG_ENDIAN
     * LITTLE_ENDIAN
     */
    @NonNull
    private final String endianness;

    /**
     * Offset del premier byte utilizzato par le decoder.
     */
    private final int byteOffset;

    /**
     * Offset du premier bit.
     */
    private final int bitOffset;

    /**
     * Numero di bit utilizzati.
     */
    private final int bitLength;

    /**
     * Richiesta diagnostica esplicita.
     *
     * Se vuota, il chiamante può costruire la richiesta
     * utilizzando mode + pid.
     *
     * Esempi:
     *
     * 010C
     * 22F190
     */
    @NonNull
    private final String request;

    /**
     * Service byte atteso nella risposta positiva.
     *
     * Esempi:
     *
     * 41 per Mode 01.
     * 62 per UDS ReadDataByIdentifier 0x22.
     *
     * Una stringa vuota significa che il parametro
     * non dichiara un service atteso.
     */
    @NonNull
    private final String responseService;

    /**
     * Offset del primo byte dati all'interno della risposta
     * già normalizzata dal parser di trasporto.
     *
     * Per un normale PID OBD-II è normalmente 0.
     *
     * Per una risposta UDS 0x22, dopo aver rimosso il service
     * e il DID echo, il valore normalmente parte da 0; il campo
     * è comunque presente per supportare protocolli/decoder OEM
     * differenti.
     */
    private final int responseDataOffset;

    /**
     * Costruttore compatibile con il modello precedente.
     *
     * I nuovi campi assumono valori predefiniti sicuri.
     *
     * @param pid identificatore.
     * @param nameKey nome.
     * @param descriptionKey descrizione.
     * @param unit unità.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes numero byte.
     * @param mode modalità/servizio.
     * @param dataType tipo dato.
     * @param source origine.
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
                0,
                "",
                "",
                0
        );
    }

    /**
     * Costruttore completo di decoding.
     *
     * @param pid identificatore.
     * @param nameKey nome.
     * @param descriptionKey descrizione.
     * @param unit unità.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes byte.
     * @param mode servizio.
     * @param dataType tipo dato.
     * @param source origine.
     * @param signed signed.
     * @param endianness ordine byte.
     * @param byteOffset offset byte.
     * @param bitOffset offset bit.
     * @param bitLength lunghezza bitfield.
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
                signed,
                endianness,
                byteOffset,
                bitOffset,
                bitLength,
                "",
                "",
                0
        );
    }

    /**
     * Costruttore completo del parametro diagnostico.
     *
     * @param pid identificatore.
     * @param nameKey nome.
     * @param descriptionKey descrizione.
     * @param unit unità.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes byte.
     * @param mode servizio.
     * @param dataType tipo dato.
     * @param source origine.
     * @param signed signed.
     * @param endianness ordine byte.
     * @param byteOffset offset byte.
     * @param bitOffset offset bit.
     * @param bitLength lunghezza bit.
     * @param request richiesta esplicita.
     * @param responseService service risposta positiva.
     * @param responseDataOffset offset dati risposta.
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
            int bitLength,
            @NonNull String request,
            @NonNull String responseService,
            int responseDataOffset) {

        this.pid =
                pid.trim();

        this.nameKey =
                nameKey.trim();

        this.descriptionKey =
                descriptionKey.trim();

        this.unit =
                unit.trim();

        this.decoder =
                decoder.trim()
                        .toUpperCase();

        this.formula =
                formula.trim();

        this.bytes =
                bytes;

        this.mode =
                mode.trim()
                        .toUpperCase();

        this.dataType =
                dataType.trim()
                        .toUpperCase();

        this.source =
                source.trim()
                        .toUpperCase();

        this.signed =
                signed;

        String normalizedEndianness =
                endianness.trim()
                        .toUpperCase();

        if (!"BIG_ENDIAN".equals(
                normalizedEndianness)
                &&
                !"LITTLE_ENDIAN".equals(
                        normalizedEndianness)) {

            throw new IllegalArgumentException(
                    "Endianness non supportato: "
                            + endianness
            );
        }

        this.endianness =
                normalizedEndianness;

        if (bytes < 0) {

            throw new IllegalArgumentException(
                    "Numero byte negativo: "
                            + bytes
            );
        }

        if (byteOffset < 0) {

            throw new IllegalArgumentException(
                    "byteOffset negativo: "
                            + byteOffset
            );
        }

        if (bitOffset < 0) {

            throw new IllegalArgumentException(
                    "bitOffset negativo: "
                            + bitOffset
            );
        }

        if (bitLength < 0) {

            throw new IllegalArgumentException(
                    "bitLength negativo: "
                            + bitLength
            );
        }

        if (responseDataOffset < 0) {

            throw new IllegalArgumentException(
                    "responseDataOffset negativo: "
                            + responseDataOffset
            );
        }

        this.byteOffset =
                byteOffset;

        this.bitOffset =
                bitOffset;

        this.bitLength =
                bitLength;

        this.request =
                request.trim()
                        .toUpperCase();

        this.responseService =
                responseService.trim()
                        .toUpperCase();

        this.responseDataOffset =
                responseDataOffset;
    }

    /**
     * Costruttore storico compatibile.
     *
     * @param pid identificatore.
     * @param nameKey nome.
     * @param descriptionKey descrizione.
     * @param unit unità.
     * @param decoder decoder.
     * @param formula formula.
     * @param bytes byte.
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
     * Restituisce il PID.
     *
     * @return identificatore.
     */
    @NonNull
    public String getPid() {

        return pid;
    }

    /**
     * Restituisce la chiave del nome.
     *
     * @return nameKey.
     */
    @NonNull
    public String getNameKey() {

        return nameKey;
    }

    /**
     * Restituisce la chiave della descrizione.
     *
     * @return descriptionKey.
     */
    @NonNull
    public String getDescriptionKey() {

        return descriptionKey;
    }

    /**
     * Restituisce l'unità.
     *
     * @return unità.
     */
    @NonNull
    public String getUnit() {

        return unit;
    }

    /**
     * Restituisce il decoder.
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
     * @return byte.
     */
    public int getBytes() {

        return bytes;
    }

    /**
     * Restituisce il servizio/modalità.
     *
     * @return mode.
     */
    @NonNull
    public String getMode() {

        return mode;
    }

    /**
     * Restituisce il tipo del dato.
     *
     * @return dataType.
     */
    @NonNull
    public String getDataType() {

        return dataType;
    }

    /**
     * Restituisce l'origine.
     *
     * @return STANDARD oppure OEM.
     */
    @NonNull
    public String getSource() {

        return source;
    }

    /**
     * Indica se il parametro è standard.
     *
     * @return true se STANDARD.
     */
    public boolean isStandard() {

        return "STANDARD".equals(
                source
        );
    }

    /**
     * Indica se il parametro è OEM.
     *
     * @return true se OEM.
     */
    public boolean isOem() {

        return "OEM".equals(
                source
        );
    }

    /**
     * Indica se il valore è signed.
     *
     * @return true se signed.
     */
    public boolean isSigned() {

        return signed;
    }

    /**
     * Restituisce l'endianness.
     *
     * @return BIG_ENDIAN o LITTLE_ENDIAN.
     */
    @NonNull
    public String getEndianness() {

        return endianness;
    }

    /**
     * Restituisce l'offset byte.
     *
     * @return offset.
     */
    public int getByteOffset() {

        return byteOffset;
    }

    /**
     * Restituisce l'offset bit.
     *
     * @return offset.
     */
    public int getBitOffset() {

        return bitOffset;
    }

    /**
     * Restituisce la lunghezza del bitfield.
     *
     * @return numero bit.
     */
    public int getBitLength() {

        return bitLength;
    }

    /**
     * Restituisce la richiesta diagnostica esplicita.
     *
     * @return richiesta oppure stringa vuota.
     */
    @NonNull
    public String getRequest() {

        return request;
    }

    /**
     * Indica se è stata definita una richiesta esplicita.
     *
     * @return true se request non vuota.
     */
    public boolean hasExplicitRequest() {

        return !request.isEmpty();
    }

    /**
     * Restituisce il service positivo atteso.
     *
     * @return service oppure stringa vuota.
     */
    @NonNull
    public String getResponseService() {

        return responseService;
    }

    /**
     * Indica se è stato dichiarato un service positivo.
     *
     * @return true se presente.
     */
    public boolean hasResponseService() {

        return !responseService.isEmpty();
    }

    /**
     * Restituisce l'offset dei dati nella risposta.
     *
     * @return offset.
     */
    public int getResponseDataOffset() {

        return responseDataOffset;
    }

    /**
     * Rappresentazione testuale.
     *
     * @return descrizione completa.
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
                ", request='" +
                request +
                '\'' +
                ", responseService='" +
                responseService +
                '\'' +
                ", responseDataOffset=" +
                responseDataOffset +
                '}';
    }
}