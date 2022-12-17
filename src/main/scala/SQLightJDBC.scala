package org.mixql.engine.sqlite

import org.mixql.protobuf.messages.clientMsgs.AnyMsg
import org.mixql.protobuf.messages.clientMsgs

import java.sql.*

object SQLightJDBC {

  import com.typesafe.config.{Config, ConfigFactory}

  val config = ConfigFactory.load()
  var c: Connection = null
}

class SQLightJDBC(identity: String) extends java.lang.AutoCloseable:

  def init() = {
    import SQLightJDBC.config
    val url =
      try {
        config.getString("mixql.org.engine.sqlight.db.path")
      } catch {
        case e: Exception =>
          println(
            s"Module $identity: Warning: could not read db path from config: " + e.getMessage
          )
          println(s"Module $identity: use in memory db")
          "jdbc:sqlite::memory:"
      }
    SQLightJDBC.c = DriverManager.getConnection(url)
    println(s"Module $identity: opened database successfully")
  }

  // returns clientMsgs.Type
  // TO-DO Should return iterator?
  def execute(stmt: String): scalapb.GeneratedMessage = {
    import org.mixql.protobuf.messages.clientMsgs
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
          val columnTypes: Seq[scalapb.GeneratedMessage] =
            getColumnTypes(resultSetMetaData, columnCount)
          val columnNames: Seq[String] =
            for (i <- 1 to columnCount) yield resultSetMetaData.getColumnName(i)

          import org.mixql.engine.sqlite.JavaSqlArrayConverter

          var arr: Seq[com.google.protobuf.any.Any] = Seq()
          while remainedRows
          do // simulate do while, as it is no longer supported in scala 3
            val rowValues = getRowFromResultSet(res, columnCount, columnTypes)
            arr = arr :+ com.google.protobuf.any.Any.pack(
              AnyMsg(
                clientMsgs.Array(Seq()).getClass.getName
                ,
                Some(
                  com.google.protobuf.any.Any
                    .pack(seqGeneratedMsgToArray(rowValues))
                )
              )
            )
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

  def seqGeneratedMsgToArray(msgs: Seq[clientMsgs.AnyMsg]): clientMsgs.Array =
    clientMsgs.Array({
      msgs
        .map { anyMsg =>
          com.google.protobuf.any.Any.pack(anyMsg)
        }
    })

  def getRowFromResultSet(
                           res: ResultSet,
                           columnCount: Int,
                           columnTypes: Seq[scalapb.GeneratedMessage]
                         ): Seq[clientMsgs.AnyMsg] =
    import org.mixql.protobuf.messages.clientMsgs
    for (i <- 1 to columnCount - 1) yield {
      columnTypes(i) match
        case clientMsgs.String(_, _, _) =>
          AnyMsg(
            clientMsgs.String("", "").getClass.getName
            ,
            Some(
              com.google.protobuf.any.Any
                .pack(clientMsgs.String(res.getString(i), ""))
            )
          )
        case clientMsgs.Bool(_, _) =>
          AnyMsg(
            clientMsgs.Bool(false).getClass.getName,
            Some(
              com.google.protobuf.any.Any
                .pack(clientMsgs.Bool(res.getBoolean(i)))
            )
          )
        case clientMsgs.Int(_, _) =>
          AnyMsg(
            clientMsgs.Int(-1).getClass.getName,
            Some(
              com.google.protobuf.any.Any
                .pack(clientMsgs.Int(res.getInt(i)))
            )
          )
        case clientMsgs.Double(_, _) =>
          AnyMsg(
            clientMsgs.Double(-1.0).getClass.getName,
            Some(
              com.google.protobuf.any.Any
                .pack(clientMsgs.Double(res.getDouble(i)))
            )
          )
        case clientMsgs.Array(_, _) =>
          AnyMsg(
            clientMsgs.Array(Seq()).getClass.getName,
            Some(
              com.google.protobuf.any.Any
                .pack(readArrayFromResultSet(res.getArray(i)))
            )
          )
    }

  def readArrayFromResultSet(javaSqlArray: java.sql.Array): clientMsgs.Array = {

    clientMsgs.Array({
      //      val javaSqlArray = res.getArray(i)
      javaSqlTypeToClientMsg(javaSqlArray.getBaseType) match
        case clientMsgs.String(_, _, _) =>
          JavaSqlArrayConverter
            .toStringArray(javaSqlArray)
            .map { str =>
              AnyMsg(
                clientMsgs.String("", "").getClass.getName,
                Some(
                  com.google.protobuf.any.Any
                    .pack(clientMsgs.String(str, ""))
                )
              )
            }
            .toSeq
            .map { anyMsg =>
              com.google.protobuf.any.Any.pack(anyMsg)
            }
        case clientMsgs.Bool(_, _) =>
          JavaSqlArrayConverter
            .toBooleanArray(javaSqlArray)
            .map { value =>
              AnyMsg(
                clientMsgs.Bool(false).getClass.getName,
                Some(
                  com.google.protobuf.any.Any
                    .pack(clientMsgs.Bool(value))
                )
              )
            }
            .toSeq
            .map { anyMsg =>
              com.google.protobuf.any.Any.pack(anyMsg)
            }
        case clientMsgs.Int(_, _) =>
          JavaSqlArrayConverter
            .toIntArray(javaSqlArray)
            .map { value =>
              AnyMsg(
                clientMsgs.Int(-1).getClass.getName,
                Some(
                  com.google.protobuf.any.Any
                    .pack(clientMsgs.Int(value))
                )
              )
            }
            .toSeq
            .map { anyMsg =>
              com.google.protobuf.any.Any.pack(anyMsg)
            }
        case clientMsgs.Double(_, _) =>
          JavaSqlArrayConverter
            .toDoubleArray(javaSqlArray)
            .map { value =>
              AnyMsg(
                clientMsgs.Double(-1.0).getClass.getName,
                Some(
                  com.google.protobuf.any.Any
                    .pack(clientMsgs.Double(value))
                )
              )
            }
            .toSeq
            .map { anyMsg =>
              com.google.protobuf.any.Any.pack(anyMsg)
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
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type NUMERIC"
        )
      case Types.TINYINT | Types.SMALLINT | Types.INTEGER =>
        clientMsgs.Int(-1)
      case Types.BIGINT =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BIGINT"
        )
      case Types.REAL | Types.FLOAT | Types.DOUBLE => clientMsgs.Double(0.0)
      case Types.VARBINARY | Types.BINARY =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type VARBINARY or BINARY"
        )
      case Types.DATE =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type Date"
        )
      case Types.TIMESTAMP =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type TIMESTAMP"
        )
      case Types.CLOB =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type CLOB"
        )
      case Types.BLOB =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type BLOB"
        )
      case Types.ARRAY => clientMsgs.Array(Seq())
      case Types.STRUCT =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type STRUCT"
        )
      case Types.REF =>
        throw Exception(
          s"Module $identity: SQLightJDBC error while execute: " +
            "unsupported column type REF"
        )

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
