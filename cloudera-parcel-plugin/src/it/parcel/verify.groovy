File parcelDownload = new File(basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel");
File parcelSha1Download = new File(basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1");
File parcelExplodeTarget = new File(basedir, "target/parcel/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE/meta/sqoop_env.sh");
File parcelExplodeRepo = new File(basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5/meta/sqoop_env.sh");
File parcelExplodeLink = new File(basedir, "target/sqoop-connectors/meta/sqoop_env.sh");
File parcelPrepare = new File(basedir, "target/parcel/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE/README");
File parcelBuild = new File(basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE-el6.parcel");
File parcelBuildSha1 = new File(basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE-el6.parcel.sha1");
File parcelBuildEnv = new File(basedir, "target/parcel.env");
File parcelBuildEnvParcel = new File(basedir, "target/parcel/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE/meta/parcel.env");
File parcelBuildManifest = new File(basedir, "target/manifest.json");
File parcelInstall = new File(basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5.BESPOKE/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE-el6.parcel");
File parcelSha1Install = new File(basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5.BESPOKE/SQOOP_TERADATA_CONNECTOR-1.4c5.BESPOKE-el6.parcel.sha1");

assert parcelDownload.isFile()
assert parcelSha1Download.isFile()
assert parcelExplodeTarget.isFile()
assert parcelExplodeRepo.isFile()
assert parcelExplodeLink.isFile()
assert parcelPrepare.isFile()
assert parcelBuild.isFile()
assert parcelBuildSha1.isFile()
assert parcelBuildEnv.isFile()
assert parcelBuildEnvParcel.isFile()
assert parcelBuildManifest.isFile()
assert parcelInstall.isFile()
assert parcelSha1Install.isFile()

return true;
