package jcommerce;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class ProductSpikeTest extends Simulation {
    private static String JWT;

    final static String PRODUCT_URL = "http://localhost:8081";

    {
        JWT = Utils.getAdminJWT();
    }

    HttpProtocolBuilder productProtocol = http
            .baseUrl(PRODUCT_URL)
            .acceptHeader("application/json");

    ScenarioBuilder productScenario = scenario("Catalogue Warmup")
            .exec(http("Catalogue Warmup")
                    .get("/api/v1/products")
                    .header("Authorization", "Bearer "+JWT)
            );

    {
        setUp(productScenario.injectOpen(
                rampUsers(100).during(10),
                rampUsers(15000).during(300),
                rampUsers(3000).during(150),
                rampUsers(1500).during(150)
        )).protocols(productProtocol);
    }
}
