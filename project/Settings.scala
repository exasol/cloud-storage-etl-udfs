package com.exasol.cloudetl.sbt

import sbt._
import sbt.Keys._

import sbtassembly.MergeStrategy
import sbtassembly.PathList
import sbtassembly.AssemblyPlugin.autoImport._

import scoverage.ScoverageSbtPlugin.autoImport._
import org.scalastyle.sbt.ScalastylePlugin.autoImport._
import wartremover.WartRemover.autoImport.wartremoverErrors

/** A list of (boilerplate) settings for build process */
object Settings {

  def projectSettings(scalaVersion: SettingKey[String]): Seq[Setting[_]] =
    buildSettings(scalaVersion) ++ miscSettings ++ scalaStyleSettings ++ assemblySettings

  def buildSettings(scalaVersion: SettingKey[String]): Seq[Setting[_]] = Seq(
    // Compiler settings
    scalacOptions ++= Compilation.compilerFlagsFn(scalaVersion.value),
    Compile / console / scalacOptions := Compilation.consoleFlagsFn(scalaVersion.value),
    javacOptions ++= Compilation.JavacCompilerFlags,
    Compile / compileOrder := CompileOrder.JavaThenScala
  )

  def miscSettings(): Seq[Setting[_]] = Seq(
    // Wartremover settings
    Compile / compile / wartremoverErrors := Compilation.WartremoverFlags,
    Test / compile / wartremoverErrors := Compilation.WartremoverTestFlags,
    // General settings
    Global / cancelable := true,
    // Scoverage settings
    coverageOutputHTML := true,
    coverageOutputXML := true,
    coverageOutputCobertura := true,
    coverageFailOnMinimum := false
  )

  /** Creates a Scalastyle tasks that run with unit and integration tests. */
  def scalaStyleSettings(): Seq[Setting[_]] = {
    lazy val mainScalastyle = taskKey[Unit]("mainScalastyle")
    lazy val testScalastyle = taskKey[Unit]("testScalastyle")
    Seq(
      scalastyleFailOnError := true,
      Compile / scalastyleConfig := (ThisBuild / baseDirectory).value / "project" / "scalastyle-config.xml",
      Test / scalastyleConfig := (ThisBuild / baseDirectory).value / "project" / "scalastyle-test-config.xml",
      mainScalastyle := (Compile / scalastyle).toTask("").value,
      testScalastyle := (Compile / scalastyle).toTask("").value,
      Test / test := (Test / test).dependsOn(mainScalastyle).value,
      Test / test := (Test / test).dependsOn(testScalastyle).value
    )
  }

  /**
   * Creates settings for integration tests.
   *
   * Use only when [[IntegrationTestPlugin]] is enabled.
   */
  def integrationTestSettings(): Seq[Setting[_]] = {
    lazy val mainScalastyle = taskKey[Unit]("mainScalastyle")
    lazy val itTestScalastyle = taskKey[Unit]("itTestScalastyle")
    Seq(
      IntegrationTest / scalastyleConfig := (Test / scalastyleConfig).value,
      IntegrationTest / scalastyleSources := Seq((IntegrationTest / scalaSource).value),
      mainScalastyle := (Compile / scalastyle).toTask("").value,
      itTestScalastyle := (IntegrationTest / scalastyle).toTask("").value,
      IntegrationTest / test := (IntegrationTest / test).dependsOn(mainScalastyle).value,
      IntegrationTest / test := (IntegrationTest / test).dependsOn(itTestScalastyle).value
    )
  }

  def assemblySettings(): Seq[Setting[_]] = Seq(
    assembly / test := {},
    assembly / logLevel := Level.Info,
    assembly / assemblyJarName := moduleName.value + "-" + version.value + ".jar",
    assembly / assemblyMergeStrategy ~= { defaultStrategy =>
      {
        case "META-INF/services/io.grpc.LoadBalancerProvider" => MergeStrategy.concat
        case "META-INF/services/io.grpc.NameResolverProvider" => MergeStrategy.concat
        case "reference.conf"                                 => MergeStrategy.concat
        case "log4j.properties"                               => MergeStrategy.last
        case x if x.endsWith("reflection-config.json")        => MergeStrategy.rename
        case x if x.endsWith(".txt")                          => MergeStrategy.rename
        case x if x.endsWith(".properties")                   => MergeStrategy.filterDistinctLines
        case s if s.endsWith(".proto")                        => MergeStrategy.last
        case x if x.endsWith(".class")                        => MergeStrategy.last
        case x                                                => defaultStrategy(x)
      }
    },
    assembly / assemblyExcludedJars := {
      val cp = (assembly / fullClasspath).value
      val exludeSet = Set.empty[String]
      cp.filter { jar =>
        exludeSet(jar.data.getName)
      }
    }
  )

}
