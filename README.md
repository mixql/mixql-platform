# mixql-platform
Main mixql platform

# How to use mixql-platform-demo:

1. build archive using command `sbt archiveMixQLPlatformDemo`
2. find archive in mixql-platform-demo/target/universal
3. unpack archive to for example /home/mihan/opt/mixql-platform-demo
4. set system variables in .bashrc or terminal
* `export MIXQL_PLATFORM_DEMO_HOME_PATH=/home/mihan/opt/mixql-platform-demo/`
* `export PATH="$HOME/opt/mixql-platform-demo/bin:$PATH"`
5. set in application.conf in /home/mihan/opt/mixql-platform-demo
* `mixql.org.engine.sqlight.db.path="jdbc:sqlite:/home/mihan/opt/mixql-platform-demo/mixql-org-sqlight-db.db"`
  where mixql.org.engine.sqlight.db.path is path to sqlite database(will be created if does not exist)
6. run
* `mixql-platform-demo -Dconfig.file=$MIXQL_PLATFORM_DEMO_HOME_PATH/application.conf --sql-file test_sql1.sql`



# For developers
## Publish
set version of mixql-platform in build.sbt in inThisBuild section, also groupId, publishTo, credentials can be changed
if needed.

Publish jars: `sbt publish`

Publish also archives: `sbt Universal / publish`