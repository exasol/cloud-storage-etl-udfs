# Cloud Storage Extension 1.1.0, released 2021-03-26

Code Name: v1.1.0 - Added support for Alluxio and HDFS filesystems

## Summary

The release `1.1.0` offers support for Alluxio and Hadoop Distributed Filesystem
(HDFS) filesystems. We also fixed bug in Parquet dictionary encoded decimal
converter and improved the documentation.

## Features

* #134: Added support for Hadoop Distributed Filesystem (HDFS) (PR #136).
* #135: Added support for Alluxio filesystem (PR #139).

## Bug Fixes

* #137: Fixed Parquet dictionary encoded decimal converter bug (PR #138).

## Documentation

* #131: Added access privilege to connection object (PR #132).

## Dependency Updates

### Runtime Dependency Updates

* Added `org.apache.hadoop:hadoop-hdfs:3.3.0`
* Added `org.alluxio:alluxio-core-client-hdfs:2.5.0`
* Updated `org.apache.parquet:parquet-hadoop:1.11.1` to `1.12.0`

### Test Dependency Updates

* Updated `org.scalatest:scalatest:3.2.3` to `3.2.6`
* Updated `org.mockito:mockito-core:3.7.7` to `3.8.0`
* Updated `org.testcontainers:localstack:1.15.1` to `1.15.2`
* Updated `com.exasol:test-db-builder-java:3.0.0` to `3.1.1`
* Updated `com.exasol:exasol-testcontainers:3.5.0` to `3.5.1`

### Plugin Updates

* Updated `com.timushev.sbt:sbt-updates:0.5.1` to `0.5.2`
