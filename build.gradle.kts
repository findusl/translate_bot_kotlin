import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*


plugins {
	application
	idea
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.serialization") version "1.8.22"
	id("com.github.ben-manes.versions") version "0.47.0"
}

group = "de.lehrbaum"
version = "1.2-SNAPSHOT"

application {
	mainClass.set("de.lehrbaum.bot.translate.MainKt")
}

repositories {
	mavenCentral()
	maven(url = "https://jitpack.io")
}

dependencies {
	// dependabot does not find the versions in the gradle.properties, but it does here
	val koinVersion = "3.4.1"
	val junitVersion = "5.9.3"
	val jsonwebtokenVersion = "0.11.5"
	val ktorVersion = "2.3.2"

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

	implementation("com.charleskorn.kaml:kaml-jvm:0.54.0")

	implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-gson:$jsonwebtokenVersion")
	// Needed for RSASSA-PSS (PS256, PS384, PS512) algorithms:
	runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.70")

	implementation("io.ktor:ktor-client-core:$ktorVersion")
	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-serialization:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

	implementation("io.insert-koin:koin-core:$koinVersion")

	implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.1.0") {
		exclude(module = "webhook")
		exclude(module = "echo")
		exclude(module = "dispatcher")
		exclude(module = "polls")
	}

	// test implementations:
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")

	testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

	testImplementation("com.natpryce:hamkrest:1.8.0.1")

	testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

	testImplementation("io.ktor:ktor-client-logging:$ktorVersion")
	testImplementation("ch.qos.logback:logback-classic:1.4.8")
}

java.targetCompatibility = JavaVersion.VERSION_17

val main: SourceSet by sourceSets.named("main")

val e2eTest: SourceSet by sourceSets.creating {
	kotlin {
		compileClasspath += main.output + configurations.testRuntimeClasspath.get()
		runtimeClasspath += compileClasspath
	}
	java.srcDir("src/e2eTest/kotlin")
	resources.srcDir("src/main/resources")
}

val runE2eTests by tasks.creating(Test::class) {
	description = "Runs the E2E tests"
	group = "verification"
	testClassesDirs = e2eTest.output.classesDirs
	classpath = e2eTest.runtimeClasspath
}

// TODO for some reason this task doesn't work, it creates a jar without project files
val jarWithDependencies by tasks.creating(Jar::class) {
	description = "Jar with all dependencies"
	group = "build"
	archiveBaseName.set(archiveBaseName.get() + "-with-dependencies")
	configurations.compileClasspath.get().forEach { file: File ->
		from(zipTree(file.absoluteFile)) {
			exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
		}
	}
}

tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	// TODO consider excluding the template resource file.
	manifest {
		attributes["Main-Class"] = "de.lehrbaum.bot.translate.MainKt"
	}
	configurations.runtimeClasspath.get().forEach { file: File ->
		from(zipTree(file.absoluteFile)) {
			exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named<Sync>("installDist") {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// this only really works locally for now because of the target platform
tasks.register("dockerImage", Exec::class) {
	group = "distribution"
	dependsOn("installDist")
	commandLine("docker", "build", "-t", "translate_bot_kotlin", ".")
	doLast {
		commandLine("docker", "save", "-o", "$buildDir/translate_bot_kotlin.tar", "translate_bot_kotlin")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "17"
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
	rejectVersionIf {
		isNonStable(candidate.version) && !isNonStable(currentVersion)
	}
}

fun isNonStable(version: String): Boolean {
	val stableKeyword = listOf("RELEASE", "FINAL", "GA")
		.any { version.uppercase(Locale.getDefault()).contains(it) }
	val regex = "^[0-9,.v-]+(-r)?$".toRegex()
	val isStable = stableKeyword || regex.matches(version)
	return isStable.not()
}

