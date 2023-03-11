# mixql-platform
Main mixql platform

# How to use mixql-platform-demo:

1. build archive using command `sbt archiveMixQLPlatformDemo`
2. find archive in mixql-platform-demo/target/universal
3. unpack bin and lib from archive to for example /home/mihan/opt/mixql-platform-demo-0.1
4. set system variables in .bashrc or terminal
* export MIXQL_CLUSTER_BASE_PATH=/home/mihan/opt/mixql-platform-demo-0.1/bin
* export PATH="$HOME/opt/mixql-platform-demo-0.1/bin:$PATH"
5. run
mixql-platform-demo -Dmixql.org.engine.sqlight.db.path="jdbc:sqlite:/home/mihan/opt/mixql-platform-demo-0.1/mixql-org-sqlight-db.db" --sql-file test_sql1.sql

where mixql.org.engine.sqlight.db.path is path to sqlite database(will be created if does not exist)

# For developers
## Publish
set version of mixql-platform in build.sbt in inThisBuild section, also groupId, publishTo, credentials can be changed
if needed.

Publish jars: `sbt publish`

Publish also archives: `sbt Universal / publish`