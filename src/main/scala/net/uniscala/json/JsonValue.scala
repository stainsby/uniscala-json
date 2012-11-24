/**
 * This file is part of the Uniscala JSON project.
 * Copyright (C) 2012 Sustainable Software Pty Ltd.
 * This is open source software, licensed under the Apache License
 * version 2.0 license - please see the LICENSE file included in
 * the distribution.
 *
 * Authors:
 * Sam Stainsby (sam@sustainablesoftware.com.au)
 */
package net.uniscala.json

import _root_.scala.collection.IndexedSeqLike
import _root_.scala.collection.generic.CanBuildFrom
import _root_.scala.collection.mutable.{Builder, MapBuilder}
import _root_.scala.collection.immutable.{MapLike, Vector, VectorBuilder}


/**
 * The base class for all JSON text or value representations.
 * This class is immutable
 */
sealed abstract class JsonValue[V] extends Immutable {
  
  /**
   * The wrapped Scala value.
   */
  def value(): V
  
  override def toString() = value.toString
}


/**
 * Represents either of the two top-level JSON texts: JSON
 * objects and arrays.
 */
sealed abstract class JsonTop[V] extends JsonValue[V] {
  def toCompactString(): String //= toString
  def toPrettyString(): String //= toPrettyString_("", "  ")
  private[json] def toPrettyString_(margin: String, indent: String): String
}


/**
 * Represents either of the two numeric JSON values: JSON
 * integers and floats.
 */
sealed abstract class JsonNumber[V <: AnyVal] extends JsonValue[V] {
  override lazy val toString = value.toString
}


/**
 * Represents either of the two boolean JSON values: JSON
 * true and false.
 */
sealed abstract class JsonBoolean(val value: Boolean) extends JsonValue[Boolean]

/**
 * Represents the JSON null value.
 */
case object JsonNull extends JsonValue[Null] {
  val value = null
  override def toString() = "null"
}


/**
 * Represents JSON string values.
 */
case class JsonString(value: String) extends JsonValue[String] {
  override lazy val toString = {
    (new StringBuilder).append("\"").append(Json.encode(value.toString)).
      append("\"").toString
  }
}


/**
 * Represents the JSON false value.
 */
case object JsonFalse extends JsonBoolean(false) {
  override def toString() = "false"
}


/**
 * Represents the JSON true value.
 */
case object JsonTrue extends JsonBoolean(true) {
  override def toString() = "true"
}


/**
 * Represents the JSON integer values.
 */
case class JsonInteger(value: Long) extends JsonNumber[Long]


/**
 * Represents the JSON float values.
 */
case class JsonFloat(value: Double) extends JsonNumber[Double]


// JSON objects


object JsonObject {
  
  lazy val empty = fromSeq(Nil)
  
  def apply(values: (String, JsonValue[_])*): JsonObject =
    new JsonObject(Map(values:_*))
  
  def fromSeq(values: Seq[(String, JsonValue[_])]): JsonObject =
    new JsonObject(Map(values:_*))
}


/**
 * Represents the JSON object texts.
 * Values are stored as JsonValue instances.
 */
case class JsonObject(value: Map[String, JsonValue[_]])
extends JsonTop[Map[String, JsonValue[_]]] with JsonObjectLike


// JSON arrays


object JsonArray {
  
  lazy val empty = fromSeq(Nil)
  
  def apply(values: JsonValue[_]*): JsonArray =
    fromSeq(values)
  
  def fromSeq(values: Seq[JsonValue[_]]): JsonArray =
    new JsonArray(Vector(values:_*))
}


/**
 * Represents the JSON array texts.
 * Elements are stored as JsonValue instances.
 */
case class JsonArray(value: Vector[JsonValue[_]])
extends JsonTop[Vector[JsonValue[_]]] with JsonArrayLike
