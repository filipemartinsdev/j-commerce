package com.orders.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AddressByCoordinatesResponse(
        Address address
) {
    public static record Address (
        String road,
        @JsonProperty("suburb") String neighborhood,
        String city,
        String municipality,
        @JsonProperty("state_district") String stateDistrict,
        String state,
        @JsonProperty("postcode") String zipCode,
        @JsonProperty("country_code") String countryCode,
        @JsonProperty("ISO3166-2-lvl4") String countryStateCode
    ){}
}
