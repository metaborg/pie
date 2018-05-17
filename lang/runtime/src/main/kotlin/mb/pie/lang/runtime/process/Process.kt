package mb.pie.lang.runtime.process

import mb.pie.api.ExecException
import mb.pie.lang.runtime.util.Tuple2
import mb.pie.lang.runtime.util.tuple
import java.io.IOException

fun execute(arguments: ArrayList<String>): Tuple2<String, String> {
  try {
    val proc = ProcessBuilder(arguments)
      .directory(null)
      .inheritIO()
      .start()
    proc.waitFor()
    val stdout = proc.inputStream.bufferedReader().readText()
    System.out.print(stdout)
    val stderr = proc.errorStream.bufferedReader().readText()
    System.err.print(stderr)
    return tuple(stdout, stderr)
  } catch(e: IOException) {
    throw ExecException("Failed to execute '${arguments.joinToString(" ")}'", e)
  }
}
