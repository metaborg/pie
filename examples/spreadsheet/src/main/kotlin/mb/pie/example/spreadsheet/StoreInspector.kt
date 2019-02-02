package mb.pie.example.spreadsheet

import mb.fs.java.JavaFSPath
import mb.pie.api.*
import mb.pie.api.fs.FileSystemResource
import mb.pie.runtime.PieImpl
import javax.swing.*
import mb.pie.runtime.store.InMemoryStore
import mb.pie.runtime.store.StoreDump
import java.awt.BorderLayout
import java.awt.Label
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane
import java.awt.Graphics
import java.awt.Panel
import java.io.IOException
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import java.awt.Dimension
import java.awt.Color
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.BorderFactory




class StoreInspector() : JFrame() {
    private var idx = 0
    private val panel = JPanel()
    private val states = mutableListOf<GraphViz>()

    init {
        title = "Store"
        isVisible = true
        add(panel, BorderLayout.CENTER);

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    fun add_state(dump : StoreDump) {
        panel.removeAll()
        panel.add(GraphViz(build_image(dump)))
        pack()
    }

    inner class GraphViz(image : BufferedImage) : JPanel() {

        private val image = image

        override fun getPreferredSize(): Dimension {
            return Dimension(image.width,image.height)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.drawImage(image, 0, 0, null) // see javadoc for more info on the parameters
        }


    }
}



fun build_image(dump: StoreDump) : BufferedImage {
    try {
        val proc = ProcessBuilder(listOf("dot","-Tjpeg"))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        val graph = toGraph(dump).toByteArray();
        proc.outputStream.buffered().use {
            it.write(graph)
        }
        return proc.inputStream.buffered().use {
            ImageIO.read(it)
        }
    } catch (ex: IOException) {
        throw ex
    }
}


fun toGraph(dump : StoreDump) : String {

    val keys = hashSetOf<TaskKey>()
    val get_id = { key : TaskKey -> keys.add(key); key.hashCode()}
    val files = hashSetOf<Path>()
    val get_file = { key : Path -> files.add(key); key.hashCode()}
    val x: FileSystemResource
    val taskReqs = dump.taskReqs.flatMap{ (e,v) -> v.map {  caller -> "${get_id(e)} -> ${get_id(caller.callee)} [arrowhead=dot]" } }
    val fileReqs = dump.fileReqs.flatMap { (e,v) -> v.map{ filereq -> "${get_id(e)} -> ${get_file(Paths.get(filereq.key.key.toString()))} [arrowhead=normal]"} }

    val key_labels = keys.map{ k ->
        val color = when (dump.observables.getOrDefault(k,Observability.Attached)) {
            Observability.Attached -> "#40e0d0"
            Observability.Observed -> "#ff00ff"
            Observability.Detached -> "#333333"
        }
        """${k.hashCode()} [label="${k.id}\n${k.key}",color="${color}"]"""}
    val file_labels = files.map{ k ->
        val label = "${ k.parent.fileName }/${k.fileName}"
        """${k.hashCode()} [label="${label}"]"""}

    val result = """
                digraph G {
                    node [shape=box]
                    ${key_labels.joinToString("\n")}
                    ${file_labels.joinToString("\n")}
                    ${taskReqs.joinToString("\n")}
                    ${fileReqs.joinToString("\n")}
                }
        """
    return result
}



