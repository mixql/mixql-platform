package org.mixql.engine.sqlite

import org.mixql.core.context.gtype.string

import java.sql.*
import scala.collection.mutable
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.mixql.remote.messages.{gtype, module}
import org.mixql.remote.messages.gtype.{Bool, gArray, gDouble, gInt, gString}
import org.mixql.engine.core.PlatformContext

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String, platformCtx: PlatformContext) extends java.lang.AutoCloseable {

  val logger = new ModuleLogger(identity)

  import logger._

  def init() = {
    val url =
      try {
        platformCtx.getVar("mixql.org.engine.sqlight.db.path").asInstanceOf[string].getValue
      } catch {
        case e: Exception =>
          logWarn(s"Warning: could not read db path from provided params: " + e.getMessage)
          logInfo(s"use in memory db")
          "jdbc:sqlite::memory:"
      }
    SQLightJDBC.c = DriverManager.getConnection(url)
    logInfo(s"opened database successfully")
  }

  def getSQLightJDBCConnection: Connection = {
    if (SQLightJDBC.c == null)
      init()
    SQLightJDBC.c
  }

  // returns messages.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): messages.Message = {
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
          val columnTypes: Seq[messages.Message] = getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount)
              yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arr: Seq[gArray] = Seq()
          while (remainedRows) {
            // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ gtype.gArray(rowValues.toArray)
            remainedRows = res.next()
          }
          gtype.gArray(arr.toArray)
        } finally {
          if (res != null)
            res.close()
        }
      } else
        messages.gtype.NULL()
    } catch {
      case e: Throwable => module.Error(s"Module $identity: SQLightJDBC error while execute: " + e.getMessage)
    } finally {
      if (jdbcStmt != null)
        jdbcStmt.close()
    }
  }

  def getRowFromResultSet(res: ResultSet,
                          columnCount: Int,
                          columnTypes: Seq[messages.Message]): Seq[messages.Message] = {

    for (i <- 1 to columnCount)
      yield {
        columnTypes(i - 1) match {
          case _: gString => gtype.gString(res.getString(i), "")
          case _: Bool    => gtype.Bool(res.getBoolean(i))
          case _: gInt    => gtype.gInt(res.getInt(i))
          case _: gDouble => gDouble(res.getDouble(i))
          case _: gArray  => readArrayFromResultSet(res.getArray(i))
        }
      }
  }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): gArray = {

    javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match {
      case _: gString =>
        gtype.gArray(JavaSqlArrayConverter.toStringArray(javaSqlArray).map { str =>
          gtype.gString(str, "")
        }.toArray)
      case _: Bool =>
        gtype.gArray(JavaSqlArrayConverter.toBooleanArray(javaSqlArray).map { value =>
          gtype.Bool(value)
        }.toArray)
      case _: gInt =>
        gtype.gArray(JavaSqlArrayConverter.toIntArray(javaSqlArray).map { value =>
          new gInt(value)
        }.toArray)
      case _: gDouble =>
        gtype.gArray(JavaSqlArrayConverter.toDoubleArray(javaSqlArray).map { value =>
          new gDouble(value)
        }.toArray)
      case _: Any => throw new Exception(s"Module $identity: SQLightJDBC error while execute: unknown type of array")
    }
  }

  def javaSqlTypeToClientMsg(intType: Int): messages.Message = {

    intType match {

      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR => gtype.gString("", "")
      case Types.BIT | Types.BOOLEAN                      => gtype.Bool(false)
      case Types.NUMERIC =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        gtype.gString("", "")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER => gtype.gInt(-1)
      case Types.BIGINT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        gtype.gString("", "")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => gtype.gDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        gtype.gString("", "")
      case Types.DATE =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        gtype.gString("", "")
      case Types.TIMESTAMP =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        gtype.gString("", "")
      case Types.CLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        gtype.gString("", "")
      case Types.BLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        gtype.gString("", "")
      case Types.ARRAY => gtype.gArray(Seq().toArray)
      case Types.STRUCT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        gtype.gString("", "")
      case Types.REF =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        gtype.gString("", "")
    }
  }

  def getColumnTypes(resultSetMetaData: ResultSetMetaData, columnCount: Int): Seq[messages.Message] = {
    (for (i <- 1 to columnCount)
      yield resultSetMetaData.getColumnType(i)).map { intType =>
      javaSqlTypeToClientMsg(intType)
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
