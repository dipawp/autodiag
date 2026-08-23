package com.dipasoftware.autodiag.diagnostic;

import androidx.annotation.NonNull;

/**
 * Definizione di una ECU diagnostica.
 *
 * Identifica il dataset OEM associato a una specifica
 * combinazione di costruttore, modello, motore e centralina.
 */
public class EcuDefinition {

    @NonNull
    private final String brand;

    @NonNull
    private final String model;

    @NonNull
    private final String engine;

    @NonNull
    private final String ecu;

    @NonNull
    private final String protocol;

    @NonNull
    private final String pidFile;

    public EcuDefinition(
            @NonNull String brand,
            @NonNull String model,
            @NonNull String engine,
            @NonNull String ecu,
            @NonNull String protocol,
            @NonNull String pidFile) {

        this.brand = brand.trim();
        this.model = model.trim();
        this.engine = engine.trim();
        this.ecu = ecu.trim();
        this.protocol = protocol.trim();
        this.pidFile = pidFile.trim();
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    @NonNull
    public String getModel() {
        return model;
    }

    @NonNull
    public String getEngine() {
        return engine;
    }

    @NonNull
    public String getEcu() {
        return ecu;
    }

    @NonNull
    public String getProtocol() {
        return protocol;
    }

    @NonNull
    public String getPidFile() {
        return pidFile;
    }

    @NonNull
    @Override
    public String toString() {
        return "EcuDefinition{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", engine='" + engine + '\'' +
                ", ecu='" + ecu + '\'' +
                ", protocol='" + protocol + '\'' +
                ", pidFile='" + pidFile + '\'' +
                '}';
    }
}