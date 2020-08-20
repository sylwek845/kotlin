
import org.gradle.jvm.tasks.Jar

description = "Compiler runner + daemon client"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    compile(project(":kotlin-build-common"))
    compileOnly(project(":daemon-common"))
    compileOnly(project(":daemon-common-new"))
    compile(projectRuntimeJar(":kotlin-daemon-client"))
    compile(commonDep("org.jetbrains.kotlinx", "kotlinx-coroutines-core")) { isTransitive = false }
    //TODO
    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }

}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}

publish()

runtimeJar(rewriteDefaultJarDepsToShadedCompiler())
sourcesJar()
javadocJar()
