name := "pathmatcher"

description := "Path matcher"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/eugenemedvediev/pathparser</url>
    <licenses>
      <license>
        <name>GPLv3</name>
        <url>http://www.gnu.org/licenses/gpl-3.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:eugenemedvediev/pathparser.git</url>
      <connection>scm:git:git@github.com:eugenemedvediev/pathparser.git</connection>
    </scm>
    <developers>
      <developer>
        <id>eugenemedvediev</id>
        <name>eugene.medvediev@gmail.com</name>
        <url>https://github.com/eugenemedvediev</url>
      </developer>
    </developers>
  )

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}