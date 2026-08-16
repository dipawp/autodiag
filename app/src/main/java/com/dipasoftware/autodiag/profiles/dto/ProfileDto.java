package com.dipasoftware.autodiag.profiles.dto;

import com.squareup.moshi.Json;

/******************************************************************************
 *
 * Classe.....: ProfileDto
 *
 * Tipo.......: DTO (Data Transfer Object)
 *
 * Package....: com.dipasoftware.autodiag.profiles.dto
 *
 * Descrizione:
 *
 * Rappresenta il contenuto del file profile.json.
 *
 * Questa classe viene utilizzata esclusivamente da Moshi
 * per leggere il file JSON del profilo ECU.
 *
 * NON contiene alcuna logica applicativa.
 * NON deve essere utilizzata direttamente dalla UI.
 *
 ******************************************************************************/
public class ProfileDto {

    /**
     * Identificatore univoco del profilo.
     *
     * Esempio:
     * FIAT_BRAVO_1_9_MJET_BOSCH_EDC16C39
     */
    @Json(name = "profileId")
    private String profileId;

    /**
     * Costruttore della centralina.
     *
     * Esempio:
     * Bosch
     */
    @Json(name = "manufacturer")
    private String manufacturer;

    /**
     * Modello ECU.
     *
     * Esempio:
     * EDC16C39
     */
    @Json(name = "ecuModel")
    private String ecuModel;

    /**
     * Marca del veicolo.
     */
    @Json(name = "vehicleBrand")
    private String vehicleBrand;

    /**
     * Modello del veicolo.
     */
    @Json(name = "vehicleModel")
    private String vehicleModel;

    /**
     * Motorizzazione.
     */
    @Json(name = "engine")
    private String engine;

    /**
     * Protocollo diagnostico.
     *
     * Esempio:
     * CAN
     */
    @Json(name = "protocol")
    private String protocol;

    /**
     * Baud rate della comunicazione.
     */
    @Json(name = "baudRate")
    private int baudRate;

    /**
     * Nome del file contenente i parametri.
     */
    @Json(name = "parametersFile")
    private String parametersFile;

    /**
     * Nome del file contenente i DTC.
     */
    @Json(name = "dtcFile")
    private String dtcFile;

    /**
     * Costruttore vuoto richiesto da Moshi.
     */
    public ProfileDto() {
    }

    // ===== GETTER =====

    public String getProfileId() {
        return profileId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getEcuModel() {
        return ecuModel;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getEngine() {
        return engine;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public String getParametersFile() {
        return parametersFile;
    }

    public String getDtcFile() {
        return dtcFile;
    }

    // ===== SETTER =====

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setEcuModel(String ecuModel) {
        this.ecuModel = ecuModel;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public void setParametersFile(String parametersFile) {
        this.parametersFile = parametersFile;
    }

    public void setDtcFile(String dtcFile) {
        this.dtcFile = dtcFile;
    }

    @Override
    public String toString() {
        return "ProfileDto{" +
                "profileId='" + profileId + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", ecuModel='" + ecuModel + '\'' +
                ", vehicleBrand='" + vehicleBrand + '\'' +
                ", vehicleModel='" + vehicleModel + '\'' +
                ", engine='" + engine + '\'' +
                ", protocol='" + protocol + '\'' +
                ", baudRate=" + baudRate +
                ", parametersFile='" + parametersFile + '\'' +
                ", dtcFile='" + dtcFile + '\'' +
                '}';
    }
}