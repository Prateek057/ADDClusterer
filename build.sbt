name := """Workbench4LSDD-Service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

lazy val webJarsPlay = file("..").getAbsoluteFile.toURI

lazy val globalResources = file("myresources")
classpathTypes += "maven-plugin"


libraryDependencies ++= Seq(
  filters,
  javaJdbc,
  cache,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "jquery" % "3.2.1",
  "org.webjars" % "bootstrap" % "3.3.6",
  "org.webjars" % "materializecss" % "0.100.2",
  "org.webjars" % "html5shiv" % "3.7.3",
  "org.mongodb.morphia" % "morphia" % "1.2.1",
  "org.webjars.bower" % "angular" % "1.6.1",
  "org.webjars.bower" % "ngstorage" % "0.3.11",
  "org.webjars.bower" % "angular-resource" % "1.6.1",
  "org.webjars.bower" % "angular-checklist-model" % "0.10.0",
  "org.webjars.bower" % "angular-route" % "1.6.1",
  "org.webjars.bower" % "angular-material" % "1.1.4",
  "org.webjars" % "d3js" % "3.5.17",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.8.1",
  "nz.ac.waikato.cms.weka" % "LibSVM" % "1.0.10",
  "org.codehaus.janino" % "janino" % "3.0.7",
/*  "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1",
  "org.deeplearning4j" % "deeplearning4j-nlp" % "0.9.1",
  "org.nd4j" % "nd4j-backends" % "0.9.1",
  "com.rapidminer.studio" % "rapidminer-studio-core" % "7.3.0",*/
  "org.apache.spark" %% "spark-core" % "2.2.0",
  "org.apache.spark" %% "spark-sql" % "2.2.0",
  "org.apache.spark" %% "spark-streaming" % "2.2.0",
  "org.apache.spark" %% "spark-mllib" % "2.2.0",
  "org.apache.hadoop" % "hadoop-client" % "2.7.2"

)

/*libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "0.9.1" classifier "windows-x86_64"*/

/*resolvers += "rapidminer-studio-core" at "https://maven.rapidminer.com/content/groups/public/"*/

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
  "nz.ac.waikato.cms.weka" % "weka-dev" % "3.9.1",
  "org.webjars" % "jquery" % "3.2.1"
)

unmanagedResourceDirectories in (Compile, runMain) <+=  baseDirectory ( _ /"../myresources")

routesGenerator := InjectedRoutesGenerator

