lazy val mixQLCore = project
  .in(file("mixql-core"))
  
lazy val mixQLProtobuf = project
  .in(file("mixql-protobuf")).dependsOn(mixQLCore)

lazy val mixQLCluster = project
  .in(file("mixql-cluster")).dependsOn(mixQLProtobuf)

lazy val cleanAll = taskKey[Unit]("Stage all projects")

cleanAll := {
  (mixQLCluster / clean).value
  (mixQLProtobuf / clean).value
  (mixQLCore / clean).value
}

lazy val buildAll = taskKey[Unit]("Build all projects")
buildAll := {
  (mixQLCluster / Compile / packageBin).value
  (mixQLProtobuf / Compile / packageBin).value
  (mixQLCore / Compile / packageBin).value
}