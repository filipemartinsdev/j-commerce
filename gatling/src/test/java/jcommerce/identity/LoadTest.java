package jcommerce.identity;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

public class LoadTest extends Simulation {
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
                constantUsersPerSec(5).during(Duration.ofMinutes(15))
        )).protocols(httpProtocol);
    }
}
