package mb.pie.runtime.test

import com.nhaarman.mockitokotlin2.*
import mb.pie.api.None
import mb.pie.api.STask
import mb.pie.api.exec.NullCancelableToken
import mb.pie.api.test.anyC
import mb.pie.api.test.anyER
import mb.pie.api.test.readResource
import mb.pie.api.test.toLowerCase
import mb.pie.runtime.exec.NoData
import mb.pie.runtime.layer.ValidationException
import mb.resource.fs.FSResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

class BottomUpTests {
  private val builder = DefaultRuntimeTestBuilder()


  @TestFactory
  fun testUpdateAffectedBy() = builder.test {
    val lowerDef = spy(toLowerCase)
    addTaskDef(lowerDef)
    val readDef = spy(readResource)
    addTaskDef(readDef)
    val combDef = spy(taskDef<FSResource, String>("combine", { input, _ -> "toLowerCase(read($input))" }) {
      val text = require(readDef.createTask(it))
      require(lowerDef.createTask(text))
    })
    addTaskDef(combDef)

    val str = "HELLO WORLD!"
    val file = resource("/file")
    write(str, file)

    val combTask = combDef.createTask(file)
    val combKey = combTask.key()
    var combOutput: String? = null
    var combObserved = 0
    pie.setCallback(combTask) { s -> combOutput = s; ++combObserved }

    val readTask = readDef.createTask(file)
    val readKey = readTask.key()
    var readOutput: String? = null
    var readObserved = 0
    pie.setCallback(readTask) { s -> readOutput = s; ++readObserved }

    val lowerTask = lowerDef.createTask(str)
    val lowerKey = lowerTask.key()
    var lowerOutput: String? = null
    var lowerObserved = 0
    pie.setCallback(lowerTask) { s -> lowerOutput = s; ++lowerObserved }

    // Build [combineTask] in top-down fashion, observe rebuild of all.
    newSession().use { session ->
      val output = session.require(combTask)
      Assertions.assertEquals("hello world!", output)
      Assertions.assertEquals("hello world!", combOutput)
      Assertions.assertEquals(1, combObserved)
      Assertions.assertEquals("HELLO WORLD!", readOutput)
      Assertions.assertEquals(1, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      val topDownSession = session.topDownSession
      inOrder(topDownSession) {
        verify(topDownSession).exec(eq(combKey), eq(combTask), eq(NoData()), any(), anyC())
        verify(topDownSession).exec(eq(readKey), eq(readTask), eq(NoData()), any(), anyC())
        verify(topDownSession).exec(eq(lowerKey), eq(lowerTask), eq(NoData()), any(), anyC())
      }
    }

    // Change required file in such a way that the output of [readTask] changes (change file content).
    val newStr = "!DLROW OLLEH"
    write(newStr, file)

    val lowerRevTask = lowerDef.createTask(newStr)
    val lowerRevKey = lowerRevTask.key()
    var lowerRevOutput: String? = null
    var lowerRevObserved = 0
    pie.setCallback(lowerRevTask) { s -> lowerRevOutput = s; ++lowerRevObserved }

    // Notify of file change, observe bottom-up execution of directly affected [readTask], which then affects
    // [combTask], which in turn requires [lowerRevTask].
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key))
      // [combTask]'s key has not changed, since it is based on a file name that did not change.
      Assertions.assertEquals("!dlrow olleh", combOutput)
      Assertions.assertEquals(2, combObserved)
      // [readTask]'s key has not changed, since it is based on a file name that did not change.
      Assertions.assertEquals("!DLROW OLLEH", readOutput)
      Assertions.assertEquals(2, readObserved)
      // [lowerTask]'s key changed, so the previous task was not required, and thus not observed (asserts are same as last session).
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      // [lowerRevTask] has been required, and has thus been observed once.
      Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
      Assertions.assertEquals(1, lowerRevObserved)
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        verify(bottomUpSession).exec(eq(readKey), eq(readTask), anyER(), anyC())
        verify(bottomUpSession).exec(eq(combKey), eq(combTask), anyER(), anyC())
        verify(bottomUpSession).require(eq(lowerRevKey), eq(lowerRevTask), any(), anyC())
        verify(bottomUpSession).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
      }
    }

    // Notify of file change, but file hasn't actually changed, observe no execution.
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key), NullCancelableToken.instance)
      // Since no task has been affected by the file change, no observers are observed (all asserts are same as last session).
      Assertions.assertEquals("!dlrow olleh", combOutput)
      Assertions.assertEquals(2, combObserved)
      Assertions.assertEquals("!DLROW OLLEH", readOutput)
      Assertions.assertEquals(2, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
      Assertions.assertEquals(1, lowerRevObserved)
      val bottomUpSession = session.bottomUpSession
      verify(bottomUpSession, never()).exec(eq(readKey), eq(readTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(bottomUpSession, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }

    // Change required file in such a way that the file changes (modified date), but the output of [readTask] does not.
    write(newStr, file)

    // Notify of file change, observe bottom-up execution of [readTask], but stop there because [combineTask] is still consistent.
    newSession().use { session ->
      session.updateAffectedBy(setOf(file.key), NullCancelableToken.instance)
      Assertions.assertEquals("!dlrow olleh", combOutput)
      Assertions.assertEquals(2, combObserved)
      Assertions.assertEquals("!DLROW OLLEH", readOutput)
      Assertions.assertEquals(3, readObserved)
      Assertions.assertEquals("hello world!", lowerOutput)
      Assertions.assertEquals(1, lowerObserved)
      Assertions.assertEquals("!dlrow olleh", lowerRevOutput)
      Assertions.assertEquals(1, lowerRevObserved)
      val bottomUpSession = session.bottomUpSession
      inOrder(bottomUpSession) {
        verify(bottomUpSession).exec(eq(readKey), eq(readTask), anyER(), anyC())
      }
      verify(bottomUpSession, never()).exec(eq(combKey), eq(combTask), anyER(), anyC())
      verify(bottomUpSession, never()).require(eq(lowerRevKey), eq(lowerRevTask), any(), anyC())
      verify(bottomUpSession, never()).exec(eq(lowerRevKey), eq(lowerRevTask), anyER(), anyC())
    }
  }

  @TestFactory
  fun testDifferentInputsFromRequiredFails() = builder.test {
    val singletonTaskDef = taskDef<Int, None>("singleton", { _ -> None.instance }) {
      println(it)
      None.instance
    }
    addTaskDef(singletonTaskDef)

    val file = resource("/file")
    val requireSingletonTaskDef = taskDef<Int, None>("requireSingletonTaskDef") {
      require(file)
      val text = file.readString()
      require(singletonTaskDef.createTask(it))
      if(text.contains("galaxy")) {
        require(singletonTaskDef.createTask(it + 1))
      }
      None.instance
    }
    addTaskDef(requireSingletonTaskDef)

    write("Hello, world!", file)
    newSession().use { session ->
      session.require(requireSingletonTaskDef.createTask(1))
    }

    write("Hello, galaxy!", file)
    newSession().use { session ->
      Assertions.assertThrows(ValidationException::class.java) {
        session.updateAffectedBy(setOf(file.key))
      }
    }
  }

  @TestFactory
  fun testDifferentInputsFromAffectedFails() = builder.test {
    val backendDef = taskDef<Triple<String, String, STask>, None>("backend", { (name, _, _) -> name }) { (_, text, frontendTask) ->
      require(frontendTask)
      println(text)
      None.instance
    }
    addTaskDef(backendDef)

    val frontendDef = taskDef<FSResource, Pair<String, String>>("frontend") { file ->
      require(file)
      val text = file.readString()
      Pair("stableName", text)
    }
    addTaskDef(frontendDef)

    val mainDef = taskDef<FSResource, None>("main") { path ->
      val frontendTask = frontendDef.createTask(path)
      val (name, text) = require(frontendTask)
      require(backendDef.createTask(Triple(name, text, frontendTask.toSerializableTask())))
    }
    addTaskDef(mainDef)

    val file = resource("/file")
    write("Hello, world!", file)
    newSession().use { session ->
      session.require(mainDef.createTask(file))
    }

    write("Hello, INCREMENTALITY BUG!", file)
    newSession().use { session ->
      Assertions.assertThrows(ValidationException::class.java) {
        // We use task(input) to refer to a task with some input, and `task(key)` to refer to a task key. This bottom-up
        // build triggers a validation error because of the following sequence of events:
        // 1. Task frontend(/file) is directly affected, and thus scheduled.
        // 2. Task frontend(/file) is executed, and produces a new output: ("stableName", "Hello, INCREMENTALITY BUG!").
        //    Note the different text in the second element of the pair.
        // 3. Dependents of frontend(/file) are scheduled: `backend("stableName")` and `main(/file)`.
        // 4. Because `main(/file)` depends on `backend("stableName")`, the backend task is executed before the
        //    main task.
        // 5. Task backend(("stableName", "Hello, world!", frontend(/file))) is executed.
        //    Note that it takes the old text as input!
        // 6. Task main(/file) is executed.
        //    a. It requires task frontend(/file), which has already been visited, so its output
        //       ("stableName", "Hello, INCREMENTALITY BUG!") is returned immediately. This output is used to...
        //    b. require task backend(("stableName", "Hello, INCREMENTALITY BUG!", frontend(/file))).
        //    c. Since `backend("stableName")` has already been visited this session, but we are requiring it with a
        //       different input (text has changed), a validation error is triggered catching this incrementality bug.
        session.updateAffectedBy(setOf(file.key))
      }
    }
  }

  @TestFactory
  fun testRequireProvidedResourceAffected() = builder.test {
    val providerDef = taskDef<FSResource, FSResource>("provider") { inputFile ->
      require(inputFile)
      val outputFile = inputFile.replaceLeafExtension("out")
      inputFile.copyTo(outputFile)
      provide(outputFile)
      outputFile
    }
    addTaskDef(providerDef)
    val file = resource("/inputFile.in")
    write("Hello, world!", file)
    val providerTask = providerDef.createTask(file)

    val requirerDef = taskDef<None, String>("requirer") {
      val outputFile = require(providerTask)
      require(outputFile)
      outputFile.readString()
    }
    addTaskDef(requirerDef)
    val requirerTask = requirerDef.createTask(None.instance)

    // Initial build.
    newSession().use { session ->
      session.require(requirerTask)
    }

    // Change input file: re-execute requirer as well, even though it is not affected by the output value of the provider.
    write("Hello, world!!!!!", file)
    newSession().use { session ->
      session.updateAffectedBy(hashSetOf(file.key))
      val bottomUpSession = session.bottomUpSession
      verify(bottomUpSession).exec(eq(providerTask.key()), eq(providerTask), anyER(), anyC())
      verify(bottomUpSession).exec(eq(requirerTask.key()), eq(requirerTask), anyER(), anyC())
    }
  }
}
