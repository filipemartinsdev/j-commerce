package jcommerce;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ProductStressTestAWS extends Simulation {
    private static final String JWT = Utils.getAdminJWT();

    private static final String PRODUCT_URL = "http://localhost:8081";

    private static final int TOTAL_PAGES = 5000;

    private final Iterator<Map<String, Object>> pageIndexFeeder = Stream.generate(() -> {
        int index = ThreadLocalRandom.current().nextInt(TOTAL_PAGES);
        return Collections.<String, Object>singletonMap("pageIndex", index);
    }).iterator();

    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json");

    ScenarioBuilder productScenario = scenario("Catalogue Stress Test AWS")
            .feed(pageIndexFeeder)
            .exec(http("Catalogue Stress Test AWS")
                    .get("/api/v1/products")
                    .header("Authorization", "Bearer "+JWT)
            );

    {
        setUp(productScenario.injectOpen(
                rampUsers(20).during(10),
                rampUsers(1000).during(1020)
        )).protocols(productProtocol);
    }
}
