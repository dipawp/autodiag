package com.dipasoftware.autodiag.profiles;

import org.junit.Test;

import static org.junit.Assert.*;

import com.dipasoftware.autodiag.profiles.dto.ParameterDefinitionDto;

/**
 * ****************************************************************************
 * Test della classe ParameterDefinitionDto.
 *****************************************************************************/
public class ParameterDefinitionDtoTest {

    /**
     * Verifica getter e setter.
     */
    @Test
    public void testGetterSetter() {

        ParameterDefinitionDto dto = new ParameterDefinitionDto();

        dto.setId("DPF_SOOT_MASS");
        dto.setName("DPF Soot Mass");
        dto.setService("22");
        dto.setIdentifier("F40C");

        assertEquals("DPF_SOOT_MASS", dto.getId());
        assertEquals("DPF Soot Mass", dto.getName());
        assertEquals("22", dto.getService());
        assertEquals("F40C", dto.getIdentifier());
    }

    /**
     * Verifica che toString non restituisca null.
     */
    @Test
    public void testToString() {

        ParameterDefinitionDto dto = new ParameterDefinitionDto();

        assertNotNull(dto.toString());

    }

}