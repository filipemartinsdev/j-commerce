package com.orders.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orders.application.exception.InvalidGeocodingResponseException;

import java.util.List;

public record GeocodingResponse (
        List<Hit> hits
) {

    public static record Hit (
            Point point
    ){}

    public static record Point (
            Double lat,
            @JsonProperty("lng") Double lon
    ){}

    public void validate() {
        if (
                this.hits() == null ||
                this.hits().isEmpty() ||
                this.hits().getFirst() == null ||
                this.hits().getFirst().point() == null ||
                this.hits().getFirst().point().lat == null ||
                this.hits().getFirst().point().lon == null
        ) {
            throw new InvalidGeocodingResponseException("Invalid GeocodingResponse: " + this);
        }
    }
}
