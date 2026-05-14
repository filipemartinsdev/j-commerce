package com.orders.application.dto;

import java.util.List;

public record RouteResponse (
    List<Path> paths
){
    public static record Path (
            Long distance,
            Long time
    ){}
}
