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

public class ProductStressTest extends Simulation {
    private static String JWT;

    final static String PRODUCT_URL = "http://localhost:8081";

    private static final int TOTAL_PAGES = 500;

    private final Iterator<Map<String, Object>> pageIndexFeeder = Stream.generate(() -> {
        int index = ThreadLocalRandom.current().nextInt(TOTAL_PAGES);
        return Collections.<String, Object>singletonMap("pageIndex", index);
    }).iterator();

    {
        JWT = Utils.getAdminJWT();
    }

    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json");

    ScenarioBuilder productScenario = scenario("Catalogue Stress Test")
            .feed(pageIndexFeeder)
            .exec(http("Catalogue Stress Test")
                    .get("/api/v1/products")
                    .header("Authorization", "Bearer "+JWT)
            );

    {
        setUp(productScenario.injectOpen(
                rampUsers(100).during(10),
                rampUsers(12000).during(600) // 20RPS
        )).protocols(productProtocol);
    }
}
