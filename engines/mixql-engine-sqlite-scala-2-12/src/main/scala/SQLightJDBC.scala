package org.mixql.engine.sqlite

import org.mixql.protobuf.{GtypeConverter, ProtoBufConverter, messages}

import java.sql._
import scala.collection.mutable

object SQLightJDBC {
  var c: Connection = null
}

class SQLightJDBC(identity: String,
                  engineParams: mutable.Map[String, messages.Message] = mutable.Map())
  extends java.lang.AutoCloseable {

  def init() = {
    val url =
      try {
        engineParams("mixql.org.engine.sqlight.db.path")
          .asInstanceOf[messages.gString].value
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

          var arr: Seq[String] = Seq()
          while (remainedRows) {
            // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ ProtoBufConverter.toJson(seqGeneratedMsgToArray(rowValues)).get
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

  def seqGeneratedMsgToArray(msgs: Seq[messages.Message]): messages.gArray = {

    new messages.gArray({
      msgs
        .map { anyMsg =>
          ProtoBufConverter.toJson(anyMsg).get
        }.toArray
    })
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
            .toSeq
            .map { anyMsg =>
              ProtoBufConverter.toJson(anyMsg).get
            }.toArray
        )
      case _: messages.Bool =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map {
              value => new messages.Bool(value)
            }
            .toSeq
            .map { anyMsg =>
              ProtoBufConverter.toJson(anyMsg).get
            }.toArray
        )
      case _: messages.gInt =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map {
              value => new messages.gInt(value)
            }
            .toSeq
            .map { anyMsg =>
              ProtoBufConverter.toJson(anyMsg).get
            }.toArray
        )
      case _: messages.gDouble =>
        new messages.gArray(
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map {
              value => new messages.gDouble(value)
            }
            .toSeq
            .map { anyMsg =>
              ProtoBufConverter.toJson(anyMsg).get
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
        new messages.gString("","")
      case Types.BIT | Types.BOOLEAN => new messages.Bool(false)
      case Types.NUMERIC =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
        new messages.gString("","")
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        new messages.gInt(-1)
      case Types.BIGINT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
        new messages.gString("","")
      case Types.REAL | Types.FLOAT | Types.DOUBLE => new messages.gDouble(0.0)
      case Types.VARBINARY | Types.BINARY =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
        new messages.gString("","")
      case Types.DATE =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
        new messages.gString("","")
      case Types.TIMESTAMP =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
        new messages.gString("","")
      case Types.CLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
        new messages.gString("","")
      case Types.BLOB =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
        new messages.gString("","")
      case Types.ARRAY => new messages.gArray(Seq().toArray)
      case Types.STRUCT =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
        new messages.gString("","")
      case Types.REF =>
        println(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )
        new messages.gString("","")
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
    println(s"Module $identity: executing close")

    if (SQLightJDBC.c != null) {
      try SQLightJDBC.c.close()
      catch {
        case e: Throwable =>
          println
          (
            s"Warning: Module $identity: error while closing sql light connection: " +
              e.getMessage
            )
      }
    }
  }
}