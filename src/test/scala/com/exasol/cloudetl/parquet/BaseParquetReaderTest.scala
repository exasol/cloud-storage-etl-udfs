package com.exasol.cloudetl.parquet

import java.io.Closeable
import java.nio.file.Path

import com.exasol.cloudetl.TestFileManager
import com.exasol.cloudetl.source.ParquetSource
import com.exasol.common.data.Row

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path => HPath}
import org.apache.hadoop.fs.FileSystem
import org.apache.parquet.example.data.Group
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.schema.MessageType
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

trait BaseParquetReaderTest
    extends AnyFunSuite
    with BeforeAndAfterEach
    with TestFileManager
    with ParquetTestDataWriter {

  private[this] var conf: Configuration = _
  private[this] var fileSystem: FileSystem = _
  private[this] var outputDirectory: Path = _
  private[this] var path: HPath = _

  override final def beforeEach(): Unit = {
    conf = new Configuration
    fileSystem = FileSystem.get(conf)
    outputDirectory = createTemporaryFolder("parquetRowReaderTest")
    path = new HPath(outputDirectory.toUri.toString, "part-00000.parquet")
    ()
  }

  override final def afterEach(): Unit = {
    deletePathFiles(outputDirectory)
    ()
  }

  protected final def withResource[T <: Closeable](writer: T)(block: T => Unit): Unit = {
    block(writer)
    writer.close()
  }

  protected final def getRecords(): Seq[Row] =
    ParquetSource(path, conf, fileSystem).streamWithValueConverter()

  protected final def getParquetWriter(
    schema: MessageType,
    encoding: Boolean
  ): ParquetWriter[Group] =
    getParquetWriter(path, schema, encoding)
}
