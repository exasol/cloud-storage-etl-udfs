package com.exasol.cloudetl.source

import scala.util.control.NonFatal

import com.exasol.cloudetl.orc.converter.StructConverter
import com.exasol.common.data.Row
import com.exasol.common.json.JsonMapper

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hive.ql.exec.vector.StructColumnVector
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch
import org.apache.orc.OrcFile
import org.apache.orc.Reader

/**
 * An Orc source that is able to read orc formatted files.
 */
final case class OrcSource(
  override val path: Path,
  override val conf: Configuration,
  override val fileSystem: FileSystem
) extends Source
    with LazyLogging {

  private val reader: Reader = createReader()
  private var recordReader = reader.rows(new Reader.Options())

  /** @inheritdoc */
  override def stream(): Iterator[Row] = new Iterator[Row] {
    val batch = reader.getSchema().createRowBatch()
    var batchIterator = new BatchIterator(batch)

    override def hasNext: Boolean = batchIterator.hasNext || {
      batch.reset()
      val _ = recordReader.nextBatch(batch)
      batchIterator = new BatchIterator(batch)
      !batch.endOfFile && batch.size > 0 && batchIterator.hasNext
    }

    override def next(): Row =
      batchIterator.next()
  }

  override def close(): Unit =
    if (recordReader != null) {
      try {
        recordReader.close()
      } finally {
        recordReader = null
      }
    }

  /**
   * @inheritdoc
   *
   * At the moment Orc values do not require additional transformation.
   */
  override def getValueConverter(): ValueConverter = new ValueConverter {
    override def convert(rows: Seq[Row]) = rows
  }

  private[this] def createReader(): Reader = {
    val options = OrcFile.readerOptions(conf).filesystem(fileSystem)
    try {
      OrcFile.createReader(path, options)
    } catch {
      case NonFatal(exception) =>
        logger.error(s"Could not create orc reader for the path: $path", exception)
        throw exception
    }
  }

  private[this] final class BatchIterator(batch: VectorizedRowBatch) extends Iterator[Row] {
    val schema = reader.getSchema()
    val fields = schema.getChildren()
    val fieldNames = schema.getFieldNames()
    val vector = new StructColumnVector(batch.numCols, batch.cols: _*)
    val converter = new StructConverter(schema)
    var offset = 0

    override def hasNext: Boolean = offset < batch.size

    override def next(): Row = {
      val valuesMap = converter.readAt(vector, offset)
      val array = Array.ofDim[Any](fields.size)
      for { index <- 0 until fields.size() } {
        val columnName = fieldNames.get(index)
        val columnValue = valuesMap.getOrElse(columnName, null)
        if (fields.get(index).getCategory().isPrimitive()) {
          array.update(index, columnValue)
        } else {
          array.update(index, JsonMapper.toJson(columnValue))
        }
      }
      offset = offset + 1
      Row(array.toSeq)
    }
  }

}
