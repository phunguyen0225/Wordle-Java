plugins {
  java
  jacoco
  pmd
  application
}       

repositories {
	mavenCentral()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
  testImplementation("org.junit.platform:junit-platform-console:1.7.1")
  testImplementation("org.mockito:mockito-core:4.3.1")
}

tasks {
  val flags = listOf("-Xlint:unchecked", "-Xlint:deprecation", "-Werror")

  getByName<JavaCompile>("compileJava") {
    options.compilerArgs = flags
  }

  getByName<JavaCompile>("compileTestJava") {
    options.compilerArgs = flags
  }
}
 
sourceSets {
  main {
    java.srcDirs("wordlegameapp/src")
  }
  test {
    java.srcDirs("wordlegameapp/test")
  }
}

val test by tasks.getting(Test::class) {
	useJUnitPlatform {}
}

pmd {
    ruleSets = listOf()
    ruleSetFiles = files("../conf/pmd/ruleset.xml")
    toolVersion = "6.37.0"    
}                                                

tasks.withType<JacocoReport> {
  afterEvaluate {
    classDirectories.setFrom(files(classDirectories.files.map {
      fileTree(it).apply {
        exclude("*/preview/*")
        exclude("*/ui/*")
      }
    }))
  }
}

application {
  mainClass.set("game.ui.WordleFrame")
}

defaultTasks("clean", "test", "jacocoTestReport", "pmdMain")
