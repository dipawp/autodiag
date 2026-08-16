package com.dipasoftware.autodiag.profiles.dto;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.Objects;

/**
 * ****************************************************************************
 * Classe: ParameterDefinitionDto
 *
 * Package:
 * com.dipasoftware.autodiag.profiles.dto
 *
 * Tipo:
 * DTO (Data Transfer Object)
 *
 * Descrizione:
 *
 * Rappresenta un parametro letto dal file parameters.json.
 *
 * Questa classe NON contiene logica applicativa.
 *
 * Viene utilizzata esclusivamente durante la lettura dei profili ECU.
 *
 *****************************************************************************/
public class ParameterDefinitionDto implements Serializable {

    /**
     * Identificatore univoco del parametro.
     *
     * Esempio:
     * DPF_SOOT_MASS
     */
    @Json(name = "id")
    private String id;

    /**
     * Nome visualizzato.
     */
    @Json(name = "name")
    private String name;

    /**
     * Descrizione del parametro.
     */
    @Json(name = "description")
    private String description;

    /**
     * Categoria.
     *
     * Esempio:
     * DPF
     * ENGINE
     * TURBO
     */
    @Json(name = "category")
    private String category;

    /**
     * Servizio diagnostico.
     *
     * Esempio:
     * 01
     * 22
     */
    @Json(name = "service")
    private String service;

    /**
     * PID o DID.
     *
     * Esempio:
     * F40C
     */
    @Json(name = "identifier")
    private String identifier;

    /**
     * Identificatore della formula.
     *
     * Esempio:
     * UINT16_DIV10
     */
    @Json(name = "formulaId")
    private String formulaId;

    /**
     * Unità logica.
     *
     * Esempio:
     * GRAM
     */
    @Json(name = "unit")
    private String unit;

    /**
     * Simbolo visualizzato.
     *
     * Esempio:
     * g
     */
    @Json(name = "displayUnit")
    private String displayUnit;

    /**
     * Valore minimo.
     */
    @Json(name = "minimumValue")
    private double minimumValue;

    /**
     * Valore massimo.
     */
    @Json(name = "maximumValue")
    private double maximumValue;

    /**
     * Numero di decimali.
     */
    @Json(name = "precision")
    private int precision;

    /**
     * Gruppo di polling.
     *
     * FAST
     * NORMAL
     * SLOW
     */
    @Json(name = "refreshGroup")
    private String refreshGroup;

    /**
     * Indica se deve essere mostrato.
     */
    @Json(name = "visible")
    private boolean visible;

    /**
     * Indica se è supportato.
     */
    @Json(name = "supported")
    private boolean supported;

    /**
     * Abilita il grafico.
     */
    @Json(name = "graphEnabled")
    private boolean graphEnabled;

    /**
     * Abilita il logging.
     */
    @Json(name = "loggingEnabled")
    private boolean loggingEnabled;

    /**
     * Preferito.
     */
    @Json(name = "favorite")
    private boolean favorite;

    /**
     * Soglia warning.
     */
    @Json(name = "warningThreshold")
    private Double warningThreshold;

    /**
     * Soglia critica.
     */
    @Json(name = "criticalThreshold")
    private Double criticalThreshold;

    /**
     * Costruttore vuoto richiesto da Moshi.
     */
    public ParameterDefinitionDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFormulaId() {
        return formulaId;
    }

    public void setFormulaId(String formulaId) {
        this.formulaId = formulaId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(String displayUnit) {
        this.displayUnit = displayUnit;
    }

    public double getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(double maximumValue) {
        this.maximumValue = maximumValue;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getRefreshGroup() {
        return refreshGroup;
    }

    public void setRefreshGroup(String refreshGroup) {
        this.refreshGroup = refreshGroup;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public boolean isGraphEnabled() {
        return graphEnabled;
    }

    public void setGraphEnabled(boolean graphEnabled) {
        this.graphEnabled = graphEnabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Double getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public Double getCriticalThreshold() {
        return criticalThreshold;
    }

    public void setCriticalThreshold(Double criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ParameterDefinitionDto that = (ParameterDefinitionDto) o;
        return Double.compare(minimumValue, that.minimumValue) == 0 && Double.compare(maximumValue, that.maximumValue) == 0 && precision == that.precision && visible == that.visible && supported == that.supported && graphEnabled == that.graphEnabled && loggingEnabled == that.loggingEnabled && favorite == that.favorite && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(category, that.category) && Objects.equals(service, that.service) && Objects.equals(identifier, that.identifier) && Objects.equals(formulaId, that.formulaId) && Objects.equals(unit, that.unit) && Objects.equals(displayUnit, that.displayUnit) && Objects.equals(refreshGroup, that.refreshGroup) && Objects.equals(warningThreshold, that.warningThreshold) && Objects.equals(criticalThreshold, that.criticalThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, service, identifier, formulaId, unit, displayUnit, minimumValue, maximumValue, precision, refreshGroup, visible, supported, graphEnabled, loggingEnabled, favorite, warningThreshold, criticalThreshold);
    }

    @Override
    public String toString() {
        return "ParameterDefinitionDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", service='" + service + '\'' +
                ", identifier='" + identifier + '\'' +
                ", formulaId='" + formulaId + '\'' +
                ", unit='" + unit + '\'' +
                ", displayUnit='" + displayUnit + '\'' +
                ", minimumValue=" + minimumValue +
                ", maximumValue=" + maximumValue +
                ", precision=" + precision +
                ", refreshGroup='" + refreshGroup + '\'' +
                ", visible=" + visible +
                ", supported=" + supported +
                ", graphEnabled=" + graphEnabled +
                ", loggingEnabled=" + loggingEnabled +
                ", favorite=" + favorite +
                ", warningThreshold=" + warningThreshold +
                ", criticalThreshold=" + criticalThreshold +
                '}';
    }

    // =========================================================================
    // Da questo punto genera automaticamente con Android Studio:
    //
    // Code -> Generate...
    //
    // - Getter
    // - Setter
    // - toString()
    // - equals()
    // - hashCode()
    // =========================================================================
}