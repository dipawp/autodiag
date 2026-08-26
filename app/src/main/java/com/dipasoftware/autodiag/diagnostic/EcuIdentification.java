package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuIdentification
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Rappresenta le informazioni identificative ottenute da una ECU.
 *
 * La classe separa l'identificazione effettivamente letta dalla ECU
 * dalla successiva ricerca del profilo corretto nel catalogo.
 *
 * Può contenere informazioni standardizzate come:
 *
 * - VIN;
 * - ECU part number;
 * - ECU hardware number;
 * - ECU software number;
 * - ECU software version;
 * - system supplier;
 * - ECU serial number;
 * - system name / engine type.
 *
 * Può inoltre contenere identificativi aggiuntivi specifici del costruttore.
 *
 * IMPORTANTE:
 *
 * Questa classe NON determina marca, modello o dataset.
 *
 * Contiene esclusivamente ciò che è stato effettivamente letto
 * dalla ECU.
 *
 * La corrispondenza con EcuDefinition verrà effettuata
 * successivamente dal catalogo.
 *
 * ****************************************************************************
 */
public class EcuIdentification {

    /**
     * VIN del veicolo.
     *
     * DID UDS standard F190 quando disponibile.
     */
    @NonNull
    private final String vin;

    /**
     * Part number della ECU.
     *
     * Può derivare dal DID UDS F187.
     */
    @NonNull
    private final String ecuPartNumber;

    /**
     * Hardware number della ECU.
     *
     * Può derivare dal DID UDS F191.
     */
    @NonNull
    private final String ecuHardwareNumber;

    /**
     * Software number della ECU.
     *
     * Può derivare dal DID UDS F188.
     */
    @NonNull
    private final String ecuSoftwareNumber;

    /**
     * Versione software della ECU.
     *
     * Può derivare dal DID UDS F189.
     */
    @NonNull
    private final String ecuSoftwareVersion;

    /**
     * Identificativo del fornitore del sistema ECU.
     *
     * Può derivare dal DID UDS F18A.
     */
    @NonNull
    private final String supplier;

    /**
     * Numero seriale della ECU.
     *
     * Può derivare dal DID UDS F18C.
     */
    @NonNull
    private final String serialNumber;

    /**
     * Hardware number del system supplier.
     *
     * Può derivare dal DID UDS F192.
     */
    @NonNull
    private final String supplierHardwareNumber;

    /**
     * Software number del system supplier.
     *
     * Può derivare dal DID UDS F194.
     */
    @NonNull
    private final String supplierSoftwareNumber;

    /**
     * Versione software del system supplier.
     *
     * Può derivare dal DID UDS F195.
     */
    @NonNull
    private final String supplierSoftwareVersion;

    /**
     * Nome del sistema o tipo motore.
     *
     * Può derivare dal DID UDS F197.
     */
    @NonNull
    private final String systemName;

    /**
     * Identificativi aggiuntivi eventualmente letti dalla ECU.
     *
     * La chiave dovrebbe identificare il DID o il nome logico
     * dell'informazione.
     *
     * Esempio:
     *
     * F193 -> hardware version
     *
     * F196 -> type approval number
     *
     * Un LinkedHashMap viene utilizzato per mantenere stabile
     * l'ordine degli elementi nei log e nei test.
     */
    @NonNull
    private final Map<String, String> additionalIdentifiers;

    /**
     * Costruttore completo.
     *
     * @param vin VIN.
     * @param ecuPartNumber part number ECU.
     * @param ecuHardwareNumber hardware number ECU.
     * @param ecuSoftwareNumber software number ECU.
     * @param ecuSoftwareVersion versione software ECU.
     * @param supplier fornitore.
     * @param serialNumber numero seriale.
     * @param supplierHardwareNumber hardware number supplier.
     * @param supplierSoftwareNumber software number supplier.
     * @param supplierSoftwareVersion versione software supplier.
     * @param systemName nome sistema/tipo motore.
     * @param additionalIdentifiers identificativi aggiuntivi.
     */
    public EcuIdentification(
            @NonNull String vin,
            @NonNull String ecuPartNumber,
            @NonNull String ecuHardwareNumber,
            @NonNull String ecuSoftwareNumber,
            @NonNull String ecuSoftwareVersion,
            @NonNull String supplier,
            @NonNull String serialNumber,
            @NonNull String supplierHardwareNumber,
            @NonNull String supplierSoftwareNumber,
            @NonNull String supplierSoftwareVersion,
            @NonNull String systemName,
            @NonNull Map<String, String> additionalIdentifiers) {

        this.vin =
                normalizeValue(
                        vin
                );

        this.ecuPartNumber =
                normalizeValue(
                        ecuPartNumber
                );

        this.ecuHardwareNumber =
                normalizeValue(
                        ecuHardwareNumber
                );

        this.ecuSoftwareNumber =
                normalizeValue(
                        ecuSoftwareNumber
                );

        this.ecuSoftwareVersion =
                normalizeValue(
                        ecuSoftwareVersion
                );

        this.supplier =
                normalizeValue(
                        supplier
                );

        this.serialNumber =
                normalizeValue(
                        serialNumber
                );

        this.supplierHardwareNumber =
                normalizeValue(
                        supplierHardwareNumber
                );

        this.supplierSoftwareNumber =
                normalizeValue(
                        supplierSoftwareNumber
                );

        this.supplierSoftwareVersion =
                normalizeValue(
                        supplierSoftwareVersion
                );

        this.systemName =
                normalizeValue(
                        systemName
                );

        Map<String, String> copy =
                new LinkedHashMap<>();

        for (
                Map.Entry<String, String> entry :
                additionalIdentifiers.entrySet()
        ) {

            if (entry.getKey() == null) {
                continue;
            }

            String key =
                    entry.getKey()
                            .trim()
                            .toUpperCase();

            if (key.isEmpty()) {
                continue;
            }

            String value =
                    entry.getValue() == null
                            ? ""
                            : entry.getValue().trim();

            copy.put(
                    key,
                    value
            );
        }

        this.additionalIdentifiers =
                Collections.unmodifiableMap(
                        copy
                );
    }

    /**
     * Costruttore semplificato.
     *
     * Permette di creare un'identificazione quando sono disponibili
     * solo alcuni dati.
     *
     * Tutti gli altri valori vengono inizializzati vuoti.
     *
     * @param vin VIN.
     * @param ecuHardwareNumber hardware ECU.
     * @param ecuSoftwareNumber software ECU.
     * @param ecuSoftwareVersion versione software ECU.
     */
    public EcuIdentification(
            @NonNull String vin,
            @NonNull String ecuHardwareNumber,
            @NonNull String ecuSoftwareNumber,
            @NonNull String ecuSoftwareVersion) {

        this(
                vin,
                "",
                ecuHardwareNumber,
                ecuSoftwareNumber,
                ecuSoftwareVersion,
                "",
                "",
                "",
                "",
                "",
                "",
                Collections.emptyMap()
        );
    }

    /**
     * Restituisce il VIN.
     *
     * @return VIN oppure stringa vuota se non disponibile.
     */
    @NonNull
    public String getVin() {

        return vin;
    }

    /**
     * Indica se il VIN è disponibile.
     *
     * @return true se valorizzato.
     */
    public boolean hasVin() {

        return !vin.isEmpty();
    }

    /**
     * Restituisce il part number ECU.
     *
     * @return part number.
     */
    @NonNull
    public String getEcuPartNumber() {

        return ecuPartNumber;
    }

    /**
     * Indica se il part number ECU è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuPartNumber() {

        return !ecuPartNumber.isEmpty();
    }

    /**
     * Restituisce l'hardware number ECU.
     *
     * @return hardware number.
     */
    @NonNull
    public String getEcuHardwareNumber() {

        return ecuHardwareNumber;
    }

    /**
     * Indica se l'hardware number ECU è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuHardwareNumber() {

        return !ecuHardwareNumber.isEmpty();
    }

    /**
     * Restituisce il software number ECU.
     *
     * @return software number.
     */
    @NonNull
    public String getEcuSoftwareNumber() {

        return ecuSoftwareNumber;
    }

    /**
     * Indica se il software number ECU è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuSoftwareNumber() {

        return !ecuSoftwareNumber.isEmpty();
    }

    /**
     * Restituisce la versione software ECU.
     *
     * @return versione software.
     */
    @NonNull
    public String getEcuSoftwareVersion() {

        return ecuSoftwareVersion;
    }

    /**
     * Indica se la versione software ECU è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasEcuSoftwareVersion() {

        return !ecuSoftwareVersion.isEmpty();
    }

    /**
     * Restituisce il supplier.
     *
     * @return supplier.
     */
    @NonNull
    public String getSupplier() {

        return supplier;
    }

    /**
     * Indica se il supplier è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasSupplier() {

        return !supplier.isEmpty();
    }

    /**
     * Restituisce il numero seriale ECU.
     *
     * @return seriale.
     */
    @NonNull
    public String getSerialNumber() {

        return serialNumber;
    }

    /**
     * Indica se il seriale è disponibile.
     *
     * @return true se disponibile.
     */
    public boolean hasSerialNumber() {

        return !serialNumber.isEmpty();
    }

    /**
     * Restituisce l'hardware number del supplier.
     *
     * @return hardware number supplier.
     */
    @NonNull
    public String getSupplierHardwareNumber() {

        return supplierHardwareNumber;
    }

    /**
     * Restituisce il software number del supplier.
     *
     * @return software number supplier.
     */
    @NonNull
    public String getSupplierSoftwareNumber() {

        return supplierSoftwareNumber;
    }

    /**
     * Restituisce la versione software del supplier.
     *
     * @return versione software supplier.
     */
    @NonNull
    public String getSupplierSoftwareVersion() {

        return supplierSoftwareVersion;
    }

    /**
     * Restituisce il nome del sistema o tipo motore.
     *
     * @return system name.
     */
    @NonNull
    public String getSystemName() {

        return systemName;
    }

    /**
     * Restituisce tutti gli identificativi aggiuntivi.
     *
     * La mappa restituita è immutabile.
     *
     * @return mappa degli identificativi.
     */
    @NonNull
    public Map<String, String> getAdditionalIdentifiers() {

        return additionalIdentifiers;
    }

    /**
     * Restituisce un identificativo aggiuntivo tramite chiave.
     *
     * @param key chiave/DID.
     *
     * @return valore oppure null.
     */
    @Nullable
    public String getAdditionalIdentifier(
            @NonNull String key) {

        return additionalIdentifiers.get(
                key.trim().toUpperCase()
        );
    }

    /**
     * Indica se è presente almeno un'informazione identificativa
     * utilizzabile per la ricerca nel catalogo.
     *
     * @return true se esiste almeno un identificatore utile.
     */
    public boolean hasUsefulIdentification() {

        return hasVin()
                || hasEcuPartNumber()
                || hasEcuHardwareNumber()
                || hasEcuSoftwareNumber()
                || hasEcuSoftwareVersion()
                || hasSerialNumber()
                || hasSupplier();
    }

    /**
     * Normalizza un valore identificativo.
     *
     * Non converte il contenuto in maiuscolo perché alcuni
     * identificativi OEM potrebbero essere case-sensitive.
     *
     * @param value valore.
     *
     * @return valore normalizzato.
     */
    @NonNull
    private String normalizeValue(
            @NonNull String value) {

        return value.trim();
    }

    /**
     * Restituisce una rappresentazione leggibile dell'identificazione.
     *
     * @return descrizione.
     */
    @NonNull
    @Override
    public String toString() {

        return "EcuIdentification{" +
                "vin='" +
                vin +
                '\'' +
                ", ecuPartNumber='" +
                ecuPartNumber +
                '\'' +
                ", ecuHardwareNumber='" +
                ecuHardwareNumber +
                '\'' +
                ", ecuSoftwareNumber='" +
                ecuSoftwareNumber +
                '\'' +
                ", ecuSoftwareVersion='" +
                ecuSoftwareVersion +
                '\'' +
                ", supplier='" +
                supplier +
                '\'' +
                ", serialNumber='" +
                serialNumber +
                '\'' +
                ", supplierHardwareNumber='" +
                supplierHardwareNumber +
                '\'' +
                ", supplierSoftwareNumber='" +
                supplierSoftwareNumber +
                '\'' +
                ", supplierSoftwareVersion='" +
                supplierSoftwareVersion +
                '\'' +
                ", systemName='" +
                systemName +
                '\'' +
                ", additionalIdentifiers=" +
                additionalIdentifiers +
                '}';
    }
}