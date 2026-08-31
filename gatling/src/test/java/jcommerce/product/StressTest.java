package jcommerce.product;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jcommerce.TokenManager;
import jcommerce.Utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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

    final static int TARGET_RPS = 50;
    final static Duration DURATION = Duration.ofMinutes(15);

    static String BODY_TEMPLATE = getBodyFile();


    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    ScenarioBuilder productScenario = scenario("Catalogue Stress Test")
            .exec(session -> session.set("cursor", ""))

            .during(DURATION)

            .on(
                    exec(session -> session.set("JWT", TokenManager.getAdminToken())),

                    exec(http("Catalogue Warmup")
                            .post("/api/v2/graphql")

                            .queryParam("cursor", session -> {
                                String cursor = session.get("cursor");
                                return cursor == null ? "" : cursor;
                            })

                            .header("Authorization", "Bearer #{JWT}")

                            .body(StringBody(session ->
                                    assembleBody(session.get("cursor"))
                            ))

                            .check(
                                    status().is(200),
                                    jsonPath("$.data.pageInfo.endCursor").optional().saveAs("cursor")
                            )
                    )

                    .pause(Duration.ofSeconds(1))
            );


    {
        setUp(
                productScenario.injectClosed(
                        constantConcurrentUsers(TARGET_RPS).during(DURATION)
                )
        ).protocols(productProtocol);
    }


    private static String getBodyFile() {
        try {
            var uri = Objects.requireNonNull(Warmup.class.getResource("/bodies/catalogue.json")).toURI();
            return Files.readString(Paths.get(uri));
        } catch (Exception e){
            throw new RuntimeException("Error while loading catalogue.json");
        }
    }

    private static String assembleBody(String cursor){
        if (cursor == null || cursor.isEmpty())
            return BODY_TEMPLATE.formatted("null");
        else
            return BODY_TEMPLATE.formatted("\""+cursor+"\"");
    }
}
