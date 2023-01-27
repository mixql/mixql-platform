lazy val mixQLCore = project
  .in(file("mixql-core"))
  
lazy val mixQLProtobuf = project
  .in(file("mixql-protobuf")).dependsOn(mixQLCore)
