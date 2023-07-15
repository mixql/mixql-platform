package org.mixql.platform.demo.repl

import org.beryx.textio.{TextIO, TextIoFactory, TextTerminal}
import org.mixql.platform.demo.utils.TerminalOps

class Terminal extends AutoCloseable {
  private var _textIO: TextIO = null
  private var _terminal: TextTerminal[_] = null

  def textIO: TextIO = if _textIO == null then
    _textIO = TextIoFactory.getTextIO
    _textIO
  else
    _textIO

  def terminal: TextTerminal[_] = if _terminal == null then
    _terminal = textIO.getTextTerminal
    _terminal
  else
    _terminal

  def init(): Unit = {


    terminal.println("No files were provided. Platform is launching in REPL mode. " +
      "Type your statement and press ENTER. " +
      "You can not put ';' in REPL mode at the end of statement." +
      "To exit type ':exit'"
    )

    val keyStrokeAbort = "alt Z"
    val keyStrokeReboot = "ctrl R"
    val keyStrokeAutoValue = "ctrl S"

    val registeredAbort = TerminalOps.registerAbort(terminal, keyStrokeAbort)

    val registeredReboot = TerminalOps.registerReboot(terminal, keyStrokeReboot)

    val registeredAutoValue = TerminalOps.registerAutoValue(terminal, keyStrokeAutoValue)

    terminal.println("--------------------------------------------------------------------------------")
    if (registeredAbort) terminal.println("Press " + keyStrokeAbort + " to abort the program")
    if (registeredReboot) terminal.println("Press " + keyStrokeReboot + " to enter multiline mode")
    if (registeredAutoValue) terminal.println("Press " + keyStrokeAutoValue +
      " to exit multiline mode")
    terminal.println("You can use these key combinations at any moment during your data entry session.")
    terminal.println("--------------------------------------------------------------------------------")
  }

  def println(value : String): Unit ={
    terminal.println(value)
  }

  def printlnRedColor(value: String): Unit = {
    terminal.executeWithPropertiesConfigurator(
      props => props.setPromptColor("red"),
      t => t.println(value))
  }

  def readMixqlStmt(): String = {
    var stmt = textIO.newStringInputReader.read("mixql>")
    if (TerminalOps.MultiLineMode) {
      while (TerminalOps.MultiLineMode) {
        TerminalOps.MultiLineString = TerminalOps.MultiLineString + textIO.newStringInputReader.read() + "\n"
      }
      stmt = stmt + "\n" + TerminalOps.MultiLineString
      TerminalOps.MultiLineString = ""
    }
    stmt
  }

  def printExitMessage(): Unit = {
    textIO.newStringInputReader
      .withMinLength(0).read("\nPress enter to terminate...")
  }

  override def close(): Unit = {
    if _textIO != null then
      _textIO.dispose()
  }
}
