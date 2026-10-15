package com.cloud.NetworkCloudDrive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
public class TestUtility {
    private final Logger logger = LoggerFactory.getLogger(TestUtility.class);

    private final String NASA_APOD_URL =
            "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches today's NASA Astronomy Picture of the Day (APOD) as a BufferedImage.
     * Returns empty if the APOD is a video, the request fails, the response body
     * contains no image bytes, or the image can't be read.
     */
    public Optional<BufferedImage> fetchNasaApodImage() {
        try {
            HttpResponse<String> jsonResponse = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(NASA_APOD_URL))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (jsonResponse.statusCode() / 100 != 2) {
                logger.warn("NASA APOD request failed with status {}", jsonResponse.statusCode());
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(jsonResponse.body());
            String imageUrl = root.get("url").asString();
            if (!imageUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)(\\?.*)?$")) {
                return Optional.empty();
            }

            HttpResponse<byte[]> imageResponse = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create(imageUrl))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray()
            );
            if (imageResponse.statusCode() / 100 != 2) {
                logger.warn("NASA APOD image request failed with status {}", imageResponse.statusCode());
                return Optional.empty();
            }

            byte[] imageBytes = imageResponse.body();
            if (imageBytes == null || imageBytes.length == 0) {
                logger.warn("NASA APOD responded with an empty image (status {})", imageResponse.statusCode());
                return Optional.empty();
            }

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) {
                logger.warn("NASA APOD response could not be decoded as an image");
                return Optional.empty();
            }
            return Optional.of(img);
        } catch (Exception e) {
            logger.warn("Failed to fetch NASA APOD image", e);
            return Optional.empty();
        }
    }

    /**
     * Creates a smooth gradient BufferedImage of the given dimensions.
     */
    public BufferedImage gradient(int width, int height, Color from, Color to) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, from, width, height, to));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }
}
