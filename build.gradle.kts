import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val koinVersion: String by project
val junitVersion: String by project

plugins {
	application
	kotlin("jvm") version "1.4.30"
	id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

group = "de.lehrbaum"
version = "1.0-SNAPSHOT"

application {
	mainClass.set("de.lehrbaum.bot.translate.MainKt")
}

repositories {
	mavenCentral()
	jcenter()
	maven(url = "https://jitpack.io")
}

dependencies {
	implementation("com.sksamuel.hoplite:hoplite-core:+")
	implementation("com.sksamuel.hoplite:hoplite-yaml:+")

	implementation("io.ktor:ktor-client-core:+")
	implementation("io.ktor:ktor-client-cio:+")

	implementation("org.koin:koin-core:$koinVersion")

	implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:+")

	testImplementation("org.koin:koin-test:$koinVersion")

	testImplementation(kotlin("test-junit5"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
	kotlinOptions.jvmTarget = "13"
}
