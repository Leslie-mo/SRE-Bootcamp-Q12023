package com.wizeline;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

public class AppTest {

    private static final String BASE_URL = "http://localhost:8000";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static final String INVALID_PASSWORD = "wrongpass";

    @Before
    public void setUp() {
        App.main(new String[0]);
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testRootEndpoint() {
        given()
                .when().get("/")
                .then()
                .statusCode(200)
                .body(equalTo("OK"));
    }

    @Test
    public void testHealthEndpoint() {
        given()
                .when().get("/_health")
                .then()
                .statusCode(200)
                .body(equalTo("OK"));
    }

    @Test
    public void testLoginEndpointSuccess() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("username", USERNAME)
                .queryParam("password", PASSWORD)
                .when().post("/login")
                .then()
                .statusCode(200)
                .body("token", containsString("."))
                .body("token", containsString("."))
                .body("token.length()", equalTo(138));
    }

    @Test
    public void testLoginEndpointFailure() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("username", USERNAME)
                .queryParam("password", INVALID_PASSWORD)
                .when().post("/login")
                .then()
                .statusCode(403)
                .body("message", equalTo("Invalid username or password"));
    }

    @Test
    public void testProtectedEndpointSuccess() {
        String token = given()
                .contentType(ContentType.JSON)
                .queryParam("username", USERNAME)
                .queryParam("password", PASSWORD)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when().get("/protected")
                .then()
                .statusCode(200)
                .body("message", equalTo("You are under protected data, " + USERNAME + " (user)"));
    }

    @Test
    public void testProtectedEndpointUnauthorized() {
        given()
                .when().get("/protected")
                .then()
                .statusCode(401)
                .body("message", equalTo("Authorization header not found"));
    }

    @Test
    public void testProtectedEndpointInvalidToken() {
        given()
                .header("Authorization", "Bearer invalid.token")
                .when().get("/protected")
                .then()
                .statusCode(401)
                .body("message", equalTo("Invalid token"));
    }

}
