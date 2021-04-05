import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	idea
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.serialization") version "1.4.32"
}

group = "de.lehrbaum"
version = "1.0"

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
	val junitVersion = "5.7.1"
	val jsonwebtokenVersion = "0.11.2"
	val ktorVersion = "1.5.3"
	val kotlinVersion = "1.4.3-native-mt"
	val hopliteVersion = "1.4.0"

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

	implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
	implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")

	implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-gson:$jsonwebtokenVersion")
	// Needed for RSASSA-PSS (PS256, PS384, PS512) algorithms:
	runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.68")

	// TODO fix version conflict with telegram library and update to latest version
	//  then use kotlinx serialization to prevent problems with kotlin specific features
	implementation("io.ktor:ktor-client-core:$ktorVersion")
	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-serialization:$ktorVersion")
	implementation("io.ktor:ktor-client-logging:$ktorVersion")

	implementation("ch.qos.logback:logback-classic:1.2.3")

	implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")

	implementation("org.koin:koin-core:$koinVersion")

	implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.4") {
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

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "13"
}
