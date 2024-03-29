package org.mixql.repl

import org.beryx.textio.{ReadAbortedException, TextIO, TextIoFactory, TextTerminal}
import org.mixql.repl.TerminalOps

import java.util.function.BiConsumer
import org.mixql.core.context.Context
import org.beryx.textio.web.RunnerData
import org.mixql.core.run
import org.mixql.engine.core.BrakeException
import org.mixql.cluster.BrokerModule

import scala.util.Try

class TerminalApp(context: Context, prompt: String = "mixql>") extends BiConsumer[TextIO, RunnerData] {

  override def accept(_textIO: TextIO, runnerData: RunnerData): Unit = {
    try {
      implicit val textIO: TextIO = _textIO
      implicit val terminal: TextTerminal[_] = textIO.getTextTerminal

      init(terminal)
      while (true) {
        try {
          val stmt = readMixqlStmt()
          stmt.trim.toLowerCase match
            case ":help" => terminal.println("""
                |:exit -> terminate terminal session, platform will exit
                |:help -> print this message again
                |:show vars -> prints declared variables
                |:show vars with params -> prints declared variables overrided by engine params
                |:show functions -> prints available platform functions
                |:show functions on engine name_of_engine_here -> prints available functions on specified engine
                |:show engines -> prints available engines
                |:show current engine -> prints name of current engine on which command will be executed
                |:print var name_of_variable_here -> prints value of variable
                |""".stripMargin)
            case ":exit" =>
              printExitMessage()
              throw new org.mixql.engine.core.BrakeException()
            case ":show vars"             => terminal.println(context.getVars().toString())
            case ":show vars with params" => terminal.println(context.getParams().toString())
            case ":show functions"        => terminal.println(context.functions.keys.mkString(", "))
            case ":show engines"          => terminal.println(context.engineNames.mkString(", "))
            case ":show current engine"   => terminal.println(context.currentEngineAllias)
            case input: String =>
              try {
                if input.startsWith(":print var") then
                  terminal.println(context.getVar(input.split("\\s").apply(2)).toString)
                  throw new BrakeException()

                if input.startsWith(":show functions on engine") then
                  terminal.println({
                    context.getEngine(input.split("\\s").apply(4)).get.getDefinedFunctions().mkString(", ")
                  })
                  throw new BrakeException()

                val res = run(
                  {
                    if (!stmt.endsWith(";"))
                      stmt + ';'
                    else
                      stmt
                  },
                  context
                )
                terminal.println("returned: " + res.toString())
              } catch {
                case e: BrakeException =>
                case e: Throwable      => throw e
              }
        } catch {
          case e: ReadAbortedException                 => throw new org.mixql.engine.core.BrakeException()
          case e: org.mixql.engine.core.BrakeException => throw e
          case e: Throwable => printlnRedColor(e.getClass.getName + ":" + e.getMessage + "\n" + e.printStackTrace());
        }
      }
    } catch {
      case e: Throwable => println("Exited REPL mode")
    } finally {
      _textIO.dispose()
      Try(context.close())
      Try({
        if BrokerModule.wasStarted then BrokerModule.close()
      })
    }
  }

  def init(terminal: TextTerminal[_]): Unit = {

    terminal.println(
      "No files were provided. Platform is launching in REPL mode. " +
        "Type your statement and press ENTER. " +
        "You can not put ';' in REPL mode at the end of statement. " +
        "To get help type :help " +
        "To exit type ':exit' "
    )

    val keyStrokeAbort = "alt Z"
    val keyStrokeReboot = "ctrl R"
    val keyStrokeAutoValue = "ctrl S"

    val registeredAbort = TerminalOps.registerAbort(terminal, keyStrokeAbort)

    val registeredReboot = TerminalOps.registerReboot(terminal, keyStrokeReboot)

    val registeredAutoValue = TerminalOps.registerAutoValue(terminal, keyStrokeAutoValue)

    terminal.println("--------------------------------------------------------------------------------")
    if (registeredAbort)
      terminal.println("Press " + keyStrokeAbort + " to abort the program")
    if (registeredReboot)
      terminal.println("Press " + keyStrokeReboot + " to enter multiline mode")
    if (registeredAutoValue)
      terminal.println(
        "Press " + keyStrokeAutoValue +
          " to exit multiline mode"
      )
    terminal.println("You can use these key combinations at any moment during your data entry session.")
    terminal.println("--------------------------------------------------------------------------------")
  }

  def printlnRedColor(value: String)(implicit terminal: TextTerminal[_]): Unit = {
    terminal.executeWithPropertiesConfigurator(props => props.setPromptColor("red"), t => t.println(value))
  }

  private def readMixqlStmt()(implicit textIO: TextIO) = {
    var stmt = textIO.newStringInputReader.read(prompt)
    if (TerminalOps.MultiLineMode) {
      while (TerminalOps.MultiLineMode)
        TerminalOps.MultiLineString = TerminalOps.MultiLineString + textIO.newStringInputReader.read() + "\n"
      stmt = stmt + "\n" + TerminalOps.MultiLineString
      TerminalOps.MultiLineString = ""
    }
    stmt
  }

  def printExitMessage()(implicit textIO: TextIO): Unit = {
    textIO.newStringInputReader.withMinLength(0).read("\nPress enter to terminate...")
  }
}
