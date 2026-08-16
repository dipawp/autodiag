package com.dipasoftware.autodiag.profiles;

import org.junit.Test;

import static org.junit.Assert.*;

import com.dipasoftware.autodiag.profiles.dto.DtcDefinitionDto;

/**
 * ****************************************************************************
 * Test della classe DtcDefinitionDto.
 *****************************************************************************/
public class DtcDefinitionDtoTest {

    /**
     * Verifica getter e setter.
     */
    @Test
    public void testGetterSetter() {

        DtcDefinitionDto dto = new DtcDefinitionDto();

        dto.setCode("P2002");
        dto.setTitle("DPF Efficiency Below Threshold");
        dto.setDescription("Filtro antiparticolato con efficienza inferiore alla soglia.");
        dto.setSeverity("HIGH");

        assertEquals("P2002", dto.getCode());
        assertEquals("DPF Efficiency Below Threshold", dto.getTitle());
        assertEquals("Filtro antiparticolato con efficienza inferiore alla soglia.", dto.getDescription());
        assertEquals("HIGH", dto.getSeverity());
    }

    /**
     * Verifica che toString() non restituisca null.
     */
    @Test
    public void testToString() {

        DtcDefinitionDto dto = new DtcDefinitionDto();

        assertNotNull(dto.toString());
    }
}
