import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val koinVersion: String by project
val junitVersion: String by project
val jsonwebtokenVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project
val hopliteVersion: String by project

plugins {
	application
	idea
	kotlin("jvm") version "1.4.30"
	kotlin("plugin.serialization") version "1.4.30"
	// id("org.jlleitschuh.gradle.ktlint") version "10.0.0" // still some Problems with tab indent
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
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

	implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
	implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")

	implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
	runtimeOnly("io.jsonwebtoken:jjwt-gson:$jsonwebtokenVersion") // or 'io.jsonwebtoken:jjwt-jackson:0.11.2'
	// Needed for RSASSA-PSS (PS256, PS384, PS512) algorithms:
	runtimeOnly("org.bouncycastle:bcprov-jdk15on:+")

	// TODO fix version conflict with telegram library and update to latest version
	//  then use kotlinx serialization to prevent problems with kotlin specific features
	implementation("io.ktor:ktor-client-core:$ktorVersion")
	implementation("io.ktor:ktor-client-cio:$ktorVersion")
	implementation("io.ktor:ktor-client-gson:$ktorVersion")

	implementation("org.koin:koin-core:$koinVersion")

	implementation("com.github.kotlin-telegram-bot:kotlin-telegram-bot:+")

	// test implementations:
	testImplementation("org.koin:koin-test:$koinVersion")

	testImplementation(kotlin("test-junit5"))
	testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
	kotlinOptions.jvmTarget = "13"
}

java.targetCompatibility = JavaVersion.VERSION_15

val e2eTest: SourceSet by sourceSets.creating

e2eTest.apply {
	java.srcDir("src/e2eTest/kotlin")
	idea.module.testSourceDirs.addAll(java.srcDirs)
	resources.srcDir("src/main/resources")
}
