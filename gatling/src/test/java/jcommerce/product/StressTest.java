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
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class StressTest extends Simulation {
    final static String PRODUCT_URL = "http://localhost:8081";

    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json");

    ScenarioBuilder productScenario = scenario("Catalogue Stress Test")
            .exec(session -> session.set("cursor", ""))

            .during(Duration.ofMinutes(15))
            .on(
                    exec(session -> session.set("JWT", TokenManager.getAdminToken())),

                    exec(http("Catalogue Stress Test")
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
                        constantConcurrentUsers(20).during(Duration.ofMinutes(15))
                )
        ).protocols(productProtocol);
    }
}
