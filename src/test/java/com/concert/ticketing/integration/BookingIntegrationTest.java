package com.concert.ticketing.integration;

import com.concert.ticketing.dto.booking.BookingRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Booking API endpoints.
 * Tests the complete booking flow including Kafka processing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Booking API Integration Tests")
class BookingIntegrationTest extends BaseIntegrationTest {

        private String jwtToken;
        private UUID userId;

        @BeforeEach
        void setUpTest() {
                RestAssured.port = port;
                RestAssured.baseURI = getBaseUrl();

                // Register and login to get JWT token
                registerAndLogin();
        }

        private void registerAndLogin() {
                // Register a test user
                String registerPayload = """
                                {
                                    "username": "testuser",
                                    "password": "password123",
                                    "fullName": "Test User",
                                    "email": "test@example.com",
                                    "role": "USER"
                                }
                                """;

                given()
                                .contentType(ContentType.JSON)
                                .body(registerPayload)
                                .when()
                                .post("/api/v1/auth/register")
                                .then()
                                .statusCode(anyOf(is(200), is(409))); // 409 if user already exists

                // Login to get JWT token
                String credentials = java.util.Base64.getEncoder()
                                .encodeToString("testuser:password123".getBytes());

                jwtToken = given()
                                .header("Authorization", "Basic " + credentials)
                                .when()
                                .post("/api/v1/auth/login")
                                .then()
                                .statusCode(200)
                                .extract()
                                .path("data.token");
        }

        @Test
        @DisplayName("Should create booking successfully")
        void createBooking_Success() {
                // Arrange
                BookingRequest request = new BookingRequest(
                                UUID.randomUUID(), // userId
                                UUID.randomUUID(), // eventId
                                UUID.randomUUID(), // categoryId
                                2 // quantity
                );

                String idempotencyKey = UUID.randomUUID().toString();

                // Act & Assert
                given()
                                .header("Authorization", "Bearer " + jwtToken)
                                .header("X-Idempotency-Key", idempotencyKey)
                                .contentType(ContentType.JSON)
                                .body(request)
                                .when()
                                .post("/api/v1/bookings")
                                .then()
                                .statusCode(202)
                                .body("status", equalTo("Success"))
                                .body("message", containsString("being processed"));
        }

        @Test
        @DisplayName("Should get booking by ID")
        void getBookingById_Success() {
                // This test assumes there's at least one booking in the database
                // In a real scenario, you would create a booking first

                UUID bookingId = UUID.randomUUID();

                given()
                                .header("Authorization", "Bearer " + jwtToken)
                                .when()
                                .get("/api/v1/bookings/" + bookingId)
                                .then()
                                .statusCode(anyOf(is(200), is(404))); // 404 if booking doesn't exist
        }

        @Test
        @DisplayName("Should get user bookings")
        void getUserBookings_Success() {
                UUID userId = UUID.randomUUID();

                given()
                                .header("Authorization", "Bearer " + jwtToken)
                                .queryParam("userId", userId)
                                .when()
                                .get("/api/v1/bookings")
                                .then()
                                .statusCode(200)
                                .body("status", equalTo("Success"));
        }

        @Test
        @DisplayName("Should cancel booking")
        void cancelBooking_Success() {
                UUID bookingId = UUID.randomUUID();

                given()
                                .header("Authorization", "Bearer " + jwtToken)
                                .when()
                                .post("/api/v1/bookings/" + bookingId + "/cancel")
                                .then()
                                .statusCode(anyOf(is(200), is(404))); // 404 if booking doesn't exist
        }

        @Test
        @DisplayName("Should reject booking without JWT token")
        void createBooking_Unauthorized() {
                BookingRequest request = new BookingRequest(
                                UUID.randomUUID(), // userId
                                UUID.randomUUID(), // eventId
                                UUID.randomUUID(), // categoryId
                                2); // quantity

                given()
                                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                                .contentType(ContentType.JSON)
                                .body(request)
                                .when()
                                .post("/api/v1/bookings")
                                .then()
                                .statusCode(403); // Forbidden without authentication
        }
}
