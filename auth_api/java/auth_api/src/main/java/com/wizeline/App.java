package com.wizeline;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import static com.wizeline.JsonUtil.json;
import static spark.Spark.*;

import spark.Response;


public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private static final String SECRET = "my2w7wjd7yXF64FIADfJxNs1oupTGAuW";

    public static void main(String[] args) {

        log.info("Listening on: http://localhost:8000/");

        port(8000);
        get("/", App::routeRoot);
        get("/_health", App::routeRoot);
        post("/login", App::urlLogin, json());
        get("/protected", App::protect, json());
    }

    public static Object routeRoot(Request req, Response res) throws Exception {
        return "OK";
    }

    public static Object urlLogin(Request req, Response res) throws Exception {
        String username = req.queryParams("username");
        String password = req.queryParams("password");

        // Authenticate user
        if (authenticate(username, password)) {
            // Generate token
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("role", getRole(username))
                    .signWith(SignatureAlgorithm.HS256, SECRET)
                    .compact();
            return new com.wizeline.Response(token);
        } else {
            res.status(403);
            return new com.wizeline.Response("Invalid username or password");
        }
    }

    public static Object protect(Request req, Response res) throws Exception {
        String authorization = req.headers("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                String username = Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
                String role = (String) Jwts.parser()
                        .setSigningKey(SECRET)
                        .parseClaimsJws(token)
                        .getBody()
                        .get("role");
                return new com.wizeline.Response("You are under protected data, " + username + " (" + role + ")");
            } catch (Exception e) {
                res.status(401);
                return new com.wizeline.Response("Invalid token");
            }
        } else {
            res.status(401);
            return new com.wizeline.Response("Authorization header not found");
        }
    }

    // Fake authentication function, always returns true
    private static boolean authenticate(String username, String password) {
        return true;
    }

    // Dummy function to get the user's role, returns "user"
    private static String getRole(String username) {
        return "user";
    }
}
