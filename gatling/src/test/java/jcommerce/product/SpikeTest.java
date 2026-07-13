package jcommerce.product;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jcommerce.TokenManager;
import jcommerce.Utils;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SpikeTest extends Simulation {
    final static String PRODUCT_URL = "http://localhost:8081";

    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json");

    ScenarioBuilder productScenario = scenario("Catalogue Spike Test")
            .exec(session -> session.set("cursor", ""))

            .during(Duration.ofMinutes(10))
            .on(
                    exec(session -> session.set("JWT", TokenManager.getAdminToken())),

                    exec(http("Catalogue Spike Test")
                            .get("/api/v1/products")
                            .queryParam("cursor", session -> {
                                String cursor = session.get("cursor");
                                return cursor == null ? "" : cursor;
                            })
                            .header("Authorization", "Bearer #{JWT}")
                            .check(
                                    status().is(200),
                                    jsonPath("$.data.lastCursor").optional().saveAs("cursor")
                            )
                    ),

                    pause(Duration.ofSeconds(1))
            );

    {
        setUp(
                productScenario.injectClosed(
                        constantConcurrentUsers(100).during(Duration.ofMinutes(10))
                )
        ).protocols(productProtocol);
    }
}
