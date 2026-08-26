package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * ****************************************************************************
 *
 * Classe.....: EcuDefinitionIdentifier
 *
 * Tipo.......: Model
 *
 * Package....: com.dipasoftware.autodiag.diagnostic
 *
 * Descrizione:
 *
 * Contiene gli identificativi che possono essere utilizzati dal catalogo
 * per riconoscere una specifica ECU.
 *
 * Una EcuDefinition può essere associata a più identificativi hardware,
 * software e part-number, perché la stessa famiglia ECU può esistere
 * con diverse revisioni o varianti.
 *
 * Gli identificatori non vengono interrogati direttamente dalla classe.
 *
 * La classe rappresenta esclusivamente:
 *
 * - numeri hardware compatibili;
 * - numeri software compatibili;
 * - part-number compatibili;
 * - supplier compatibili;
 * - eventuali pattern VIN.
 *
 * ****************************************************************************
 */
public class EcuDefinitionIdentifier {

    /**
     * Numeri hardware ECU compatibili.
     */
    @NonNull
    private final List<String> hardwareNumbers;

    /**
     * Numeri software ECU compatibili.
     */
    @NonNull
    private final List<String> softwareNumbers;

    /**
     * Part-number ECU compatibili.
     */
    @NonNull
    private final List<String> partNumbers;

    /**
     * Supplier compatibili.
     */
    @NonNull
    private final List<String> suppliers;

    /**
     * Pattern VIN compatibili.
     *
     * Non devono essere interpretati necessariamente come
     * regex Java: il loro significato sarà definito dal matcher.
     *
     * La V1 li tratta come pattern testuali semplici.
     */
    @NonNull
    private final List<String> vinPatterns;

    /**
     * Costruttore completo.
     *
     * @param hardwareNumbers hardware compatibili.
     * @param softwareNumbers software compatibili.
     * @param partNumbers part-number compatibili.
     * @param suppliers supplier compatibili.
     * @param vinPatterns pattern VIN.
     */
    public EcuDefinitionIdentifier(
            @NonNull List<String> hardwareNumbers,
            @NonNull List<String> softwareNumbers,
            @NonNull List<String> partNumbers,
            @NonNull List<String> suppliers,
            @NonNull List<String> vinPatterns) {

        this.hardwareNumbers =
                immutableNormalizedList(
                        hardwareNumbers
                );

        this.softwareNumbers =
                immutableNormalizedList(
                        softwareNumbers
                );

        this.partNumbers =
                immutableNormalizedList(
                        partNumbers
                );

        this.suppliers =
                immutableNormalizedList(
                        suppliers
                );

        this.vinPatterns =
                immutableNormalizedList(
                        vinPatterns
                );
    }

    /**
     * Costruttore vuoto.
     *
     * Utile per ECU che non dispongono ancora
     * di identificativi catalogati.
     */
    public EcuDefinitionIdentifier() {

        this(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * Restituisce i numeri hardware.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getHardwareNumbers() {

        return hardwareNumbers;
    }

    /**
     * Restituisce i numeri software.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getSoftwareNumbers() {

        return softwareNumbers;
    }

    /**
     * Restituisce i part-number.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getPartNumbers() {

        return partNumbers;
    }

    /**
     * Restituisce i supplier.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getSuppliers() {

        return suppliers;
    }

    /**
     * Restituisce i pattern VIN.
     *
     * @return lista immutabile.
     */
    @NonNull
    public List<String> getVinPatterns() {

        return vinPatterns;
    }

    /**
     * Indica se esiste almeno un hardware number.
     *
     * @return true se presente.
     */
    public boolean hasHardwareNumbers() {

        return !hardwareNumbers.isEmpty();
    }

    /**
     * Indica se esiste almeno un software number.
     *
     * @return true se presente.
     */
    public boolean hasSoftwareNumbers() {

        return !softwareNumbers.isEmpty();
    }

    /**
     * Indica se esiste almeno un part-number.
     *
     * @return true se presente.
     */
    public boolean hasPartNumbers() {

        return !partNumbers.isEmpty();
    }

    /**
     * Indica se esiste almeno un supplier.
     *
     * @return true se presente.
     */
    public boolean hasSuppliers() {

        return !suppliers.isEmpty();
    }

    /**
     * Indica se esiste almeno un pattern VIN.
     *
     * @return true se presente.
     */
    public boolean hasVinPatterns() {

        return !vinPatterns.isEmpty();
    }

    /**
     * Indica se il valore è presente tra gli hardware number.
     *
     * Il confronto è case-insensitive.
     *
     * @param value valore da cercare.
     *
     * @return true se trovato.
     */
    public boolean matchesHardware(
            @NonNull String value) {

        return containsNormalized(
                hardwareNumbers,
                value
        );
    }

    /**
     * Indica se il valore è presente tra gli software number.
     *
     * @param value valore da cercare.
     *
     * @return true se trovato.
     */
    public boolean matchesSoftware(
            @NonNull String value) {

        return containsNormalized(
                softwareNumbers,
                value
        );
    }

    /**
     * Indica se il valore è presente tra i part-number.
     *
     * @param value valore da cercare.
     *
     * @return true se trovato.
     */
    public boolean matchesPartNumber(
            @NonNull String value) {

        return containsNormalized(
                partNumbers,
                value
        );
    }

    /**
     * Indica se il supplier è presente.
     *
     * @param value supplier.
     *
     * @return true se trovato.
     */
    public boolean matchesSupplier(
            @NonNull String value) {

        return containsNormalized(
                suppliers,
                value
        );
    }

    /**
     * Verifica un VIN contro i pattern dichiarati.
     *
     * La V1 utilizza un confronto:
     *
     * - case-insensitive;
     * - contains.
     *
     * Non viene utilizzata una regex arbitraria, così un JSON
     * proveniente da una fonte esterna non può introdurre
     * espressioni regolari inattese.
     *
     * @param vin VIN.
     *
     * @return true se compatibile.
     */
    public boolean matchesVin(
            @NonNull String vin) {

        String normalizedVin =
                normalize(
                        vin
                );

        if (normalizedVin.isEmpty()) {

            return false;
        }

        for (
                String pattern :
                vinPatterns
        ) {

            if (normalizedVin.contains(
                    pattern
            )) {

                return true;
            }
        }

        return false;
    }

    /**
     * Verifica se non sono stati definiti identificatori.
     *
     * @return true se la definizione è vuota.
     */
    public boolean isEmpty() {

        return hardwareNumbers.isEmpty()
                &&
                softwareNumbers.isEmpty()
                &&
                partNumbers.isEmpty()
                &&
                suppliers.isEmpty()
                &&
                vinPatterns.isEmpty();
    }

    /**
     * Normalizza una lista e la rende immutabile.
     *
     * I valori null e vuoti vengono ignorati.
     *
     * @param values lista originale.
     *
     * @return lista normalizzata.
     */
    @NonNull
    private List<String> immutableNormalizedList(
            @NonNull List<String> values) {

        List<String> result =
                new ArrayList<>();

        for (
                String value :
                values
        ) {

            if (value == null) {
                continue;
            }

            String normalized =
                    normalize(
                            value
                    );

            if (normalized.isEmpty()) {
                continue;
            }

            if (!result.contains(
                    normalized
            )) {

                result.add(
                        normalized
                );
            }
        }

        return Collections.unmodifiableList(
                result
        );
    }

    /**
     * Normalizza una stringa.
     *
     * @param value valore.
     *
     * @return stringa normalizzata.
     */
    @NonNull
    private String normalize(
            @NonNull String value) {

        return value
                .trim()
                .toUpperCase(
                        Locale.US
                );
    }

    /**
     * Cerca un valore normalizzato in una lista.
     *
     * @param values valori.
     * @param value valore da cercare.
     *
     * @return true se trovato.
     */
    private boolean containsNormalized(
            @NonNull List<String> values,
            @NonNull String value) {

        String normalized =
                normalize(
                        value
                );

        if (normalized.isEmpty()) {

            return false;
        }

        return values.contains(
                normalized
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

        return "EcuDefinitionIdentifier{" +
                "hardwareNumbers=" +
                hardwareNumbers +
                ", softwareNumbers=" +
                softwareNumbers +
                ", partNumbers=" +
                partNumbers +
                ", suppliers=" +
                suppliers +
                ", vinPatterns=" +
                vinPatterns +
                '}';
    }
}