package mb.ceres

import name.falgout.jeffrey.testing.junit5.GuiceExtension
import name.falgout.jeffrey.testing.junit5.IncludeModule
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GuiceExtension::class)
@IncludeModule(TestModule::class)
internal class LMDBBuildStoreTests : TestBase() {
}