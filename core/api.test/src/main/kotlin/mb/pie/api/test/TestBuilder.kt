package mb.pie.api.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream

open class TestBuilder {
  fun build(name: String, testFunc: TestCtx.() -> Unit): Stream<out DynamicTest> {
    val filesystem = Jimfs.newFileSystem(Configuration.unix())
    val test = DynamicTest.dynamicTest(name) {
      val context = TestCtx(filesystem)
      context.testFunc()
    }
    return Stream.of(test)
  }
}
