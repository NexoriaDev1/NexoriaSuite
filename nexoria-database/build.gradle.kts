plugins {
    `java-library`
}

dependencies {
    implementation(project(":nexoria-core"))
    compileOnly("com.zaxxer:HikariCP:5.1.0")
}
