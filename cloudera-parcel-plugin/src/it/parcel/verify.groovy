File parcelPrepare = new File( basedir, "target/parcel/SQOOP_TERADATA_CONNECTOR-1.4c5/README" );
File parcelBuild = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelBuildSha1 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );
File parcelInstall = new File( basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelSha1Install = new File( basedir, "../../local-repo/com/cloudera/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );

assert parcelPrepare.isFile()
assert parcelBuild.isFile()
assert parcelBuildSha1.isFile()
assert parcelInstall.isFile()
assert parcelSha1Install.isFile()

return true;
