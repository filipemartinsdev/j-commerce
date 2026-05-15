package com.orders.application.service;

import org.springframework.stereotype.Service;

@Service
public interface RouteService {
    Route route(Point pointA, Point pointB);

    record Point(double lat, double lon) {}

    record Route(long distanceInMeters, long timeInMilli) {}
}
