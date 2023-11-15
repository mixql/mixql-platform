package org.mixql.engine.sqlite.local

import java.sql.*
import org.mixql.core.context.{EngineContext, mtype}
import org.mixql.engine.local.logger.IEngineLogger

import scala.collection.mutable
import scala.util.Try

class SQLightJDBC(identity: String, ctx: EngineContext, dbPathParameter: Option[String] = None)
    extends java.lang.AutoCloseable
    with IEngineLogger:
  var c: Connection = null

  override def name: String = identity

  def init() = {
    def getStringParam(name: String): String = {
      val r = ctx.getVar(name).asInstanceOf[mtype.MString]
      logInfo(s"Got db path from provided params: " + name)
      r.toString
    }

    val url: String = Try {
      getStringParam(dbPathParameter.get.trim)
    }.getOrElse(Try {
      if (dbPathParameter.isDefined)
        logWarn(s"could not read dbPathParameter: " + dbPathParameter.get)
      getStringParam("mixql.org.engine.sqlight.db.path")
    }.getOrElse({
      logWarn(s"could not read string parameter mixql.org.engine.sqlight.db.path. Use in memory db")
      val path = "jdbc:sqlite::memory:"
      path
    }))

    c = DriverManager.getConnection(url)
    logInfo(s"opened database successfully")
  }

  // returns messages.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): mtype.MType = {
    this.synchronized {
      if c == null then init()
    }

    var jdbcStmt: Statement = null

    try {
      jdbcStmt = c.createStatement()
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
          val columnTypes: Seq[mtype.MType] = getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount)
              yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.local.JavaSqlArrayConverter

          var arr: Seq[mtype.MArray] = Seq()
          while remainedRows
          do // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ mtype.MArray(rowValues.toArray)
            remainedRows = res.next()
          end while
          new mtype.MArray(arr.toArray)
        } finally {
          if (res != null)
            res.close()
        }
      else mtype.MNull.get()
    } catch {
      case e: Throwable => throw new Exception(s"[ENGINE $identity] : SQLightJDBC error while execute: " + e.getMessage)
    } finally {
      if jdbcStmt != null then jdbcStmt.close()
    }
  }

  def getRowFromResultSet(res: ResultSet, columnCount: Int, columnTypes: Seq[mtype.MType]): Seq[mtype.MType] =
    for (i <- 1 to columnCount)
      yield {
        columnTypes(i - 1) match
          case _: mtype.MString => mtype.MString(res.getString(i), "")
          case _: mtype.MBool   => mtype.MBool.get(res.getBoolean(i))
          case _: mtype.MInt    => mtype.MInt(res.getInt(i))
          case _: mtype.MDouble => mtype.MDouble(res.getDouble(i))
          case _: mtype.MArray  => readArrayFromResultSet(res.getArray(i))
      }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): mtype.MArray = {

    mtype.MArray({
      //      val javaSqlArray = res.getArray(i)
      javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
        case _: mtype.MString =>
          JavaSqlArrayConverter.toStringArray(javaSqlArray).map { str => mtype.MString(str, "") }.toArray
        case _: mtype.MBool =>
          JavaSqlArrayConverter.toBooleanArray(javaSqlArray).map { value => mtype.MBool.get(value) }.toArray
        case _: mtype.MInt => JavaSqlArrayConverter.toIntArray(javaSqlArray).map { value => mtype.MInt(value) }.toArray
        case _: mtype.MDouble =>
          JavaSqlArrayConverter.toDoubleArray(javaSqlArray).map { value => mtype.MDouble(value) }.toArray
        case _: Any => throw Exception(s"[ENGINE $identity] : SQLightJDBC error while execute: unknown type of array")
    })
  }

  def javaSqlTypeToClientMsg(intType: Int): mtype.MType =
    intType match
      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR => mtype.MString("")
      case Types.BIT | Types.BOOLEAN                      => mtype.MBool.get(false)
      case Types.NUMERIC =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type NUMERIC")
        mtype.MString("")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER => mtype.MInt(-1)
      case Types.BIGINT =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type BIGINT")
        mtype.MString("")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => mtype.MDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type VARBINARY or BINARY")
        mtype.MString("")
      case Types.DATE =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type Date")
        mtype.MString("")
      case Types.TIMESTAMP =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type TIMESTAMP")
        mtype.MString("")
      case Types.CLOB =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type CLOB")
        mtype.MString("")
      case Types.BLOB =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type BLOB")
        mtype.MString("")
      case Types.ARRAY => mtype.MArray(Seq().toArray)
      case Types.STRUCT =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type STRUCT")
        mtype.MString("")
      case Types.REF =>
        logWarn(s"SQLightJDBC error while execute: unsupported column type REF")
        mtype.MString("")

  def getColumnTypes(resultSetMetaData: ResultSetMetaData, columnCount: Int): Seq[mtype.MType] = {
    (for (i <- 1 to columnCount)
      yield resultSetMetaData.getColumnType(i)).map { intType =>
      javaSqlTypeToClientMsg(intType)
    }
  }

  override def close(): Unit =
    logDebug(s"[ENGINE $identity] : executing close")
    if c != null then
      try c.close()
      catch
        case e: Throwable =>
          logDebug(
            s"Warning: [ENGINE $identity] : error while closing sql light connection: " +
              e.getMessage
          )
