package com.dipasoftware.autodiag.profiles;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.dipasoftware.autodiag.profiles.dto.ProfileDto;
import com.dipasoftware.autodiag.profiles.loader.ProfileLoader;

/**
 * ****************************************************************************
 * Test della classe ProfileLoader.
 *****************************************************************************/
public class ProfileLoaderTest {

    @Test
    public void loadAssetAsString_ShouldReadJsonFile() throws Exception {

        Context context = ApplicationProvider.getApplicationContext();

        ProfileLoader loader = new ProfileLoader(context);

        String json = loader.loadAssetAsString(
                "pids/test/profile.json"
        );

        assertNotNull(json);

        assertFalse(json.isEmpty());

    }


    /**
     * Verifica la conversione JSON -> ProfileDto.
     */
    @Test
    public void loadProfile_ShouldReturnProfileDto()
            throws Exception {

        Context context =
                ApplicationProvider.getApplicationContext();

        ProfileLoader loader =
                new ProfileLoader(context);

        ProfileDto profile = loader.loadProfile(
                "pids/test/profile.json"
        );

        assertNotNull(profile);

        assertEquals(
                "TEST_PROFILE",
                profile.getProfileId()
        );

    }

}
