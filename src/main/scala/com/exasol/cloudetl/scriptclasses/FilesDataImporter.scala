package com.exasol.cloudetl.scriptclasses

import scala.collection.mutable.ListBuffer

import com.exasol.ExaIterator
import com.exasol.ExaMetadata
import com.exasol.cloudetl.bucket.Bucket
import com.exasol.cloudetl.emitter.DataEmitterPipeline
import com.exasol.cloudetl.source._
import com.exasol.cloudetl.storage.StorageProperties

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.Path

/**
 * A importer class that reads and imports data into Exasol database.
 */
object FilesDataImporter extends LazyLogging {

  /**
   * Reads files and emits their data into Exasol iterator.
   *
   * @param metadata an Exasol metadata object
   * @param iterator an Exasol iterator object
   */
  def run(metadata: ExaMetadata, iterator: ExaIterator): Unit = {
    val storageProperties = StorageProperties(iterator.getString(1), metadata)
    val bucket = Bucket(storageProperties)
    val files = groupFiles(iterator, 2)
    val nodeId = metadata.getNodeId()
    val vmId = metadata.getVmId()
    logger.info(s"The total number of files is '${files.size}' for node '$nodeId', vm '$vmId'.")
    runImport(bucket, files, iterator)
  }

  private[this] def groupFiles(iterator: ExaIterator, fileIndex: Int): Seq[String] = {
    val files = ListBuffer[String]()
    do {
      files.append(iterator.getString(fileIndex))
    } while (iterator.next())
    files.toSeq
  }

  private[this] def runImport(bucket: Bucket, files: Seq[String], iterator: ExaIterator): Unit = {
    val properties = bucket.properties
    val fileFormat = properties.getFileFormat()
    val conf = bucket.getConfiguration()
    val filesystem = bucket.fileSystem
    files.foreach { case file =>
      logger.info(s"Starting import for file '$file'.")
      val source = Source(fileFormat, new Path(file), conf, filesystem)
      new DataEmitterPipeline(source, properties).emit(iterator)
      source.close()
    }
  }

}
