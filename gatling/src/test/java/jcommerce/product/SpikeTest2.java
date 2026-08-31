package jcommerce.product;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import jcommerce.TokenManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SpikeTest2 extends Simulation {
    final static String PRODUCT_URL = "http://localhost:8081";

    final static int TARGET_RPS = 200;
    final static Duration DURATION = Duration.ofMinutes(10);

    static String BODY_TEMPLATE = getBodyFile();


    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    ScenarioBuilder productScenario = scenario("Catalogue Spike Test 2")
            .exec(session -> session.set("cursor", ""))

            .during(DURATION)

            .on(
                    exec(session -> session.set("JWT", TokenManager.getAdminToken())),

                    exec(http("Catalogue Spike Test 2")
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
            var uri = Objects.requireNonNull(SpikeTest.class.getResource("/bodies/catalogue.json")).toURI();
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
