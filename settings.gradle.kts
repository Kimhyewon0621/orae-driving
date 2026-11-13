// 오래운전 루트 설정
// - core : 안드로이드와 무관한 순수 Kotlin 모듈 (데이터 구조, 엔진, 시나리오 재생기)
// - app  : Pleos 공식 튜토리얼 프로젝트를 가져와 붙일 안드로이드 앱 모듈 (app/README.md 참고)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "orae-driving"
include(":core")
// app 모듈을 가져온 뒤 아래 줄의 주석을 해제하세요.
// include(":app")
