package com.dipasoftware.autodiag.core.models;


import com.dipasoftware.autodiag.core.parameters.ParameterCategory;
import com.dipasoftware.autodiag.diagnostic.DiagnosticService;

import java.io.Serializable;
import java.util.Objects;

/**
 * ****************************************************************************
 * Classe: ParameterDefinition
 *
 * Package:
 * com.dipasoftware.autodiag.core.models
 *
 * Tipo:
 * Model
 *
 * Descrizione:
 *
 * Definisce completamente un parametro diagnostico.
 *
 * Questa classe viene utilizzata dal motore diagnostico durante
 * tutta l'esecuzione dell'applicazione.
 *
 * NON contiene il valore letto dalla centralina.
 *
 * Contiene solamente tutte le informazioni necessarie per
 * interrogare il parametro.
 *
 *****************************************************************************/
public class ParameterDefinition implements Serializable {

    /**
     * Identificatore univoco del parametro.
     */
    private String id;

    /**
     * Nome visualizzato.
     */
    private String name;

    /**
     * Descrizione del parametro.
     */
    private String description;

    /**
     * Categoria del parametro.
     */
    private ParameterCategory category;

    /**
     * Servizio OBD.
     */
    private DiagnosticService service;

    /**
     * PID o DID della centralina.
     */
    private String identifier;

    /**
     * Identificatore della formula da utilizzare.
     */
    private String formulaId;

    /**
     * Unità logica.
     */
    private String unit;

    /**
     * Simbolo visualizzato.
     */
    private String displayUnit;

    /**
     * Valore minimo.
     */
    private double minimumValue;

    /**
     * Valore massimo.
     */
    private double maximumValue;

    /**
     * Numero di cifre decimali.
     */
    private int precision;

    /**
     * Gruppo di polling.
     */
    private String refreshGroup;

    /**
     * Parametro visibile.
     */
    private boolean visible;

    /**
     * Parametro supportato.
     */
    private boolean supported;

    /**
     * Abilita grafico.
     */
    private boolean graphEnabled;

    /**
     * Abilita logging.
     */
    private boolean loggingEnabled;

    /**
     * Parametro preferito.
     */
    private boolean favorite;

    /**
     * Soglia warning.
     */
    private Double warningThreshold;

    /**
     * Soglia critica.
     */
    private Double criticalThreshold;

    /**
     * Costruttore vuoto.
     */
    public ParameterDefinition() {
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

    public ParameterCategory getCategory() {
        return category;
    }

    public void setCategory(ParameterCategory category) {
        this.category = category;
    }

    public DiagnosticService getService() {
        return service;
    }

    public void setService(DiagnosticService service) {
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
        ParameterDefinition that = (ParameterDefinition) o;
        return Double.compare(minimumValue, that.minimumValue) == 0 && Double.compare(maximumValue, that.maximumValue) == 0 && precision == that.precision && visible == that.visible && supported == that.supported && graphEnabled == that.graphEnabled && loggingEnabled == that.loggingEnabled && favorite == that.favorite && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description) && category == that.category && service == that.service && Objects.equals(identifier, that.identifier) && Objects.equals(formulaId, that.formulaId) && Objects.equals(unit, that.unit) && Objects.equals(displayUnit, that.displayUnit) && Objects.equals(refreshGroup, that.refreshGroup) && Objects.equals(warningThreshold, that.warningThreshold) && Objects.equals(criticalThreshold, that.criticalThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, category, service, identifier, formulaId, unit, displayUnit, minimumValue, maximumValue, precision, refreshGroup, visible, supported, graphEnabled, loggingEnabled, favorite, warningThreshold, criticalThreshold);
    }

    @Override
    public String toString() {
        return "ParameterDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", service=" + service +
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

    // =====================================================================
    // Generare con Android Studio:
    //
    // Code -> Generate
    //
    // Getter
    // Setter
    // equals()
    // hashCode()
    // toString()
    // =====================================================================

}