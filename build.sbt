enablePlugins(ScalaJSBundlerPlugin)

name := "epub-images-viewer"

scalaVersion := "2.13.5"

scalacOptions ++= Seq(
  "-Ymacro-annotations",
  "-language:reflectiveCalls"
)

libraryDependencies ++= Seq(
  "me.shadaj" %%% "slinky-web" % "0.6.7",
  "me.shadaj" %%% "slinky-hot" % "0.6.7",
  "org.scala-js" %%% "scalajs-dom" % "1.1.0",
  "dev.zio" %%% "zio" % "1.0.5",
  "dev.zio" %%% "zio-interop-cats" % "2.3.1.0",
  "io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
  "org.typelevel" %%% "cats-core" % "2.4.2",
  "org.typelevel" %%% "cats-effect" % "2.3.3",
  "org.scalatest" %%% "scalatest" % "3.2.6" % "test"
)

npmDependencies in Compile ++= Seq(
  "react" -> "17.0.1",
  "react-dom" -> "17.0.1",
  "react-proxy" -> "1.1.8",
  "file-loader" -> "6.2.0",
  "style-loader" -> "2.0.0",
  "css-loader" -> "5.1.1",
  "html-webpack-plugin" -> "4.5.2",
  "copy-webpack-plugin" -> "6.4.1",
  "webpack-merge" -> "5.7.3",
  "bootstrap" -> "4.6.0",
  "libarchive.js" -> "1.3.0"
)

version in webpack := "4.46.0"
version in startWebpackDevServer := "3.11.2"

webpackResources := baseDirectory.value / "webpack" * "*"

webpackConfigFile in fastOptJS := Some(
  baseDirectory.value / "webpack" / "webpack-fastopt.config.js"
)
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js")
webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js")

webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot")
webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly()

requireJsDomEnv in Test := true

addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS")
addCommandAlias("build", "fullOptJS::webpack")
