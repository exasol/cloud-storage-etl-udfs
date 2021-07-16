package com.exasol.cloudetl.source

import com.exasol.common.data.Row

/**
 * An interface for source value transformers.
 */
trait ValueConverter {

  /**
   * Converts row of values.
   *
   * @param values iterator of rows
   * @return transformed iterator
   */
  def convert(values: Seq[Row]): Seq[Row]

}
