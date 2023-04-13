package org.mixql.engine.sqlite

import org.mixql.protobuf.GtypeConverter
import org.mixql.protobuf.messages.clientMsgs

import java.sql.*
import scala.collection.mutable

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String,
                  engineParams: mutable.Map[String, scalapb.GeneratedMessage] = mutable.Map())
  extends java.lang.AutoCloseable :

  def init() = {
    val url =
      try {
        engineParams("mixql.org.engine.sqlight.db.path")
          .asInstanceOf[clientMsgs.String]
          .value
      } catch {
        case e: Exception =>
          println(
            s"Module $identity: Warning: could not read db path from provided params: " + e.getMessage
          )
          println(s"Module $identity: use in memory db")
          "jdbc:sqlite::memory:"
      }
    SQLightJDBC.c = DriverManager.getConnection(url)
    println(s"Module $identity: opened database successfully")
  }

  def getSQLightJDBCConnection: Connection =
    if SQLightJDBC.c == null then init()
    SQLightJDBC.c

  // returns clientMsgs.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): scalapb.GeneratedMessage = {
    import org.mixql.protobuf.messages.clientMsgs
    var jdbcStmt: Statement = null

    try {
      jdbcStmt = getSQLightJDBCConnection.createStatement()
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
          val columnTypes: Seq[scalapb.GeneratedMessage] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arr: Seq[com.google.protobuf.any.Any] = Seq()
          while remainedRows
          do // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ GtypeConverter.toProtobufAny(seqGeneratedMsgToArray(rowValues))
            remainedRows = res.next()
          end while
          clientMsgs.Array(arr)
        } finally {
          if (res != null) res.close()
        }
      else clientMsgs.NULL()
    } catch {
      case e: Throwable =>
        clientMsgs.Error(
          s"Module $identity: SQLightJDBC error while execute: " + e.getMessage
        )
    } finally {
      if jdbcStmt != null then jdbcStmt.close()
    }
  }

  def seqGeneratedMsgToArray(msgs: Seq[scalapb.GeneratedMessage]): clientMsgs.Array =
    clientMsgs.Array({
      msgs
        .map { anyMsg =>
          GtypeConverter.toProtobufAny(anyMsg)
        }
    })

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[scalapb.GeneratedMessage]
                         ): Seq[scalapb.GeneratedMessage] =
    import org.mixql.protobuf.messages.clientMsgs
    for (i <- 1 to columnCount) yield {
      columnTypes(i - 1) match
        case clientMsgs.String(_, _, _) =>
          clientMsgs.String(res.getString(i), "")
        case clientMsgs.Bool(_, _) =>
          clientMsgs.Bool(res.getBoolean(i))
        case clientMsgs.Int(_, _) =>
          clientMsgs.Int(res.getInt(i))
        case clientMsgs.Double(_, _) =>
          clientMsgs.Double(res.getDouble(i))
        case clientMsgs.Array(_, _) =>
          readArrayFromResultSet(res.getArray(i))
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): clientMsgs.Array = {

    clientMsgs.Array({
      //      val javaSqlArray = res.getArray(i)
      javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
        case clientMsgs.String(_, _, _) =>
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str => clientMsgs.String(str, "") }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        case clientMsgs.Bool(_, _) =>
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map { value => clientMsgs.Bool(value) }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        case clientMsgs.Int(_, _) =>
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map { value => clientMsgs.Int(value) }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        case clientMsgs.Double(_, _) =>
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map { value => clientMsgs.Double(value) }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        case _: Any =>
          throw Exception(
            s"Module $identity: SQLightJDBC error while execute: unknown type of array"
          )
    })
  }

  def javaSqlTypeToClientMsg(intType: Int): scalapb.GeneratedMessage =
    import org.mixql.protobuf.messages.clientMsgs
    intType match
      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR =>
        clientMsgs.String("")
      case Types.BIT | Types.BOOLEAN => clientMsgs.Bool(false)
      case Types.NUMERIC =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        clientMsgs.String("")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        clientMsgs.Int(-1)
      case Types.BIGINT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        clientMsgs.String("")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => clientMsgs.Double(0.0)
      case Types.VARBINARY | Types.BINARY =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        clientMsgs.String("")
      case Types.DATE =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        clientMsgs.String("")
      case Types.TIMESTAMP =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        clientMsgs.String("")
      case Types.CLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        clientMsgs.String("")
      case Types.BLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        clientMsgs.String("")
      case Types.ARRAY => clientMsgs.Array(Seq())
      case Types.STRUCT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        clientMsgs.String("")
      case Types.REF =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        clientMsgs.String("")

  def getColumnTypes(
                      resultSetMetaData: ResultSetMetaData,
                      columnCount: Int
                    ): Seq[scalapb.GeneratedMessage] = {
    (for (i <- 1 to columnCount) yield resultSetMetaData.getColumnType(i)).map {
      intType => javaSqlTypeToClientMsg(intType)
    }
  }

  override def close(): Unit =
    println(s"Module $identity: executing close")
    if SQLightJDBC.c != null then
      try SQLightJDBC.c.close()
      catch
        case e: Throwable =>
          println(
            s"Warning: Module $identity: error while closing sql light connection: " +
              e.getMessage
          )
