package org.mixql.engine.sqlite.local

import java.sql.*
import org.mixql.core.context.gtype
import scala.util.Try

object SQLightJDBC {

  import com.typesafe.config.{Config, ConfigFactory}

  val config = ConfigFactory.load()
  var c: Connection = null
}

class SQLightJDBC(identity: String, dbPathParameter: Option[String] = None) extends java.lang.AutoCloseable :

  def init() = {
    import SQLightJDBC.config
    def getStringParam(name: String): String = {
      val r = config.getString(name)
      println(
        s"[ENGINE $identity] : Got db path from config's param: " + name
      )
      r
    }

    val url: String =
      Try {
        getStringParam(dbPathParameter.get.trim)
      }.getOrElse(
        Try {
          getStringParam("mixql.org.engine.sqlight.db.path")
        }.getOrElse({
          val path = "jdbc:sqlite::memory:"
          println(s"[ENGINE $identity] : use in memory db")
          path
        })
      )

    SQLightJDBC.c = DriverManager.getConnection(url)
    println(s"[ENGINE $identity] : opened database successfully")
  }

  // returns clientMsgs.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): gtype.Type = {
    if SQLightJDBC.c == null then init()

    var jdbcStmt: Statement = null

    try {
      jdbcStmt = SQLightJDBC.c.createStatement()
      val flag = jdbcStmt.execute(stmt)
      if flag then
        // some result was returned
        var res: ResultSet = null
        try {
          res = jdbcStmt.getResultSet
          // init iterator
          var remainedRows = res.next()

          val resultSetMetaData = res.getMetaData
          val columnCount = resultSetMetaData.getColumnCount
          val columnTypes: Seq[gtype.Type] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.local.JavaSqlArrayConverter

          var arr: Seq[gtype.array] = Seq()
          while remainedRows
          do // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ gtype.array(rowValues.toArray)
            remainedRows = res.next()
          end while
          gtype.array(arr.toArray)
        } finally {
          if (res != null) res.close()
        }
      else gtype.Null
    } catch {
      case e: Throwable =>
        throw new Exception(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " + e.getMessage
        )
    } finally {
      if jdbcStmt != null then jdbcStmt.close()
    }
  }

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[gtype.Type]
                         ): Seq[gtype.Type] =
    for (i <- 1 to columnCount - 1) yield {
      columnTypes(i) match
        case gtype.string(_, _) =>
          gtype.string(res.getString(i), "")
        case gtype.bool(_) =>
          gtype.bool(res.getBoolean(i))
        case gtype.int(_) =>
          gtype.int(res.getInt(i))
        case gtype.double(_) =>
          gtype.double(res.getDouble(i))
        case gtype.array(_) =>
          readArrayFromResultSet(res.getArray(i))
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): gtype.array = {

    gtype.array({
      //      val javaSqlArray = res.getArray(i)
      javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
        case gtype.string(_, _) =>
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str => gtype.string(str, "")
            }
            .toArray
        case gtype.bool(_) =>
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map { value => gtype.bool(value)
            }
            .toArray
        case gtype.int(_) =>
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map { value => gtype.int(value)
            }
            .toArray
        case gtype.double(_) =>
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map { value => gtype.double(value)
            }
            .toArray
        case _: Any =>
          throw Exception(
            s"[ENGINE $identity] : SQLightJDBC error while execute: unknown type of array"
          )
    })
  }

  def javaSqlTypeToClientMsg(intType: Int): gtype.Type =
    intType match
      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR =>
        gtype.string("")
      case Types.BIT | Types.BOOLEAN => gtype.bool(false)
      case Types.NUMERIC =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        gtype.string("")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        gtype.int(-1)
      case Types.BIGINT =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        gtype.string("")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => gtype.double(0.0)
      case Types.VARBINARY | Types.BINARY =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        gtype.string("")
      case Types.DATE =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        gtype.string("")
      case Types.TIMESTAMP =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        gtype.string("")
      case Types.CLOB =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        gtype.string("")
      case Types.BLOB =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        gtype.string("")
      case Types.ARRAY => gtype.array(Seq().toArray)
      case Types.STRUCT =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        gtype.string("")
      case Types.REF =>
        println(
          s"[ENGINE $identity] : SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        gtype.string("")

  def getColumnTypes(
                      resultSetMetaData: ResultSetMetaData,
                      columnCount: Int
                    ): Seq[gtype.Type] = {
    (for (i <- 1 to columnCount) yield resultSetMetaData.getColumnType(i)).map {
      intType => javaSqlTypeToClientMsg(intType)
    }
  }

  override def close(): Unit =
    println(s"[ENGINE $identity] : executing close")
    if SQLightJDBC.c != null then
      try SQLightJDBC.c.close()
      catch
        case e: Throwable =>
          println(
            s"Warning: [ENGINE $identity] : error while closing sql light connection: " +
              e.getMessage
          )
