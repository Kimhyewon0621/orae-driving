plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

// 안드로이드 앱이 이 모듈을 그대로 쓸 수 있도록 Java 17 바이트코드로 컴파일합니다.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

tasks.test {
    useJUnitPlatform()
    // 시나리오 파일 위치를 테스트에 알려줍니다.
    systemProperty("scenarioDir", rootProject.file("scenarios").absolutePath)
}
