package com.dipasoftware.autodiag.profiles.dto;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.Objects;

/**
 * ****************************************************************************
 * Classe: DtcDefinitionDto
 *
 * Package:
 * com.dipasoftware.autodiag.profiles.dto
 *
 * Tipo:
 * DTO (Data Transfer Object)
 *
 * Descrizione:
 *
 * Rappresenta un codice DTC letto dal file dtc.json.
 *
 * Questa classe viene utilizzata esclusivamente durante
 * la lettura dei profili ECU.
 *
 * Non contiene logica applicativa.
 *
 *****************************************************************************/
public class DtcDefinitionDto implements Serializable {

    /**
     * Codice DTC.
     *
     * Esempio:
     * P2002
     */
    @Json(name = "code")
    private String code;

    /**
     * Titolo breve del DTC.
     */
    @Json(name = "title")
    private String title;

    /**
     * Descrizione completa del DTC.
     */
    @Json(name = "description")
    private String description;

    /**
     * Livello di gravità.
     *
     * LOW
     * MEDIUM
     * HIGH
     * CRITICAL
     */
    @Json(name = "severity")
    private String severity;

    /**
     * Costruttore vuoto richiesto da Moshi.
     */
    public DtcDefinitionDto() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DtcDefinitionDto that = (DtcDefinitionDto) o;
        return Objects.equals(code, that.code) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(severity, that.severity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, description, severity);
    }

    @Override
    public String toString() {
        return "DtcDefinitionDto{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }
// =========================================================================
    // Generare automaticamente con Android Studio:
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