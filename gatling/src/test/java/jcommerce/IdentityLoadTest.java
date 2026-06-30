package jcommerce;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class IdentityLoadTest extends Simulation {
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    ScenarioBuilder scenarioLogin = scenario("Identity Load Test")
            .exec(http("Load Test")
                    .post("/api/v1/auth/login")
                    .body(RawFileBody("bodies/login-admin.json")).asJson()
            );

    {
        setUp(scenarioLogin.injectOpen(
                rampUsers(600).during(300)
        )).protocols(httpProtocol);
    }
}
