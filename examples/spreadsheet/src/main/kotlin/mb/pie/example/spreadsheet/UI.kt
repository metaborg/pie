package mb.pie.example.spreadsheet

import jdk.nashorn.internal.codegen.ObjectClassGenerator.pack
import mb.fs.java.JavaFSNode
import mb.fs.java.JavaFSPath
import mb.pie.api.ResourceKey
import mb.pie.api.StoreReadTxn
import mb.pie.api.TaskData
import mb.pie.api.TaskKey
import mb.pie.api.fs.FileSystemResource
import mb.pie.runtime.PieImpl
import mb.pie.runtime.store.InMemoryStore
import java.awt.BorderLayout
import java.awt.Label
import javax.swing.*


class SpreadSheet(pie: PieImpl, root_task: TaskKey, inspector: StoreInspector) : JFrame() {
    private val pie = pie
    private val root_task = root_task
    private val panel = JPanel()
    private var active : TaskKey? = null
    private val inspector = inspector
    init {
        title = "Spreadsheet2"

        val sheets = tx().taskRequires(root_task);
        pie.dropOutput(root_task)
        for (sheet in sheets) {
            pie.dropOutput(sheet.callee);
        }

        sheets.getOrNull(0)?.let { e -> setActiveSheet(e.callee)}

        isVisible = true
        refresh()
        pack();
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    fun setActiveSheet(key : TaskKey) {
        if(key == active) { return }
        println("Activate $key : Disable : $active")
        active?.let { active ->  pie.dropOutput(active) }
        pie.addOutput(key);
        val store = pie.store.readTxn() as InMemoryStore;
        inspector.add_state(store.dump())
        //check_state(store)
        active = key
    }

    fun refresh() {
        panel.removeAll()
        val tabs = JTabbedPane()
        panel.add(tabs, BorderLayout.CENTER);


        val root = (tx().input(root_task) as MultiSheet.Input).workspace
        panel.add(PromptComponent(root,{e -> update(e)}),BorderLayout.SOUTH)
        add(panel, BorderLayout.CENTER);

        val sheets = tx().taskRequires(root_task);
        for (sheet in sheets) {
            val sheet_state = tx().data(sheet.callee) as TaskData<JavaFSPath, Int>;
            val sheet_name = sheet_state.input.javaPath.fileName
            val sheet_result = sheet_state.output
            val cells_state = all_dependents(tx(),sheet.callee).map { req -> tx().data(req) as TaskData<JavaFSPath,Int> }.toList()
            val component = SheetComponent(sheet.callee,sheet_state,cells_state,{e -> update(e)} )
            tabs.addTab(sheet_name.toString() + "($sheet_result)",component)
            if( sheet.callee == active ) {
                tabs.selectedComponent = component
            }
        }

        val store = pie.store.readTxn() as InMemoryStore;
        inspector.add_state(store.dump())
        //check_state(store)

        tabs.addChangeListener{ _ ->
            if (tabs.selectedComponent != null) {
                val selected = tabs.selectedComponent as SheetComponent
                setActiveSheet(selected.key)
            }
        }
    }

    fun update(changed_file : JavaFSPath) {
        println("Updated: ${changed_file}")
        val asResourceKey = FileSystemResource(JavaFSNode(changed_file.javaPath)).key()
        pie.bottomUpExecutor.requireBottomUp(setOf(asResourceKey))
        //check_state(pie.store.readTxn() as InMemoryStore)
        println("Ok: ${changed_file}")
        refresh()
    }

    fun tx() : StoreReadTxn {
        return pie.store.readTxn()
    }


    class SheetComponent(key: TaskKey, sheet_state: TaskData<JavaFSPath,Int>, cells_state: List<TaskData<JavaFSPath, Int>>, refresh:(JavaFSPath) -> Unit) : JPanel() {
        val key = key
        private val sheet_state =sheet_state
        val cells_state = cells_state
        val cells = JTabbedPane()
        init {
            setSize(300,300)
            add(Label("Result ${sheet_state.output}"))
            add(cells, BorderLayout.CENTER);
            for ( cell in cells_state) {
                val name = "${cell.input.javaPath.fileName.toString()} = ${cell.output}"
                cells.addTab(name,CellComponent(cell,refresh))
            }


        }
    }

    class CellComponent(state : TaskData<JavaFSPath,Int>,refresh:(JavaFSPath) -> Unit) : JPanel() {
        val content = JTextArea(10,5)
        private val state = state
        init {
            val node = JavaFSNode(state.input);
            content.text = String(node.readAllBytes())
            add(content)
            val btn = JButton();
            btn.text = "Save"
            btn.addActionListener { _ ->
                val out = content.text.toByteArray()
                node.writeAllBytes(out)
                refresh(state.input)
            }
            add(btn)
        }
    }


    class PromptComponent( root : JavaFSPath, refresh: (JavaFSPath) -> Unit) : JTextField(20) {
        init {
            addActionListener { _ ->
                val changed = root.appendSegment(this.text).normalized
                refresh(changed)

             }
        }
    }
}




