File parcel = new File( basedir, "../../local-repo/com/cloudera/parcel/explode/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelSha1 = new File( basedir, "../../local-repo/com/cloudera/parcel/explode/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );
File parcelExploded = new File( basedir, "../../local-repo/com/cloudera/parcel/explode/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5" );
File parcelExplodedLink = new File( basedir, "target/sqoop-connectors/sqoop-connector-teradata-1.4c5.jar" );


assert parcel.isFile()
assert parcelSha1.isFile()
assert parcelExploded.isDirectory()
assert parcelExplodedLink.isFile()

return true;
