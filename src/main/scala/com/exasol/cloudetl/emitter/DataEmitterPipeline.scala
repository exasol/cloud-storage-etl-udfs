package com.exasol.cloudetl.emitter

import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

import com.exasol.ExaIterator
import com.exasol.cloudetl.source.Source
import com.exasol.cloudetl.storage.StorageProperties
import com.exasol.common.data.Row

import akka.Done
import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Source => AkkaSource}
import com.typesafe.scalalogging.LazyLogging
import net.ruippeixotog.streammon.ThroughputMonitor
import net.ruippeixotog.streammon.ThroughputMonitor.Stats

/**
 * A class that uses actor system to read a file and emit into Exasol, with an asynchronous boundary between the two
 * stages.
 */
class DataEmitterPipeline(source: Source, properties: StorageProperties) extends Emitter with LazyLogging {
  private[this] implicit val system: ActorSystem = ActorSystem()
  private[this] implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  /**
   * @inheritdoc
   */
  override final def emit(exasolIterator: ExaIterator): Unit = {
    val bufferSize = getBufferSize()
    val valueConverter = source.getValueConverter()
    val pipeline = AkkaSource
      .fromIterator(() => source.stream().grouped(getChunkSize()))
      .via(ThroughputMonitor(5.seconds, stats => logger.info(getStatsMessage(stats, "READ"))))
      .async
      .buffer(bufferSize, OverflowStrategy.backpressure)
      .map { values => valueConverter.convert(values) }
      .async
      .buffer(bufferSize, OverflowStrategy.backpressure)
      .via(ThroughputMonitor(5.seconds, stats => logger.info(getStatsMessage(stats, "EMIT"))))
      .runForeach(buffer => emitBuffer(buffer, exasolIterator))
    try {
      Await
        .ready(pipeline, Duration.Inf)
        .onComplete {
          case Success(Done)      => logger.info("Finished emitter pipeline.")
          case Failure(exception) => throw new IllegalStateException(exception)
        }
    } catch {
      case exception: Throwable =>
        throw new IllegalStateException("Failed to read and emit data from file.", exception)
    } finally {
      val _ = system.terminate()
    }
  }

  private[this] def getStatsMessage(stats: Stats, eventType: String): String = {
    val count = stats.count
    val throughput = stats.throughput
    s"$eventType processed '$count' events, throughput is ${"%.2f".format(throughput)} events/s"
  }

  private[this] def emitBuffer(buffer: Seq[Row], iterator: ExaIterator): Unit =
    buffer.foreach { row =>
      val columns: Seq[Object] = row.getValues().map(_.asInstanceOf[AnyRef])
      iterator.emit(columns: _*)
    }

  private[this] def getChunkSize(): Int = {
    val chunkSize = properties.getChunkSize()
    logger.info(s"Using chunk size of '$chunkSize'.")
    chunkSize
  }

  private[this] def getBufferSize(): Int = {
    val bufferSize = properties.getBufferSize()
    logger.info(s"Using buffer size of '$bufferSize'.")
    bufferSize
  }

}
