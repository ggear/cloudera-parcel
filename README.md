#Cloudera Parcel

A module to help mananage Cloudera Parcels, including a Maven plugin to:

* Build
* Install
* Deploy
* Download
* Explode

and meta data to drive parcel builds for:

* Impala Single Node OS-X

##Requirements

To compile, build and package from source, this project requires:

* JDK 1.7
* Maven 3

##Install

This project can be installed to a local repository as per:

```bash
git clone git@github.com:ggear/cloudera-parcel.git
cd cloudera-parcel
mvn install -PCMP
cd cloudera-parcel-repository
mvn install
cd ..
```

Alternatively, the module can be distributed as a binary by installing the dependencies into a shared lib (eg, [cloudera-framework](https://github.com/ggear/cloudera-framework/tree/master/cloudera-framework-thirdparty/src/main/repository)).

##Usage

The plugin can be used as per the [integration tests](https://github.com/ggear/cloudera-parcel/cloudera-parcel-plugin/src/it) and [cloudera-framework](https://github.com/ggear/cloudera-framework/tree/master/cloudera-framework-thirdparty/src/main/repository).

##Release

To perform a release:

```bash
# Change the following variables to appropriate values for your target environment
export CP_VERSION_RELEASE=0.1.0
export CP_VERSION_HEAD=0.2.0
mvn clean install
mvn release:prepare -B -DreleaseVersion=$CP_VERSION_RELEASE -DdevelopmentVersion=$CP_VERSION_HEAD-SNAPSHOT
mvn release:clean
git push --all
git tag
git branch --all
```
