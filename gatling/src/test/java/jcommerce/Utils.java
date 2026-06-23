package jcommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;import java.nio.charset.StandardCharsets;

public class Utils {
    private final static String IDENTITY_URL = "http://localhost:8080";

    public static String getAdminJWT(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IDENTITY_URL + "/api/v1/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString(getAdminLogin()))
                .header("Content-Type", "application/json")
                .build();

        var client = HttpClient.newHttpClient();

        HttpResponse<String> response;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving JWT");
        }

        assert(response.statusCode() == 200);

        return retrieveAccessToken(response.body());
    }

    private static String getAdminLogin(){
        InputStream inputStream = Utils.class
                .getResourceAsStream("/bodies/login-admin.json");

        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Error while retrieving login admin.json");
            return "";
        }
    }

    private static String retrieveAccessToken(String body){
        var objectMapper = new ObjectMapper();

        JsonNode jsonNode;

        try {
            jsonNode = objectMapper.readTree(body);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while parsing access token");
        }

        return jsonNode.path("data").path("accessToken").path("token").asText();
    }
}
