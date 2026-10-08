plugins {
    `java-library`
}

dependencies {
    implementation(project(":nexoria-core"))
    compileOnly("redis.clients:jedis:5.1.0")
}
