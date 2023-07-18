package org.mixql.engine.sqlite

import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter, messages}

import java.sql._
import scala.collection.mutable

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String,
                  engineParams: mutable.Map[String, messages.Message] = mutable.Map())
  extends java.lang.AutoCloseable {

  val logger = new ModuleLogger(identity)

  import logger._

  def init() = {
    val url =
      try {
        engineParams("mixql.org.engine.sqlight.db.path")
          .asInstanceOf[messages.gString].value
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
          val columnTypes: Seq[messages.Message] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arr: Seq[messages.gArray] = Seq()
          while (remainedRows) {
            // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ new messages.gArray(rowValues.toArray)
            remainedRows = res.next()
          }
          new messages.gArray(arr.toArray)
        } finally {
          if (res != null) res.close()
        }
      }
      else new messages.NULL()
    } catch {
      case e: Throwable =>
        new messages.Error(
          s"Module $identity: SQLightJDBC error while execute: " + e.getMessage
        )
    } finally {
      if (jdbcStmt != null) jdbcStmt.close()
    }
  }

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[messages.Message]
                         ): Seq[messages.Message] =

    for (i <- 1 to columnCount) yield {
      columnTypes(i - 1) match {
        case _: messages.gString =>
          new messages.gString(res.getString(i), "")
        case _: messages.Bool =>
          new messages.Bool(res.getBoolean(i))
        case _: messages.gInt =>
          new messages.gInt(res.getInt(i))
        case _: messages.gDouble =>
          new messages.gDouble(res.getDouble(i))
        case _: messages.gArray =>
          readArrayFromResultSet(res.getArray(i))
      }
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): messages.gArray = {

    javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match {
      case _: messages.gString =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str =>
              new messages.gString(str, "")
            }
            .toArray
        )
      case _: messages.Bool =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map {
              value => new messages.Bool(value)
            }.toArray
        )
      case _: messages.gInt =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map {
              value => new messages.gInt(value)
            }.toArray
        )
      case _: messages.gDouble =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map {
              value => new messages.gDouble(value)
            }.toArray
        )
      case _: Any =>
        throw new Exception(
          s"Module $identity: SQLightJDBC error while execute: unknown type of array"
        )
    }
  }

  def javaSqlTypeToClientMsg(intType: Int): messages.Message = {

    intType match {

      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR =>
        new messages.gString("", "")
      case Types.BIT | Types.BOOLEAN => new messages.Bool(false)
      case Types.NUMERIC =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        new messages.gString("", "")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        new messages.gInt(-1)
      case Types.BIGINT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        new messages.gString("", "")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => new messages.gDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        new messages.gString("", "")
      case Types.DATE =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        new messages.gString("", "")
      case Types.TIMESTAMP =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        new messages.gString("", "")
      case Types.CLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        new messages.gString("", "")
      case Types.BLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        new messages.gString("", "")
      case Types.ARRAY => new messages.gArray(Seq().toArray)
      case Types.STRUCT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        new messages.gString("", "")
      case Types.REF =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        new messages.gString("", "")
    }
  }

  def getColumnTypes(
                      resultSetMetaData: ResultSetMetaData,
                      columnCount: Int
                    ): Seq[messages.Message] = {
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