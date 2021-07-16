package com.exasol.cloudetl.sbt

import sbt.{ExclusionRule, _}
import sbt.librarymanagement.InclExclRule

/** A list of required dependencies */
object Dependencies {

  // Runtime dependencies versions
  private val ImportExportUDFVersion = "0.2.0"
  private val ParquetIOVersion = "1.0.2"
  private val HadoopVersion = "3.3.1"
  private val DeltaVersion = "0.7.0"
  private val OrcVersion = "1.6.9"
  private val GoogleStorageVersion = "1.9.4-hadoop3"
  private val SparkSQLVersion = "3.0.1"
  private val AlluxioCoreHDFSVersion = "2.6.0"
  private val AkkaStreamVersion = "2.6.15"
  private val AkkaStreamMonVersion = "0.1.0"

  // Test dependencies versions
  private val ScalaTestVersion = "3.2.9"
  private val ScalaTestPlusVersion = "1.0.0-M2"
  private val MockitoCoreVersion = "3.11.2"
  private val HamcrestVersion = "2.2"
  private val ExasolHamcrestMatcherVersion = "1.4.0"
  private val ExasolTestDBBuilderVersion = "3.2.0"
  private val ExasolTestContainersVersion = "3.5.3"
  private val TestContainersLocalstackVersion = "1.15.3"
  private val TestContainersScalaVersion = "0.39.5"

  val Resolvers: Seq[Resolver] = Seq(
    "Exasol Releases" at "https://maven.exasol.com/artifactory/exasol-releases"
  )

  lazy val StorageDependencies: Seq[ModuleID] = Seq(
    "org.apache.commons" % "commons-lang3" % "3.12.0",
    "com.google.guava" % "guava" % "30.1.1-jre",
    "com.exasol" %% "import-export-udf-common-scala" % ImportExportUDFVersion
      exclude ("org.slf4j", "slf4j-simple"),
    "org.apache.hadoop" % "hadoop-common" % HadoopVersion
      exclude ("com.google.guava", "guava")
      exclude ("org.apache.commons", "commons-text")
      exclude ("org.apache.commons", "commons-lang3")
      exclude ("org.apache.avro", "avro"),
    "org.apache.hadoop" % "hadoop-aws" % HadoopVersion,
    "org.apache.hadoop" % "hadoop-azure" % HadoopVersion
      exclude ("org.slf4j", "slf4j-api")
      exclude ("commons-logging", "commons-logging")
      exclude ("com.fasterxml.jackson.core", "jackson-core")
      exclude ("com.microsoft.azure", "azure-keyvault-core"),
    "org.apache.hadoop" % "hadoop-azure-datalake" % HadoopVersion
      exclude ("org.slf4j", "slf4j-api")
      exclude ("com.fasterxml.jackson.core", "jackson-core"),
    "org.apache.hadoop" % "hadoop-hdfs" % HadoopVersion
      exclude ("io.netty", "netty")
      exclude ("commons-logging", "commons-logging")
      exclude ("com.google.protobuf", "protobuf-java"),
    "org.alluxio" % "alluxio-core-client-hdfs" % AlluxioCoreHDFSVersion
      exclude ("com.google.guava", "guava")
      exclude ("commons-logging", "commons-logging")
      exclude ("io.grpc", "grpc-netty")
      exclude ("io.netty", "netty-handler")
      exclude ("io.netty", "netty-transport-native-epoll")
      exclude ("org.apache.logging.log4j", "log4j-api")
      exclude ("org.apache.logging.log4j", "log4j-slf4j-impl")
      exclude ("org.apache.commons", "commons-lang3")
      exclude ("org.apache.hadoop", "hadoop-client"),
    "io.grpc" % "grpc-netty" % "1.39.0",
    "com.google.cloud.bigdataoss" % "gcs-connector" % GoogleStorageVersion
      exclude ("com.google.guava", "guava")
      exclude ("org.apache.httpcomponents", "httpclient"),
    "org.apache.orc" % "orc-core" % OrcVersion
      exclude ("org.slf4j", "slf4j-api")
      exclude ("org.apache.commons", "commons-lang3")
      exclude ("com.google.protobuf", "protobuf-java")
      exclude ("javax.xml.bind", "jaxb-api"),
    "io.delta" %% "delta-core" % DeltaVersion,
    "org.apache.spark" %% "spark-sql" % SparkSQLVersion
      exclude ("org.apache.hadoop", "hadoop-client")
      exclude ("org.apache.parquet", "parquet-hadoop")
      exclude ("javax.activation", "activation")
      exclude ("com.fasterxml.jackson.core", "jackson-annotations")
      exclude ("com.fasterxml.jackson.core", "jackson-core")
      exclude ("com.fasterxml.jackson.core", "jackson-databind")
      excludeAll (
        ExclusionRule(organization = "org.apache.arrow"),
        ExclusionRule(organization = "org.apache.avro"),
        ExclusionRule(organization = "org.apache.curator"),
        ExclusionRule(organization = "org.apache.orc"),
        ExclusionRule(organization = "org.apache.zookeeper")
      ),
    "com.exasol" % "parquet-io-java" % ParquetIOVersion
      exclude ("org.slf4j", "slf4j-api")
      exclude ("org.slf4j", "slf4j-log4j12")
      exclude ("commons-cli", "commons-cli")
      exclude ("commons-codec", "commons-codec")
      exclude ("commons-logging", "commons-logging")
      exclude ("javax.annotation", "javax.annotation-api")
      exclude ("org.apache.commons", "commons-compress")
      exclude ("org.apache.commons", "commons-lang3")
      exclude ("org.apache.hadoop", "hadoop-common")
      exclude ("org.apache.logging.log4j", "log4j-slf4j-impl")
      exclude ("com.google.code.findbugs", "jsr305")
      exclude ("com.fasterxml.jackson.core", "jackson-core")
      exclude ("org.apache.avro", "avro")
      exclude ("org.apache.hadoop", "hadoop-yarn-api")
      exclude ("org.apache.hadoop", "hadoop-yarn-client")
      exclude ("org.apache.hadoop", "hadoop-yarn-common")
      exclude ("org.xerial.snappy", "snappy-java")
      exclude ("com.fasterxml.jackson.core", "jackson-core")
      exclude ("com.fasterxml.jackson.core", "jackson-databind")
      excludeAll (
        ExclusionRule(organization = "org.eclipse.jetty"),
        ExclusionRule(organization = "org.apache.kerby"),
        ExclusionRule(organization = "org.apache.curator"),
        ExclusionRule(organization = "org.apache.zookeeper")
      ),
    "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
    "net.ruippeixotog" %% "akka-stream-mon" % AkkaStreamMonVersion,
    // Logging Dependencies
    "org.slf4j" % "slf4j-log4j12" % "1.7.31",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
  )

  lazy val TestDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion,
    "org.scalatestplus" %% "scalatestplus-mockito" % ScalaTestPlusVersion,
    "org.mockito" % "mockito-core" % MockitoCoreVersion,
    "com.exasol" % "exasol-testcontainers" % ExasolTestContainersVersion,
    "com.exasol" % "test-db-builder-java" % ExasolTestDBBuilderVersion,
    "com.exasol" % "hamcrest-resultset-matcher" % ExasolHamcrestMatcherVersion,
    "org.hamcrest" % "hamcrest" % HamcrestVersion,
    "com.dimafeng" %% "testcontainers-scala-scalatest" % TestContainersScalaVersion,
    "org.testcontainers" % "localstack" % TestContainersLocalstackVersion
  ).map(_ % Test)

  lazy val ExcludedDependencies: Seq[InclExclRule] = Seq(
    ExclusionRule("org.ow2.asm", "asm"),
    ExclusionRule("javax.ws.rs", "jsr311-api"),
    ExclusionRule("com.sun.jersey", "jersey-core"),
    ExclusionRule("com.sun.jersey", "jersey-server"),
    ExclusionRule("com.sun.jersey", "jersey-json"),
    ExclusionRule("javax.servlet", "servlet-api"),
    ExclusionRule("javax.servlet.jsp", "jsp-api"),
    ExclusionRule("org.openjfx", "javafx.base"),
    ExclusionRule("org.apache.logging.log4j", "log4j-core"),
    ExclusionRule("org.slf4j", "slf4j-simple")
  )

}
