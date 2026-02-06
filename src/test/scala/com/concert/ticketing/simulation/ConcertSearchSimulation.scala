package com.concert.ticketing.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Gatling load test simulation for concert search.
 * Simulates concurrent users searching for concerts.
 */
class ConcertSearchSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  // Scenario: Search concerts
  val searchConcerts = scenario("Search Concerts")
    .exec(http("Get All Concerts")
      .get("/api/v1/concerts")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Search by Artist")
      .get("/api/v1/concerts")
      .queryParam("artist", "Rock")
      .check(status.is(200)))
    .pause(1)
    .exec(http("Search by Venue")
      .get("/api/v1/concerts")
      .queryParam("venue", "Jakarta")
      .check(status.is(200)))

  // Load test configuration
  setUp(
    searchConcerts.inject(
      rampUsers(20) during (15.seconds), // Ramp up to 20 users over 15 seconds
      constantUsersPerSec(10) during (60.seconds) // Maintain 10 users per second for 60 seconds
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(3000), // Max response time < 3 seconds
      global.successfulRequests.percent.gt(99) // 99% success rate
    )
}
