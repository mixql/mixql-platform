ThisBuild / scalaVersion := "3.2.1"

lazy val mixQLCore = project
  .in(file("mixql-core"))
  
lazy val mixQLProtobuf = project
  .in(file("mixql-protobuf")).dependsOn(mixQLCore)
  .settings(
    Compile / PB.targets := Seq(
      scalapb.gen(grpc = true) -> {
        //val file = (Compile / sourceManaged).value
        //        println("PB  target: " + file.getPath)
        //        val file = new File("target/scala-3.2.1/src_managed/main")
        val file = (Compile / scalaSource).value / "scalaPB"
        println("PB  target: " + file.getAbsolutePath)
        file
      }
    )
  )

//lazy val mixQLCluster = project
//  .in(file("mixql-cluster")).dependsOn(mixQLProtobuf)
//
//lazy val cleanAll = taskKey[Unit]("Stage all projects")
//
//cleanAll := {
//  (mixQLCluster / clean).value
//  (mixQLProtobuf / clean).value
//  (mixQLCore / clean).value
//}
//
//lazy val buildAll = taskKey[Unit]("Build all projects")
//buildAll := {
//  (mixQLCluster / Compile / packageBin).value
//  (mixQLProtobuf / Compile / packageBin).value
//  (mixQLCore / Compile / packageBin).value
//}