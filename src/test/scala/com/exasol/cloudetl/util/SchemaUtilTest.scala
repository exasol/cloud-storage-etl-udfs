package com.exasol.cloudetl.util

import com.exasol.ExaIterator
import com.exasol.cloudetl.data.ExaColumnInfo

import org.apache.parquet.schema._
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName
import org.apache.parquet.schema.Type.Repetition
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar

@SuppressWarnings(Array("org.wartremover.contrib.warts.ExposedTuples"))
class SchemaUtilTest extends FunSuite with MockitoSugar {

  test("createParquetMessageType throws if type is unknown") {
    val thrown = intercept[IllegalArgumentException] {
      SchemaUtil.createParquetMessageType(
        Seq(ExaColumnInfo("c_short", classOf[java.lang.Short], 0, 0, 0, false)),
        "test_schema"
      )
    }
    val expectedMsg = s"Cannot convert Exasol type '${classOf[java.lang.Short]}' to Parquet type."
    assert(thrown.getMessage === expectedMsg)
  }

  test("createParquetMessageType returns Parquet MessageType from Exasol columns") {
    val exasolColumns = Seq(
      ExaColumnInfo("c_int", classOf[java.lang.Integer], 0, 0, 0, true),
      ExaColumnInfo("c_int", classOf[java.lang.Integer], 1, 0, 0, true),
      ExaColumnInfo("c_int", classOf[java.lang.Integer], 9, 0, 0, true),
      ExaColumnInfo("c_long", classOf[java.lang.Long], 0, 0, 0, false),
      ExaColumnInfo("c_long", classOf[java.lang.Long], 18, 0, 0, true),
      ExaColumnInfo("c_decimal_int", classOf[java.math.BigDecimal], 9, 0, 0, false),
      ExaColumnInfo("c_decimal_long", classOf[java.math.BigDecimal], 17, 0, 0, false),
      ExaColumnInfo("c_decimal", classOf[java.math.BigDecimal], 38, 10, 16, false),
      ExaColumnInfo("c_double", classOf[java.lang.Double], 0, 0, 0, true),
      ExaColumnInfo("c_string", classOf[java.lang.String], 0, 0, 0, false),
      ExaColumnInfo("c_string", classOf[java.lang.String], 0, 0, 20, false),
      ExaColumnInfo("c_boolean", classOf[java.lang.Boolean], 0, 0, 0, false),
      ExaColumnInfo("c_date", classOf[java.sql.Date], 0, 0, 0, false),
      ExaColumnInfo("c_timestamp", classOf[java.sql.Timestamp], 0, 0, 0, false)
    )

    val schemaName = "exasol_export_schema"

    val messageType = new MessageType(
      schemaName,
      new PrimitiveType(Repetition.OPTIONAL, PrimitiveType.PrimitiveTypeName.INT32, "c_int"),
      Types
        .primitive(PrimitiveTypeName.INT32, Repetition.OPTIONAL)
        .precision(1)
        .scale(0)
        .as(OriginalType.DECIMAL)
        .named("c_int"),
      Types
        .primitive(PrimitiveTypeName.INT32, Repetition.OPTIONAL)
        .precision(9)
        .scale(0)
        .as(OriginalType.DECIMAL)
        .named("c_int"),
      new PrimitiveType(Repetition.REQUIRED, PrimitiveType.PrimitiveTypeName.INT64, "c_long"),
      Types
        .primitive(PrimitiveTypeName.INT64, Repetition.OPTIONAL)
        .precision(18)
        .scale(0)
        .as(OriginalType.DECIMAL)
        .named("c_long"),
      Types
        .primitive(PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY, Repetition.REQUIRED)
        .precision(9)
        .scale(0)
        .length(4)
        .as(OriginalType.DECIMAL)
        .named("c_decimal_int"),
      Types
        .primitive(PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY, Repetition.REQUIRED)
        .precision(17)
        .scale(0)
        .length(8)
        .as(OriginalType.DECIMAL)
        .named("c_decimal_long"),
      Types
        .primitive(PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY, Repetition.REQUIRED)
        .precision(38)
        .scale(10)
        .length(16)
        .as(OriginalType.DECIMAL)
        .named("c_decimal"),
      new PrimitiveType(Repetition.OPTIONAL, PrimitiveType.PrimitiveTypeName.DOUBLE, "c_double"),
      new PrimitiveType(
        Repetition.REQUIRED,
        PrimitiveType.PrimitiveTypeName.BINARY,
        "c_string",
        OriginalType.UTF8
      ),
      Types
        .primitive(PrimitiveTypeName.BINARY, Repetition.REQUIRED)
        .length(20)
        .as(OriginalType.UTF8)
        .named("c_string"),
      new PrimitiveType(
        Repetition.REQUIRED,
        PrimitiveType.PrimitiveTypeName.BOOLEAN,
        "c_boolean"
      ),
      Types
        .primitive(PrimitiveTypeName.INT32, Repetition.REQUIRED)
        .as(OriginalType.DATE)
        .named("c_date"),
      new PrimitiveType(Repetition.REQUIRED, PrimitiveType.PrimitiveTypeName.INT96, "c_timestamp")
    )

    assert(SchemaUtil.createParquetMessageType(exasolColumns, schemaName) === messageType)
  }

  test("createParquetMessageType throws if integer precision is larger than allowed") {
    val exasolColumns = Seq(ExaColumnInfo("c_int", classOf[java.lang.Integer], 10, 0, 0, true))
    val thrown = intercept[IllegalArgumentException] {
      SchemaUtil.createParquetMessageType(exasolColumns, "test")
    }
    val expectedMsg = "requirement failed: Got an 'Integer' type with more than '9' precision."
    assert(thrown.getMessage === expectedMsg)
  }

  test("createParquetMessageType throws if long precision is larger than allowed") {
    val exasolColumns = Seq(ExaColumnInfo("c_long", classOf[java.lang.Long], 20, 0, 0, true))
    val thrown = intercept[IllegalArgumentException] {
      SchemaUtil.createParquetMessageType(exasolColumns, "test")
    }
    val expectedMsg = "requirement failed: Got a 'Long' type with more than '18' precision."
    assert(thrown.getMessage === expectedMsg)
  }

  test("exaColumnToValue returns value with column type") {
    val iter = mock[ExaIterator]
    val startIdx = 3
    val bd = new java.math.BigDecimal(1337)
    val dt = new java.sql.Date(System.currentTimeMillis())
    val ts = new java.sql.Timestamp(System.currentTimeMillis())

    when(iter.getInteger(3)).thenReturn(1)
    when(iter.getLong(4)).thenReturn(3L)
    when(iter.getBigDecimal(5)).thenReturn(bd)
    when(iter.getDouble(6)).thenReturn(3.14)
    when(iter.getString(7)).thenReturn("xyz")
    when(iter.getBoolean(8)).thenReturn(true)
    when(iter.getDate(9)).thenReturn(dt)
    when(iter.getTimestamp(10)).thenReturn(ts)

    val data = Seq(
      1 -> ExaColumnInfo("c_int", classOf[java.lang.Integer]),
      3L -> ExaColumnInfo("c_long", classOf[java.lang.Long]),
      bd -> ExaColumnInfo("c_decimal", classOf[java.math.BigDecimal]),
      3.14 -> ExaColumnInfo("c_double", classOf[java.lang.Double]),
      "xyz" -> ExaColumnInfo("c_string", classOf[java.lang.String]),
      true -> ExaColumnInfo("c_boolean", classOf[java.lang.Boolean]),
      dt -> ExaColumnInfo("c_date", classOf[java.sql.Date]),
      ts -> ExaColumnInfo("c_timestamp", classOf[java.sql.Timestamp])
    )

    data.zipWithIndex.map {
      case ((expectedValue, col), idx) =>
        val nxtIdx = startIdx + idx
        val ret = SchemaUtil.exaColumnToValue(iter, nxtIdx, col)
        assert(ret === expectedValue)
        assert(ret.getClass === col.`type`)
    }

    val thrown = intercept[IllegalArgumentException] {
      SchemaUtil.exaColumnToValue(iter, 0, ExaColumnInfo("c_short", classOf[java.lang.Short]))
    }
    assert(
      thrown.getMessage === "Cannot get Exasol value for column type 'class java.lang.Short'."
    )

  }
}
