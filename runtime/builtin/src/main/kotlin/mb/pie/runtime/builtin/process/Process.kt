package mb.pie.runtime.builtin.process

import mb.pie.runtime.builtin.util.Tuple2
import mb.pie.runtime.builtin.util.tuple
import mb.pie.runtime.core.BuildException
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
  } catch (e: IOException) {
    throw BuildException("Failed to execute '${arguments.joinToString(" ")}'", e)
  }
}
