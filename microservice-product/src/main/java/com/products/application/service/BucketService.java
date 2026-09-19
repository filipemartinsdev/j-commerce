package com.products.application.service;

import java.net.URL;
import java.util.List;

public interface BucketService {
    URL getReadPreSignedURL(String key);

    List<URL> getReadPreSignedURLByPrefix(String prefix);

    URL getUploadPreSignedURL(String key);

    void deleteObject(String key);

    void deleteAllObjectsByPrefix(String keyPrefix);
}
