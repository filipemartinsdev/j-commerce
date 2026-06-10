package com.orders.application.service;

import com.orders.application.dto.AddressByCoordinatesResponse;
import com.orders.application.dto.GeocodingResponse;
import com.orders.application.exception.InvalidGeocodingResponseException;
import com.orders.application.exception.InvalidReverseGeocodingResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    @Value("${graphHopperClient.apiKey}")
    private String GRAPH_HOPPER_API_KEY;

    private final NominatimClient nominatimClient;
    private final GraphHopperClient graphHopperClient;

    public GeocodingServiceImpl(NominatimClient nominatimClient, GraphHopperClient graphHopperClient) {
        this.nominatimClient = nominatimClient;
        this.graphHopperClient = graphHopperClient;
    }

    @Override
    public Point toCoordinates(Address address) {
        String query = address.street() + ", " +
                address.neighborhood() + ", " +
                address.city() + ", " +
                address.stateCode() + ", " +
                address.countryCode() + ", ";

        GeocodingResponse response = graphHopperClient.geocode(query, GRAPH_HOPPER_API_KEY).getBody();

        if (response == null)
            throw new InvalidGeocodingResponseException("Null geocoding response");

        return parseGraphHopperResponseToPoint(response);
    }

    private Point parseGraphHopperResponseToPoint(GeocodingResponse response) {
        return new Point(
                response.hits().getFirst().point().lat(),
                response.hits().getFirst().point().lon()
        );
    }

    @Override
    public Address toAddress(Point coordinates) {
        AddressByCoordinatesResponse response = nominatimClient.getAddressByCoordinates(
                coordinates.lat(),
                coordinates.lon(),
                "json"
        ).getBody();

        if (response == null){
            throw new InvalidGeocodingResponseException("Null geocoding response");
        }

        return parseNominatimResponseToAddress(response);
    }

    private Address parseNominatimResponseToAddress(AddressByCoordinatesResponse response) {
        String city;

        if (response.address().city() != null)
            city = response.address().city();
        else if (response.address().municipality() != null)
            city = response.address().municipality();
        else if (response.address().stateDistrict() != null)
            city = response.address().stateDistrict();
        else
            throw new InvalidReverseGeocodingResponseException("Invalid Nominatim response");

        return new Address(
                response.address().road(),
                response.address().neighborhood(),
                city,
                response.address().zipCode().replace("-", ""),
                response.address().countryStateCode().substring(3), /*The response format is: BR-UF*/
                response.address().countryCode()
        );
    }
}
