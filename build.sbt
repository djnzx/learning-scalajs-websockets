import org.scalajs.linker.interface.ModuleKind
import sbt.Keys.scalaVersion
import scala.collection.Seq

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "3.8.3"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "org.djnzx"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

lazy val circeVersion = "0.14.15"
lazy val http4sVersion = "0.23.34"

lazy val common = (crossProject(JVMPlatform, JSPlatform) in file("common"))
  .settings(
    name := "common"
  )
  .jvmSettings()
  .jsSettings()

lazy val back = (project in file("back"))
  .dependsOn(common.jvm)
  .settings(
    name := "back",
    libraryDependencies ++= Seq(
      "co.fs2"                %% "fs2-io"                        % "3.13.0",
      "org.http4s"            %% "http4s-ember-server"           % http4sVersion,
      "org.http4s"            %% "http4s-ember-client"           % http4sVersion,
      "org.http4s"            %% "http4s-dsl"                    % http4sVersion,
      "org.http4s"            %% "http4s-circe"                  % http4sVersion,
      "io.circe"              %% "circe-parser"                  % circeVersion,
      "io.circe"              %% "circe-generic"                 % circeVersion,
      "com.github.pureconfig" %% "pureconfig-core"               % "0.17.10",
      // logging machinery
      "org.typelevel"         %% "log4cats-slf4j"                % "2.8.0",
      "ch.qos.logback"         % "logback-classic"               % "1.5.32",
      // tests
      "com.lihaoyi"           %% "pprint"                        % "0.9.6",
      "org.scalatest"         %% "scalatest"                     % "3.2.20"   % Test,
      "org.scalacheck"        %% "scalacheck"                    % "1.19.0"   % Test,
      "org.scalatestplus"     %% "scalacheck-1-19"               % "3.2.20.0" % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest" % "1.8.0"    % Test,
      "org.testcontainers"     % "testcontainers"                % "2.0.5"    % Test,
      "org.testcontainers"     % "testcontainers-postgresql"     % "2.0.5"    % Test,
    ),
    Compile / mainClass := Some("org.djnzx.BackendLauncher"),
    Compile / run / fork := true
  )

lazy val front = (project in file("front"))
  .dependsOn(common.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "front",
    scalacOptions ++= Seq(),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "tyrian-io"     % "0.14.0",
      "com.armanbilge"  %%% "fs2-dom"       % "0.3.0-M1", // window.history wrapper
      "org.http4s"      %%% "http4s-dom"    % "0.2.12",
      "io.circe"        %%% "circe-parser"  % circeVersion,
      "io.circe"        %%% "circe-generic" % circeVersion
    )
  )
