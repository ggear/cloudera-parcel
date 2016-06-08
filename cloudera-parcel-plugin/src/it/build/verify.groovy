File parcel = new File( basedir, "../../local-repo/com/cloudera/parcel/build/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelSha1 = new File( basedir, "../../local-repo/com/cloudera/parcel/build/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );
File parcelBuild = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelBuildSha1 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );

assert parcel.isFile()
assert parcelSha1.isFile()
assert parcelBuild.isFile()
assert parcelBuildSha1.isFile()

return true;
