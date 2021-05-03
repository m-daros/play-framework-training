package slick.codegen

import scala.reflect.io.File

object SlickCodeGen extends App {

  private val appPath      = File ( "app" ).toAbsolute.path
  private val modelPackage = "model"
  private val profile      = "slick.jdbc.PostgresProfile"
  private val driver       = "org.postgresql.Driver"
  private val dbUser       = "customer-service"
  private val jdbcUrl      = "jdbc:postgresql://localhost/postgres"
  private val dbPassword   = "password"

  println ( s"Generating slick model on folder ${appPath}/${modelPackage}" )

  slick.codegen.SourceCodeGenerator.main (

    Array ( profile, driver, jdbcUrl, appPath, modelPackage, dbUser, dbPassword )
  )
}