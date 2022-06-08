import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	idea
	kotlin("jvm") version "1.7.0"
	kotlin("plugin.serialization") version "1.7.0"
}

group = "de.lehrbaum"
version = "1.1-SNAPSHOT"

application {
	mainClass.set("de.lehrbaum.bot.translate.MainKt")
}

repositories {
	mavenCentral()
	jcenter()
	maven(url = "https://jitpack.io")
}

dependencies {
	// dependabot does not find the versions in the gradle.properties, but it does here
	val koinVersion = "2.2.2"
	val junitVersion = "5.8.2"
	val jsonwebtokenVersion = "0.11.5"
	val ktorVersion = "2.0.2"
	val kotlinVersion = "1.6.1"

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

	implementation("com.charleskorn.kaml:kaml-jvm:0.44.0")

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

	implementation("org.koin:koin-core:$koinVersion")

	implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.7") {
		exclude(module = "webhook")
		exclude(module = "echo")
		exclude(module = "dispatcher")
		exclude(module = "polls")
	}

	// test implementations:
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinVersion")

	testImplementation("org.koin:koin-test:$koinVersion")

	testImplementation("com.natpryce:hamkrest:1.8.0.1")

	testImplementation(kotlin("test-junit5"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

	testImplementation("io.ktor:ktor-client-logging:$ktorVersion")
	testImplementation("ch.qos.logback:logback-classic:1.2.11")
}

java.targetCompatibility = JavaVersion.VERSION_15

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

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "11"
}
