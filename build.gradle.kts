// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

dependencies {
    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Material 3 Extended Icons
    implementation("androidx.compose.material:material-icons-extended")
}