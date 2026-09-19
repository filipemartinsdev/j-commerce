package com.products.application.dto.admin;

import java.net.URL;
import java.util.UUID;

public record UploadImageResponse(
        UUID imageId,
        URL uploadURL
) {
}
