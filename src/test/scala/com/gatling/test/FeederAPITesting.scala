package com.gatling.test

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.ChainBuilder

class FeederAPITesting extends Simulation{

  // protocol
  val httpProtocol = http.baseUrl("https://reqres.in/api")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")


  def USERCOUNT : Int = System.getProperty("USERS","5").toInt

  def RAMPUPDUR : Int = System.getProperty("RAMPUP_DUR","10").toInt

  def TESTDUR : Int = System.getProperty("TEST_DUR","30").toInt


before{
  println(s"Running test with ${USERCOUNT} users")
  println(s"Ramping test with ${RAMPUPDUR} seconds")
  println(s"Total test  duration ${TESTDUR} seconds")
}

val csvfeeder = csv("data/data.csv").random


// HTTP Call's

def getAllUsers(): ChainBuilder = {
exec(
   http("Get all Users")
     .get("/users")
     .check(status is 200)
  )
}

def createNewUser() : ChainBuilder = {
  feed(csvfeeder)
    .exec(http("Create a new User - #{email}")
      .post("/users")
      .body(ElFileBody("bodies/newUserTemplate.json")).asJson
    )

}

def getSingleUser() : ChainBuilder ={
  exec(http("get single user - #{id} #{firstName}")
  .get("/users/#{id}")
    .check(jsonPath(path="$.data.first_name").is(expected = "#{firstName}"))
  )

}
  //scenario

  val scn = scenario("all Dummy users and create users final script")
    .forever {
      exec(getAllUsers())
        .pause(duration = 2)
        .exec(createNewUser())
        .pause(duration = 2)
        .exec(getSingleUser())

    }

  //setup

  setUp(
    scn.inject(
      nothingFor(5),
      rampUsers(USERCOUNT).during(RAMPUPDUR)
    ).protocols(httpProtocol)

  ).maxDuration(TESTDUR)

  after {
    println("Stress test is completed")
  }
}
