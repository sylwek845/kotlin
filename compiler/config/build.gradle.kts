plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":core:metadata"))
    api(project(":core:compiler.common"))
    api(project(":compiler:util"))
    compile(project(":compiler:cli-config-base"))
    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
