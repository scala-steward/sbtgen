import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

turbo in ThisBuild := true
classLoaderLayeringStrategy in ThisBuild := ClassLoaderLayeringStrategy.ScalaLibrary

name := "izumi-sbtgen"
organization in ThisBuild := "io.7mind.izumi.sbt"

publishMavenStyle in ThisBuild := true
homepage in ThisBuild := Some(url("https://izumi.7mind.io"))
licenses in ThisBuild := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))
developers in ThisBuild := List(
  Developer(id = "7mind", name = "Septimal Mind", url = url("https://github.com/7mind"), email = "team@7mind.io"),
)
scmInfo in ThisBuild := Some(ScmInfo(url("https://github.com/7mind/sbtgen"), "scm:git:https://github.com/7mind/sbtgen.git"))
credentials in ThisBuild += Credentials(file(".secrets/credentials.sonatype-nexus.properties"))

sonatypeProfileName := "io.7mind"
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  runClean, // : ReleaseStep
  runTest, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  //publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)

publishTo in ThisBuild := (if (!isSnapshot.value) {
  sonatypePublishToBundle.value
} else {
  Some(Opts.resolver.sonatypeSnapshots)
})


lazy val sbtgen = (project in file("sbtgen"))
  .settings(
    crossScalaVersions := Seq(ScalaVersions.scala_213, ScalaVersions.scala_212),
    scalaVersion := crossScalaVersions.value.head,
    //    libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2",
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.1",
    libraryDependencies += "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
    libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  )

lazy val `sbt-izumi-deps` = (project in file("sbt/sbt-izumi-deps"))
  .settings(
    crossSbtVersions := Seq(sbtVersion.value),
    crossScalaVersions := Seq(ScalaVersions.scala_212),
    scalaVersion := crossScalaVersions.value.head,
    sbtPlugin := true,
  )

lazy val `sbt-izumi` = (project in file("sbt/sbt-izumi"))
  .settings(
    crossScalaVersions := Seq(ScalaVersions.scala_212),
    scalaVersion := crossScalaVersions.value.head,
    crossSbtVersions := Seq(sbtVersion.value),
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "io.get-coursier" %% "coursier" % "2.0.0-RC3-3",
    ),
    sbtPlugin := true,

    // https://github.com/scoverage/sbt-scoverage
    addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0"),

    // http://www.scala-sbt.org/sbt-pgp/
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0-M2"),

    // https://github.com/sbt/sbt-git
    addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0"),

    // http://www.scalastyle.org/sbt.html
    addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0"),

    // https://github.com/orrsella/sbt-stats
    addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7"),

    // https://github.com/xerial/sbt-sonatype
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.7"),

    // https://github.com/sbt/sbt-release
    addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11"),

    // https://github.com/jrudolph/sbt-dependency-graph
    addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2"),

    // https://github.com/sbt/sbt-duplicates-finder
    addSbtPlugin("org.scala-sbt" % "sbt-duplicates-finder" % "0.8.1"),

  )

lazy val `sbt-tests` = (project in file("sbt/sbt-tests"))
  .dependsOn(`sbt-izumi`, `sbt-izumi-deps`)
  .enablePlugins(ScriptedPlugin)
  .settings(
    crossSbtVersions := Seq(sbtVersion.value),
    crossScalaVersions := Seq(ScalaVersions.scala_212),
    scalaVersion := crossScalaVersions.value.head,
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "sbt" % sbtVersion.value
    ),
    skip in publish := true,
    scriptedLaunchOpts := {
      Seq(
        scriptedLaunchOpts.value,
        Seq(
          "-Xmx1024M",
          "-Dplugin.version=" + version.value,
        ),
        Option(System.getProperty("sbt.ivy.home"))
          .toSeq
          .flatMap(value =>  Seq(s"-Dsbt.ivy.home=$value", s"-Divy.home=$value")),
      ).flatten,
    },
    scriptedBufferLog := false,

  )

lazy val `sbtgen-root` = (project in file("."))
  .aggregate(
    sbtgen,
    `sbt-izumi-deps`,
    `sbt-izumi`,
  )
  .settings(
    scalaVersion := ScalaVersions.scala_212,
    crossScalaVersions := Nil,
    skip in publish := true,
  )
