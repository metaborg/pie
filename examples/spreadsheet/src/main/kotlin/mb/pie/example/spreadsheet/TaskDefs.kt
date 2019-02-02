package mb.pie.example.spreadsheet

import mb.fs.java.JavaFSNode
import mb.fs.java.JavaFSPath
import mb.pie.api.ExecContext
import mb.pie.api.None
import mb.pie.api.TaskDef
import mb.pie.api.fs.stamp.FileSystemStampers
import java.io.File
import java.nio.file.Path
import java.io.Serializable

class Cell : TaskDef<JavaFSPath, Int> {
    override val id: String = javaClass.simpleName
    override fun ExecContext.exec(input: JavaFSPath): Int {
        val bytes = JavaFSNode(input).readAllBytes()
        val lines = String(bytes).split(" ","\n").filter({ s -> s.isNotEmpty()})
        require(input.javaPath, FileSystemStampers.hash)
        var sum = 0
        for ( line in lines) {
            try {
                sum += line.toInt()
            } catch (e :  NumberFormatException) {
                val path = input.parent!!.appendSegment(line).normalized
                val sub_sum = require(Cell(),path)
                sum += sub_sum
            }
        }
        return sum

    }
}

class Sheet : TaskDef<JavaFSPath, Int> {
    override val id: String = javaClass.simpleName
    override fun ExecContext.exec(input: JavaFSPath): Int {
        val path =  input.appendSegment("root").normalized
        return require(Cell(),path)
    }
}

class MultiSheet : TaskDef<MultiSheet.Input, None> {
    data class Input (val workspace : JavaFSPath) : Serializable
    override val id: String = javaClass.simpleName
    override fun ExecContext.exec(input: Input): None {
        val sheetDirectories = JavaFSNode(input.workspace)
                .list()
                .filter { it.isDirectory }
        for( entry in sheetDirectories ) {

            require(Sheet(),entry.path)

        }
        return None()
    }
}

