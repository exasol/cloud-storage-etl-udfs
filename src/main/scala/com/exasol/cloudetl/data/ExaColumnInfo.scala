package com.exasol.cloudetl.data

/** An Exasol table column information */
final case class ExaColumnInfo(
  name: String,
  `type`: Class[_],
  precision: Int = 0,
  scale: Int = 0,
  length: Int = 0,
  isNullable: Boolean = true
)
