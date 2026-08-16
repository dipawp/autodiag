package com.dipasoftware.autodiag.profiles.mapper;

import com.dipasoftware.autodiag.core.models.ParameterDefinition;
import com.dipasoftware.autodiag.profiles.dto.ParameterDefinitionDto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/******************************************************************************
 * Test della classe ParameterMapper.
 ******************************************************************************/
public class ParameterMapperTest {

    /**
     * Verifica che il mapper copi correttamente i dati
     * dal DTO al Model.
     */
    @Test
    public void testMap() {

        ParameterDefinitionDto dto = new ParameterDefinitionDto();

        dto.setId("ENGINE_RPM");
        dto.setName("Engine RPM");
        dto.setDescription("Numero di giri motore");
        dto.setCategory("ENGINE");
        dto.setService("01");
        dto.setIdentifier("0C");
        dto.setFormulaId("UINT16_DIV4");
        dto.setUnit("RPM");
        dto.setDisplayUnit("rpm");
        dto.setMinimumValue(0);
        dto.setMaximumValue(8000);
        dto.setPrecision(0);
        dto.setRefreshGroup("FAST");
        dto.setVisible(true);
        dto.setSupported(true);
        dto.setGraphEnabled(true);
        dto.setLoggingEnabled(true);
        dto.setFavorite(true);

        ParameterDefinition model = ParameterMapper.map(dto);

        assertNotNull(model);

        assertEquals(dto.getId(), model.getId());
        assertEquals(dto.getIdentifier(), model.getIdentifier());
        assertEquals(dto.getFormulaId(), model.getFormulaId());
        assertEquals(dto.getDisplayUnit(), model.getDisplayUnit());
    }

}