package com.gatling.test

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SampleAPITest extends Simulation{

  //protocol

  val httpProtocol = http.baseUrl("https://reqres.in/api")
    .acceptHeader("application/json")

  //scenario
val scn = scenario("Get Sample API Demo")
  .exec(
  http("Get page")
    .get("/users/2")
    .check(
      status.is(200)
    )
  ).pause(1)

  //setup
  setUp(
    scn.inject(rampUsers(5).during(5))

    ).protocols(httpProtocol)

}
