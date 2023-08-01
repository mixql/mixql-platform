package org.mixql.engine.sqlite

import org.mixql.core.context.gtype.string
import org.mixql.engine.core.PlatformContext
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.messages.gtype.{Bool, NULL, gArray, gDouble, gInt, gString}
import org.mixql.remote.messages.{Message, module}

import java.sql._
import scala.collection.mutable

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String,
                  platformCtx: PlatformContext)
  extends java.lang.AutoCloseable {

  val logger = new ModuleLogger(identity)

  import logger._

  def init() = {
    val url =
      try {
        platformCtx.getVar("mixql.org.engine.sqlight.db.path").asInstanceOf[string].getValue
      } catch {
        case e: Exception =>
          logWarn(
            s"Warning: could not read db path from provided params: " + e.getMessage
          )
          logInfo(s"use in memory db")
          "jdbc:sqlite::memory:"
      }
    SQLightJDBC.c = DriverManager.getConnection(url)
    logInfo(s"opened database successfully")
  }

  def getSQLightJDBCConnection: Connection = {
    if (SQLightJDBC.c == null) init()
    SQLightJDBC.c
  }


  // returns messages.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): Message = {
    var jdbcStmt: Statement = null

    try {
      jdbcStmt = getSQLightJDBCConnection.createStatement()
      val flag = jdbcStmt.execute(stmt)
      if (flag) {
        // some result was returned
        var res: ResultSet = null
        try {
          res = jdbcStmt.getResultSet
          // init iterator
          var remainedRows = res.next()

          val resultSetMetaData = res.getMetaData
          val columnCount = resultSetMetaData.getColumnCount
          val columnTypes: Seq[Message] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arr: Seq[gArray] = Seq()
          while (remainedRows) {
            // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ new gArray(rowValues.toArray)
            remainedRows = res.next()
          }
          new gArray(arr.toArray)
        } finally {
          if (res != null) res.close()
        }
      }
      else new NULL()
    } catch {
      case e: Throwable =>
        new module.Error(
          s"Module $identity: SQLightJDBC error while execute: " + e.getMessage
        )
    } finally {
      if (jdbcStmt != null) jdbcStmt.close()
    }
  }

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[Message]
                         ): Seq[Message] =

    for (i <- 1 to columnCount) yield {
      columnTypes(i - 1) match {
        case _: gString =>
          new gString(res.getString(i), "")
        case _: Bool =>
          new Bool(res.getBoolean(i))
        case _: gInt =>
          new gInt(res.getInt(i))
        case _: gDouble =>
          new gDouble(res.getDouble(i))
        case _: gArray =>
          readArrayFromResultSet(res.getArray(i))
      }
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): gArray = {

    javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match {
      case _: gString =>
        new gArray(
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str =>
              new gString(str, "")
            }
            .toArray
        )
      case _: Bool =>
        new gArray(
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map {
              value => new Bool(value)
            }.toArray
        )
      case _: gInt =>
        new gArray(
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map {
              value => new gInt(value)
            }.toArray
        )
      case _: gDouble =>
        new gArray(
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map {
              value => new gDouble(value)
            }.toArray
        )
      case _: Any =>
        throw new Exception(
          s"Module $identity: SQLightJDBC error while execute: unknown type of array"
        )
    }
  }

  def javaSqlTypeToClientMsg(intType: Int): Message = {

    intType match {

      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR =>
        new gString("", "")
      case Types.BIT | Types.BOOLEAN => new Bool(false)
      case Types.NUMERIC =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        new gString("", "")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        new gInt(-1)
      case Types.BIGINT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        new gString("", "")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => new gDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        new gString("", "")
      case Types.DATE =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        new gString("", "")
      case Types.TIMESTAMP =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        new gString("", "")
      case Types.CLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        new gString("", "")
      case Types.BLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        new gString("", "")
      case Types.ARRAY => new gArray(Seq().toArray)
      case Types.STRUCT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        new gString("", "")
      case Types.REF =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        new gString("", "")
    }
  }

  def getColumnTypes(
                      resultSetMetaData: ResultSetMetaData,
                      columnCount: Int
                    ): Seq[Message] = {
    (for (i <- 1 to columnCount) yield resultSetMetaData.getColumnType(i)).map {
      intType => javaSqlTypeToClientMsg(intType)
    }
  }

  override def close(): Unit = {
    logInfo(s"executing close")

    if (SQLightJDBC.c != null) {
      try SQLightJDBC.c.close()
      catch {
        case e: Throwable =>
          logWarn(
            s"Warning: error while closing sql light connection: " +
              e.getMessage
          )
      }
    }
  }
}