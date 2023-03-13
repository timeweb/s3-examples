plugins {
    application
    kotlin("jvm") version "1.7.20"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${properties["version.kotlinx-coroutines"]}")
    implementation("aws.sdk.kotlin:s3:${properties["version.aws-sdk-kotlin-s3"]}")
    implementation("ch.qos.logback:logback-classic:${properties["version.logback"]}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
