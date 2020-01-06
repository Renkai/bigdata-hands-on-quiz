/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/5.5.1/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java
    scala
    // Apply the application plugin to add support for building a CLI application
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

val scalaV = "2.12"

dependencies {
    compile("org.scala-lang:scala-library:$scalaV.8")
    compile("org.scala-lang:scala-reflect:$scalaV.8")
    compile("org.json4s","json4s-jackson_$scalaV","3.6.7")
    compile("com.typesafe.play", "play-json_$scalaV", "2.8.0")

    testImplementation("org.scalatest:scalatest_$scalaV:3.0.8")
}

sourceSets {
    //为了实现Java和Scala相互调用,将source set都移动到了scala侧,避免compileJava阶段找不到Scala代码的情况
    main {
        withConvention(ScalaSourceSet::class) {
            scala {
                setSrcDirs(listOf("src/main/scala", "src/main/java"))
            }
        }
        java {
            setSrcDirs(emptyList<String>())
        }
    }
    test {
        withConvention(ScalaSourceSet::class) {
            scala {
                setSrcDirs(listOf("src/test/scala", "src/test/java"))
            }
        }
        java {
            setSrcDirs(emptyList<String>())
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<CreateStartScripts>("startScripts") {
    //让JVM拥有APP_HOME作为环境变量
    classpath = classpath?.plus(configurations.compileOnly.get())
    val myStartScript = File(rootDir, "unixStartScript.sh")
    (unixStartScriptGenerator as TemplateBasedScriptGenerator).template = resources.text.fromFile(myStartScript)
}

application {
    //将conf目录和compile only的jar包放到成品包下
    mainClassName = "App"
    applicationDistribution.from("conf") {
        into("conf")
    }

    applicationDistribution.from(configurations.compileOnly) {
        into("lib")
    }
}

tasks.getByName<JavaExec>("run") {
    classpath += configurations.compileOnly.get()
}

tasks.register<Jar>("fatJar") {
    group = "Build"
    description = "打出一个胖胖的jar / give you a fat fat jar"
    archiveClassifier.set("fat")

    duplicatesStrategy = DuplicatesStrategy.FAIL

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}