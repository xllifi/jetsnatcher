import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("jvm")
  alias(libs.plugins.kotlin.serialization)

  // Apply the Application plugin to add support for building an executable JVM application.
  application
}

kotlin {
  // Use a specific Java version to make it easier to work in different environments.
  jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
  // Configure all test Gradle tasks to use JUnitPlatform.
  useJUnitPlatform()

  // Log information about all test results, not only the failed ones.
  testLogging {
    events(
      TestLogEvent.FAILED,
      TestLogEvent.PASSED,
      TestLogEvent.SKIPPED
    )
  }
}


dependencies {
//  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.bundles.ktor)
  implementation(libs.bundles.kotlinx)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.ktor.client.mock)
}

application {

  // Define the Fully Qualified Name for the application main class
  // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
  mainClass = "BooruApiKt"
}
