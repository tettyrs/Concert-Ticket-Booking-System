package com.concert.ticketing.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Gatling load test simulation for booking flow.
 * Simulates concurrent users creating bookings.
 */
class BookingLoadSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Scenario: Create booking
  val createBooking = scenario("Create Booking")
    .exec(http("Login")
      .post("/api/v1/auth/login")
      .header("Authorization", "Basic dGVzdHVzZXI6cGFzc3dvcmQxMjM=") // testuser:password123
      .check(jsonPath("$.data.token").saveAs("jwtToken")))
    .pause(1)
    .exec(http("Create Booking")
      .post("/api/v1/bookings")
      .header("Authorization", "Bearer ${jwtToken}")
      .header("X-Idempotency-Key", session => java.util.UUID.randomUUID().toString)
      .body(StringBody("""{
        "userId": "${java.util.UUID.randomUUID()}",
        "categoryId": "${java.util.UUID.randomUUID()}",
        "quantity": 2
      }""")).asJson
      .check(status.is(202)))

  // Load test configuration
  setUp(
    createBooking.inject(
      rampUsers(10) during (10.seconds), // Ramp up to 10 users over 10 seconds
      constantUsersPerSec(5) during (30.seconds) // Maintain 5 users per second for 30 seconds
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(5000), // Max response time < 5 seconds
      global.successfulRequests.percent.gt(95) // 95% success rate
    )
}
