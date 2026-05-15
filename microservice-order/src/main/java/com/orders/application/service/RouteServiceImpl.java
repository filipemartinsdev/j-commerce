package com.orders.application.service;

import com.orders.application.dto.RouteRequest;
import com.orders.application.dto.RouteResponse;
import com.orders.application.exception.InvalidRouteResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RouteServiceImpl implements RouteService {
    @Value("${graphHopperClient.apiKey}")
    private String GRAPH_HOPPER_API_KEY;

    private final GraphHopperClient graphHopperClient;

    public RouteServiceImpl(GraphHopperClient graphHopperClient) {
        this.graphHopperClient = graphHopperClient;
    }

    @Override
    public Route route(Point pointA, Point pointB) {
        Double[][] points = new Double[2][2];

        /*
        * The GraphHopper API needs the inverted default points, then:
        * [lon, lat] instead of [lat, lon]
        * **/

        points[0][0] = pointA.lon();
        points[0][1] = pointA.lat();

        points[1][0] = pointB.lon();
        points[1][1] = pointA.lat();

        RouteRequest request = new RouteRequest("car", points);

        RouteResponse routeResponse = graphHopperClient.route(
                GRAPH_HOPPER_API_KEY, request
        ).getBody();

        if (!isRouteResponseValid(routeResponse)) {
            throw new InvalidRouteResponseException();
        }

        return new Route(
                routeResponse.paths().getFirst().distance(),
                routeResponse.paths().getFirst().time()
        );
    }

    private boolean isRouteResponseValid(RouteResponse routeResponse) {
    return routeResponse != null &&
            routeResponse.paths() != null &&
            !routeResponse.paths().isEmpty() &&
            routeResponse.paths().getFirst() != null &&
            routeResponse.paths().getFirst().distance() != null &&
            routeResponse.paths().getFirst().time() != null;
    }
}
