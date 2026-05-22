package com.se4458.hotelbooking.hoteladminservice.hotel;

import com.se4458.hotelbooking.hoteladminservice.common.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HotelImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    private final Path imageRoot;

    public HotelImageStorageService(@Value("${app.upload.hotel-images-dir:uploads/hotel-images}") String imageRoot) {
        this.imageRoot = Path.of(imageRoot).toAbsolutePath().normalize();
    }

    public String store(UUID hotelId, MultipartFile image) {
        validate(image);
        createImageRoot();

        String fileName = hotelId + "-" + UUID.randomUUID() + extension(image);
        Path destination = imageRoot.resolve(fileName).normalize();
        if (!destination.startsWith(imageRoot)) {
            throw new IllegalArgumentException("Invalid image destination.");
        }

        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, destination);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store hotel image.", exception);
        }

        return "/api/v1/admin/hotel-images/" + fileName;
    }

    public Resource load(String fileName) {
        if (fileName == null || fileName.contains("/") || fileName.contains("\\")) {
            throw new NotFoundException("Hotel image was not found.");
        }

        Path imagePath = imageRoot.resolve(fileName).normalize();
        if (!imagePath.startsWith(imageRoot) || !Files.isRegularFile(imagePath)) {
            throw new NotFoundException("Hotel image was not found.");
        }

        try {
            return new UrlResource(imagePath.toUri());
        } catch (IOException exception) {
            throw new NotFoundException("Hotel image was not found.");
        }
    }

    public MediaType mediaType(String fileName) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lowerName.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lowerName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }

    private void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only JPEG, PNG, WEBP, and GIF images are supported.");
        }
    }

    private String extension(MultipartFile image) {
        String contentType = image.getContentType();
        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        if (MediaType.IMAGE_GIF_VALUE.equalsIgnoreCase(contentType)) {
            return ".gif";
        }
        return ".jpg";
    }

    private void createImageRoot() {
        try {
            Files.createDirectories(imageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not prepare hotel image storage.", exception);
        }
    }
}
