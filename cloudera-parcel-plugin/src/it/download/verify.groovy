File parcel = new File( basedir, "../../local-repo/com/cloudera/parcel/download/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel" );
File parcelSha1 = new File( basedir, "../../local-repo/com/cloudera/parcel/download/SQOOP_TERADATA_CONNECTOR/1.4c5/SQOOP_TERADATA_CONNECTOR-1.4c5-el6.parcel.sha1" );

assert parcel.isFile()
assert parcelSha1.isFile()

return true;
