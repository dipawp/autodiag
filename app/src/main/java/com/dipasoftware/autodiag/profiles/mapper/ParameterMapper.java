package com.dipasoftware.autodiag.profiles.mapper;

import com.dipasoftware.autodiag.core.models.ParameterDefinition;
import com.dipasoftware.autodiag.core.parameters.ParameterCategory;
import com.dipasoftware.autodiag.diagnostic.DiagnosticService;
import com.dipasoftware.autodiag.profiles.dto.ParameterDefinitionDto;

/******************************************************************************
 *
 * Classe: ParameterMapper
 *
 * Package:
 * com.dipasoftware.autodiag.profiles.mapper
 *
 * Tipo:
 * Utility
 *
 * Descrizione:
 *
 * Converte un ParameterDefinitionDto nel corrispondente
 * ParameterDefinition utilizzato dal motore diagnostico.
 *
 * Tutti i DTO devono essere convertiti prima di essere utilizzati
 * dal resto dell'applicazione.
 *
 ******************************************************************************/
public final class ParameterMapper {

    /**
     * Costruttore privato.
     *
     * La classe contiene solo metodi statici.
     */
    private ParameterMapper() {
    }

    /**
     * Converte un ParameterDefinitionDto in ParameterDefinition.
     *
     * @param dto DTO letto dal file JSON.
     *
     * @return Model utilizzato dal motore diagnostico.
     *
     * @throws IllegalArgumentException se dto è null.
     */
    public static ParameterDefinition map(ParameterDefinitionDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("ParameterDefinitionDto nullo.");
        }

        ParameterDefinition model = new ParameterDefinition();

        model.setId(dto.getId());
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());

        model.setCategory(
                ParameterCategory.valueOf(dto.getCategory())
        );

        model.setService(
                DiagnosticService.fromCode(dto.getService())
        );

        model.setIdentifier(dto.getIdentifier());
        model.setFormulaId(dto.getFormulaId());

        model.setUnit(dto.getUnit());
        model.setDisplayUnit(dto.getDisplayUnit());

        model.setMinimumValue(dto.getMinimumValue());
        model.setMaximumValue(dto.getMaximumValue());

        model.setPrecision(dto.getPrecision());

        model.setRefreshGroup(dto.getRefreshGroup());

        model.setVisible(dto.isVisible());
        model.setSupported(dto.isSupported());

        model.setGraphEnabled(dto.isGraphEnabled());
        model.setLoggingEnabled(dto.isLoggingEnabled());

        model.setFavorite(dto.isFavorite());

        model.setWarningThreshold(dto.getWarningThreshold());
        model.setCriticalThreshold(dto.getCriticalThreshold());

        return model;
    }

}