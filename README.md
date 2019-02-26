# Cloudera Parcel

A module to help mananage Cloudera Parcels, including a Maven plugin to bridge between JAR and Parcel artifacts and their repositories, giving the ability to affect the full lifecycle of parcel operations:

* Build
* Install
* Deploy
* Download
* Explode

Additional modules are included to provide the meta data to drive parcel builds for:

* CDH OS-X
* Kudu OS-X
* Impala OS-X

## Requirements

To compile, build and package from source, this project requires:

* JDK 1.7
* Maven 3

## Install

This project can be installed to a local repository as per:

```bash
git clone git@github.com:ggear/cloudera-parcel.git
cd cloudera-parcel
mvn install -PCMP
cd cloudera-parcel-repository
mvn install
cd ..
```

## Usage

The plugin can be used as per the [integration tests](https://github.com/ggear/cloudera-parcel/tree/master/cloudera-parcel-plugin/src/it), the embedded [example](https://github.com/ggear/cloudera-parcel/tree/master/cloudera-parcel-repository/cloudera-parcel-repository-sqoop-teradata) and [cloudera-framework](https://github.com/ggear/cloudera-framework).

## Release

To perform a release:

```bash
# Change the following variables to appropriate values for your target release
export CP_VERSION_RELEASE=0.8.7
export CP_VERSION_HEAD=0.8.8
mvn release:prepare -B -DreleaseVersion=$CP_VERSION_RELEASE -DdevelopmentVersion=$CP_VERSION_HEAD-SNAPSHOT
mvn release:perform
mvn release:clean clean
find . -type f -name pom.xml | xargs sed -i "" 's/'$CP_VERSION_RELEASE'-SNAPSHOT/'$CP_VERSION_HEAD'-SNAPSHOT/g';
git add -A
git commit -m "[maven-release-plugin] prepare sub modules for next development iteration"
git push --all
mvn clean install -PCMP
git tag
```
