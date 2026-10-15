package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.TestUtility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Real-network test against the NASA APOD API. Excluded from the default build
 * (tagged {@code network} and gated behind a system property), so routine runs are
 * deterministic and never depend on api.nasa.gov availability.
 *
 * <p>Run it explicitly with:
 * {@code mvn test -DnetworkTests=true -Dtest=NasaApodNetworkTest}</p>
 *
 * <p>If NASA is down or rate-limits the request, the test is skipped — never failed —
 * because it is not this application's fault.</p>
 */
@SpringBootTest
@Tag("network")
@EnabledIfSystemProperty(named = "networkTests", matches = "true")
class NasaApodNetworkTest {

    @Autowired
    @Lazy
    private TestUtility testUtility;

    @Test
    void nasaApodApi_deliversDecodableImage() {
        Optional<BufferedImage> image = testUtility.fetchNasaApodImage();
        assumeTrue(image.isPresent(),
                "NASA APOD delivered no image (API down or rate-limited) — skipping, not failing");

        BufferedImage img = image.get();
        assertNotNull(img, "Fetched APOD image must not be null");
        assertTrue(img.getWidth() > 0 && img.getHeight() > 0,
                "Fetched APOD image must have positive dimensions");
    }
}
