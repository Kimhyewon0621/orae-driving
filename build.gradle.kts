// 플러그인 버전은 루트에서 한 번만 선언하고, 각 모듈에서는 버전 없이 적용합니다.
plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
}
