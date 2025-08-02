plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
