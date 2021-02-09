package com.exasol.cloudetl.storage

import java.util.Locale.ENGLISH

/**
 * A companion object for [[FileFormat]] class.
 *
 * It provides factory methods to create file format classes from
 * strings.
 */
object FileFormat {

  def apply(fileFormat: String): FileFormat = fileFormat.toUpperCase(ENGLISH) match {
    case "AVRO"    => AVRO
    case "DELTA"   => DELTA
    case "FILE"    => FILE
    case "ORC"     => ORC
    case "PARQUET" => PARQUET
    case _         => throw new IllegalArgumentException(s"Unsupported file format $fileFormat!")
  }

  case object AVRO extends FileFormat
  case object DELTA extends FileFormat
  case object FILE extends FileFormat
  case object ORC extends FileFormat
  case object PARQUET extends FileFormat

}

/**
 * An enum for supported file formats.
 */
sealed trait FileFormat extends Product with Serializable
