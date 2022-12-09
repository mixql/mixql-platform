import java.sql.*

object SQLightJDBC {

  import com.typesafe.config.{Config, ConfigFactory}

  val config = ConfigFactory.load()
  var c: Connection = null
}

class SQLightJDBC(identity: String) extends java.lang.AutoCloseable:

  def init() = {
    import SQLightJDBC.config
    val url = try {
      config.getString("mixql.org.engine.sqlight.db.path")
    } catch {
      case e: Exception => println(s"Module $identity: Warning: could not read db path from config: " + e.getMessage)
        println(s"Module $identity: use in memory db")
        "jdbc:sqlite::memory:"
    }
    SQLightJDBC.c = DriverManager.getConnection(url)
    println(s"Module $identity: opened database successfully")
  }


  def execute(stmt: String): Option[ResultSet] = {
    if SQLightJDBC.c == null then
      init()

    var jdbcStmt: Statement = null

    try {
      jdbcStmt = SQLightJDBC.c.createStatement()
      val flag = jdbcStmt.execute(stmt)
      if flag then
        val res = jdbcStmt.getResultSet
        while (res.next()){
          res.getObject(0)
          val a = res.getArray(1)


        }
      else None
    } finally {
      if jdbcStmt != null then
        jdbcStmt.close()
    }
  }

  override def close(): Unit =
    println(s"Module $identity: executing close")
    if SQLightJDBC.c != null then
      try
        SQLightJDBC.c.close()
      catch
        case e: Throwable => println(s"Warning: Module $identity: error while closing sql light connection: " +
          e.getMessage)


