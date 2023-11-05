package org.mixql.engine.sqlite

import org.mixql.core.context.mtype

import java.sql.*
import scala.collection.mutable
import org.mixql.engine.core.logger.ModuleLogger
import org.mixql.remote.{GtypeConverter, RemoteMessageConverter, messages}
import org.mixql.remote.messages.module
import org.mixql.engine.core.PlatformContext
import org.mixql.remote.messages.rtype.Error
import org.mixql.remote.messages.rtype.mtype.{MArray, MBool, MDouble, MInt, MNULL, MString}

class SQLightJDBC(identity: String, platformCtx: PlatformContext) extends java.lang.AutoCloseable {

  val logger = new ModuleLogger(identity)

  import logger._
  var c: Connection = null

  def init() = {
    val url =
      try {
        platformCtx.getVar("mixql.org.engine.sqlight.db.path").asInstanceOf[mtype.MString].getValue
      } catch {
        case e: Exception =>
          logWarn(s"Warning: could not read db path from provided params: " + e.getMessage)
          logInfo(s"use in memory db")
          "jdbc:sqlite::memory:"
      }
    c = DriverManager.getConnection(url)
    logInfo(s"opened database successfully")
  }

  def getSQLightJDBCConnection: Connection = {
    this.synchronized {
      if (c == null)
        init()
    }
    c
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

          var arr: Seq[MArray] = Seq()
          while (remainedRows) {
            // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ MArray(rowValues.toArray)
            remainedRows = res.next()
          }
          MArray(arr.toArray)
        } finally {
          if (res != null)
            res.close()
        }
      } else
        MNULL()
    } catch {
      case e: Throwable =>
        org.mixql.remote.messages.rtype.Error(s"Module $identity: SQLightJDBC error while execute: " + e.getMessage)
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
          case _: MString => MString(res.getString(i), "")
          case _: MBool   => MBool(res.getBoolean(i))
          case _: MInt    => MInt(res.getInt(i))
          case _: MDouble => MDouble(res.getDouble(i))
          case _: MArray  => readArrayFromResultSet(res.getArray(i))
        }
      }
  }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): MArray = {

    javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match {
      case _: MString =>
        MArray(JavaSqlArrayConverter.toStringArray(javaSqlArray).map { str =>
          MString(str, "")
        }.toArray)
      case _: MBool =>
        MArray(JavaSqlArrayConverter.toBooleanArray(javaSqlArray).map { value =>
          MBool(value)
        }.toArray)
      case _: MInt =>
        MArray(JavaSqlArrayConverter.toIntArray(javaSqlArray).map { value =>
          new MInt(value)
        }.toArray)
      case _: MDouble =>
        MArray(JavaSqlArrayConverter.toDoubleArray(javaSqlArray).map { value =>
          new MDouble(value)
        }.toArray)
      case _: Any => throw new Exception(s"Module $identity: SQLightJDBC error while execute: unknown type of array")
    }
  }

  def javaSqlTypeToClientMsg(intType: Int): messages.Message = {

    intType match {

      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR => MString("", "")
      case Types.BIT | Types.BOOLEAN                      => MBool(false)
      case Types.NUMERIC =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        MString("", "")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER => MInt(-1)
      case Types.BIGINT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        MString("", "")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => MDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        MString("", "")
      case Types.DATE =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        MString("", "")
      case Types.TIMESTAMP =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        MString("", "")
      case Types.CLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        MString("", "")
      case Types.BLOB =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        MString("", "")
      case Types.ARRAY => MArray(Seq().toArray)
      case Types.STRUCT =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        MString("", "")
      case Types.REF =>
        logError(
          s"SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        MString("", "")
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

    if (c != null) {
      try c.close()
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
