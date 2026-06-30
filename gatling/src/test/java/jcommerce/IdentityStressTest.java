package jcommerce;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class IdentityStressTest extends Simulation {
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioLogin = scenario("Identity Stress Test")
            .exec(http("Identity Stress Test")
                    .post("/api/v1/auth/login")
                    .body(RawFileBody("bodies/login-admin.json")).asJson()
            );

    {
        setUp(scenarioLogin.injectOpen(
                rampUsers(900).during(300)
        )).protocols(httpProtocol);
    }
}
