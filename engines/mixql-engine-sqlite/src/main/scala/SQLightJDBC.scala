package org.mixql.engine.sqlite

import org.mixql.protobuf.GtypeConverter
import org.mixql.protobuf.generated.messages

import java.sql.*
import scala.collection.mutable

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String,
                  engineParams: mutable.Map[String, com.google.protobuf.GeneratedMessageV3] = mutable.Map())
  extends java.lang.AutoCloseable :

  def init() = {
    val url =
      try {
        engineParams("mixql.org.engine.sqlight.db.path")
          .asInstanceOf[messages.String]
          .getValue
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

  // returns messages.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): com.google.protobuf.GeneratedMessageV3 = {
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
          val columnTypes: Seq[com.google.protobuf.GeneratedMessageV3] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arrayBuilder = messages.Array.newBuilder
          while remainedRows
          do // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arrayBuilder = arrayBuilder.addArr(
              GtypeConverter.toProtobufAny(seqGeneratedMsgToArray(rowValues))
            )
            remainedRows = res.next()
          end while
          arrayBuilder.build()
        } finally {
          if (res != null) res.close()
        }
      else messages.NULL.getDefaultInstance
    } catch {
      case e: Throwable =>
        messages.Error.newBuilder().setMsg(
          s"Module $identity: SQLightJDBC error while execute: " + e.getMessage
        ).build()
    } finally {
      if jdbcStmt != null then jdbcStmt.close()
    }
  }

  def seqGeneratedMsgToArray(msgs: Seq[com.google.protobuf.GeneratedMessageV3]): messages.Array =
    var arrayBuilder = messages.Array.newBuilder

    msgs.map(anyMsg =>
      GtypeConverter.toProtobufAny(anyMsg)
    ) foreach (
      v => arrayBuilder = arrayBuilder.addArr(v)
      )

    arrayBuilder.build()

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[com.google.protobuf.GeneratedMessageV3]
                         ): Seq[com.google.protobuf.GeneratedMessageV3] =

    for (i <- 1 to columnCount) yield {
      columnTypes(i - 1) match
        case _: messages.String =>
          messages.String
            .newBuilder()
            .setValue(res.getString(i))
            .setQuote("")
            .build()
        case _: messages.Bool =>
          messages.Bool.newBuilder().setValue(res.getBoolean(i)).build()
        case _: messages.Int =>
          messages.Int.newBuilder().setValue(res.getInt(i)).build()
        case _: messages.Double =>
          messages.Double.newBuilder().setValue(res.getDouble(i)).build()
        case _: messages.Array =>
          readArrayFromResultSet(res.getArray(i))
    }

  def seqGeneratedMsgAnyToArray(msgs: Seq[com.google.protobuf.Any]): messages.Array =
    var arrayBuilder = messages.Array.newBuilder

    msgs foreach (
      v => arrayBuilder = arrayBuilder.addArr(v)
      )

    arrayBuilder.build()

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): messages.Array = {

    javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
      case _: messages.String =>
        seqGeneratedMsgAnyToArray(
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str =>
              messages.String.newBuilder().setValue(str).setQuote("").build()
            }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        )
      case _: messages.Bool =>
        seqGeneratedMsgAnyToArray(
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map {
              value => messages.Bool.newBuilder().setValue(value).build()
            }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        )
      case _: messages.Int =>
        seqGeneratedMsgAnyToArray(
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map {
              value => messages.Int.newBuilder().setValue(value).build()
            }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        )
      case _: messages.Double =>
        seqGeneratedMsgAnyToArray(
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map {
              value => messages.Double.newBuilder().setValue(value).build()
            }
            .toSeq
            .map { anyMsg =>
              GtypeConverter.toProtobufAny(anyMsg)
            }
        )
      case _: Any =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: unknown type of array"
        )
  }

  def javaSqlTypeToClientMsg(intType: Int): com.google.protobuf.GeneratedMessageV3 =

    intType match
      case Types.VARCHAR | Types.CHAR | Types.LONGVARCHAR =>
        messages.String.getDefaultInstance
      case Types.BIT | Types.BOOLEAN => messages.Bool.getDefaultInstance
      case Types.NUMERIC =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        messages.String.getDefaultInstance
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        messages.Int.getDefaultInstance
      case Types.BIGINT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        messages.String.getDefaultInstance
      case Types.REAL | Types.FLOAT | Types.DOUBLE => messages.Double.getDefaultInstance
      case Types.VARBINARY | Types.BINARY =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        messages.String.getDefaultInstance
      case Types.DATE =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        messages.String.getDefaultInstance
      case Types.TIMESTAMP =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        messages.String.getDefaultInstance
      case Types.CLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        messages.String.getDefaultInstance
      case Types.BLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        messages.String.getDefaultInstance
      case Types.ARRAY => messages.Array.getDefaultInstance
      case Types.STRUCT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        messages.String.getDefaultInstance
      case Types.REF =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        messages.String.getDefaultInstance

  def getColumnTypes(
                      resultSetMetaData: ResultSetMetaData,
                      columnCount: Int
                    ): Seq[com.google.protobuf.GeneratedMessageV3] = {
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
