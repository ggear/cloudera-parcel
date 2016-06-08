File parcelBuild1 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.1-el6.parcel" );
File parcelBuildSha11 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.1-el6.parcel.sha1" );
File parcelBuild2 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.1-el6.parcel" );
File parcelBuildSha12 = new File( basedir, "target/SQOOP_TERADATA_CONNECTOR-1.4c5.1-el6.parcel.sha1" );

assert parcelBuild1.isFile()
assert parcelBuildSha11.isFile()
assert parcelBuild2.isFile()
assert parcelBuildSha12.isFile()

return true;
