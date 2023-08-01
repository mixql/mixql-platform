package org.mixql.engine.sqlite.local

import java.sql.*
import org.mixql.core.context.{EngineContext, gtype}
import org.mixql.engine.local.logger.IEngineLogger

import scala.collection.mutable
import scala.util.Try

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String, ctx: EngineContext,
                  dbPathParameter: Option[String] = None) extends java.lang.AutoCloseable
  with IEngineLogger :

  override def name: String = identity

  def init() = {
    def getStringParam(name: String): String = {
      val r = ctx.getVar(name).asInstanceOf[gtype.string]
      logInfo(s"Got db path from provided params: " + name)
      r.toString
    }

    val url: String =
      Try {
        getStringParam(dbPathParameter.get.trim)
      }.getOrElse(
        Try {
          getStringParam("mixql.org.engine.sqlight.db.path")
        }.getOrElse({
          val path = "jdbc:sqlite::memory:"
          logInfo(s"use in memory db")
          path
        })
      )

    SQLightJDBC.c = DriverManager.getConnection(url)
    logInfo(s"opened database successfully")
  }

  // returns messages.Type
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
          new gtype.array(arr.toArray)
        } finally {
          if (res != null) res.close()
        }
      else new gtype.Null()
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
    for (i <- 1 to columnCount) yield {
      columnTypes(i - 1) match
        case _: gtype.string =>
          gtype.string(res.getString(i), "")
        case _: gtype.bool =>
          gtype.bool(res.getBoolean(i))
        case _: gtype.gInt =>
          gtype.gInt(res.getInt(i))
        case _: gtype.gDouble =>
          gtype.gDouble(res.getDouble(i))
        case _: gtype.array =>
          readArrayFromResultSet(res.getArray(i))
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): gtype.array = {

    gtype.array({
      //      val javaSqlArray = res.getArray(i)
      javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
        case _: gtype.string =>
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str => gtype.string(str, "")
            }
            .toArray
        case _: gtype.bool =>
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map { value => gtype.bool(value)
            }
            .toArray
        case _: gtype.gInt =>
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map { value => gtype.gInt(value)
            }
            .toArray
        case _: gtype.gDouble =>
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map { value => gtype.gDouble(value)
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
        logWarn(s"SQLightJDBC error while execute: unsupported column type NUMERIC")
        gtype.string("")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        gtype.gInt(-1)
      case Types.BIGINT =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type BIGINT")
        gtype.string("")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => gtype.gDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type VARBINARY or BINARY")
        gtype.string("")
      case Types.DATE =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type Date")
        gtype.string("")
      case Types.TIMESTAMP =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type TIMESTAMP")
        gtype.string("")
      case Types.CLOB =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type CLOB")
        gtype.string("")
      case Types.BLOB =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type BLOB")
        gtype.string("")
      case Types.ARRAY => gtype.array(Seq().toArray)
      case Types.STRUCT =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type STRUCT")
        gtype.string("")
      case Types.REF =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type REF")
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
    logDebug(s"[ENGINE $identity] : executing close")
    if SQLightJDBC.c != null then
      try SQLightJDBC.c.close()
      catch
        case e: Throwable =>
          logDebug(
            s"Warning: [ENGINE $identity] : error while closing sql light connection: " +
              e.getMessage
          )
