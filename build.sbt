scalaVersion := "3.3.6"

resolvers += Resolver.sonatypeCentralSnapshots

enablePlugins(ScalafixPlugin, SemanticdbPlugin)

inThisBuild(
  List(
    semanticdbEnabled := true
  )
)

scalacOptions ++= Seq(
  "-Wunused:imports",
  "-feature",
  "-language:implicitConversions",
  "-no-indent",
  "-Xmax-inlines",
  "128",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  // scalatest
  "org.scalactic" %% "scalactic" % "3.2.19",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test"
)

lazy val parTestGroup = inputKey[Unit]("Runs a single test group")
parTestGroup := (Def.inputTaskDyn {
  val List(groupId, numberOfGroups) = complete.DefaultParsers
    .spaceDelimited("<arg>")
    .parsed
    .map(_.toInt)

  val allTests = (Test / definedTests).value

  val numberOfTests = allTests.size
  val numberOfTestsPerGroup =
    if (numberOfTests % numberOfGroups == 0) {
      numberOfTests / numberOfGroups
    } else { (numberOfTests / numberOfGroups) + 1 }

  val groups = allTests.grouped(numberOfTestsPerGroup).toArray

  val groupToRun     = groups(groupId - 1)
  val argForTestOnly = " " + groupToRun.map(_.name).mkString(" ")

  streams.value.log.info(s"Running testOnly:$argForTestOnly")

  Def.taskDyn {
    (Test / testOnly).toTask(argForTestOnly)
  }
}).evaluated

lazy val root = project
  .in(file("."))
  .settings()
