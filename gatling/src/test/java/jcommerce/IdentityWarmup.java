package jcommerce;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class IdentityWarmup extends Simulation {
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioLogin = scenario("Identity Warmup")
            .exec(http("Identity Warmup")
                    .post("/api/v1/auth/login")
                    .body(RawFileBody("bodies/login-admin.json")).asJson()
            );

    {
        setUp(scenarioLogin.injectOpen(
                rampUsers(180).during(180)
        )).protocols(httpProtocol);
    }
}
