package com.exasol.cloudetl.emitter

import com.exasol.ExaIterator

/*
 * An interface for data emitting classes.
 */
trait Emitter {

  /**
   * Emits data read from files into Exasol table.
   *
   * @param iterator an Exasol iterator for emitting
   */
  def emit(iterator: ExaIterator): Unit

}
