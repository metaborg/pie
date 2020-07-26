package mb.pie.lang.runtime

import mb.pie.api.ExecException
import java.io.IOException

fun execute(arguments: ArrayList<String>): Tuple2<String, String> {
  try {
    val process = ProcessBuilder(arguments)
      .directory(null)
      .inheritIO()
      .start()
    process.waitFor()
    val stdout = process.inputStream.bufferedReader().readText()
    System.out.print(stdout)
    val stderr = process.errorStream.bufferedReader().readText()
    System.err.print(stderr)
    return tuple(stdout, stderr)
  } catch(e: IOException) {
    throw ExecException("Failed to execute '${arguments.joinToString(" ")}'", e)
  }
}
