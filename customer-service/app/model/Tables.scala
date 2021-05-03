package model
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Customers.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Customers
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar), Length(50,true) */
  case class CustomersRow(id: Int, name: String)
  /** GetResult implicit for fetching CustomersRow objects using plain SQL queries */
  implicit def GetResultCustomersRow(implicit e0: GR[Int], e1: GR[String]): GR[CustomersRow] = GR{
    prs => import prs._
    CustomersRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table customers. Objects of this class serve as prototypes for rows in queries. */
  class Customers(_tableTag: Tag) extends profile.api.Table[CustomersRow](_tableTag, Some("customer-service"), "customers") {
    def * = (id, name) <> (CustomersRow.tupled, CustomersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name))).shaped.<>({r=>import r._; _1.map(_=> CustomersRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar), Length(50,true) */
    val name: Rep[String] = column[String]("name", O.Length(50,varying=true))
  }
  /** Collection-like TableQuery object for table Customers */
  lazy val Customers = new TableQuery(tag => new Customers(tag))
}
