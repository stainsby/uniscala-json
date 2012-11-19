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
  def value: V
  
  override def toString = value.toString
}


/**
 * Represents either of the two top-level JSON texts: JSON
 * objects and arrays.
 */
sealed abstract class JsonTop[V] extends JsonValue[V] {
  def toCompactString(): String = toString
  def toPrettyString(): String = toPrettyString_("", "  ")
  private[json] def toPrettyString_(margin: String, indent: String): String
}


/**
 * Represents either of the two numeric JSON values: JSON
 * integers and floats.
 */
sealed abstract class JsonNumber[V <: AnyVal] extends JsonValue[V]


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
  override val toString = "null"
}


/**
 * Represents JSON string values.
 */
case class JsonString(value: String) extends JsonValue[String] {
  override def toString = "\"" + Json.encode(value.toString) + "\""
}


/**
 * Represents the JSON false value.
 */
case object JsonFalse extends JsonBoolean(false)


/**
 * Represents the JSON true value.
 */
case object JsonTrue extends JsonBoolean(true)


/**
 * Represents the JSON integer values.
 */
case class JsonInteger(value: Long) extends JsonNumber[Long]


/**
 * Represents the JSON float values.
 */
case class JsonFloat(value: Double) extends JsonNumber[Double]


object JsonObject {
  
  def apply(values: (String, JsonValue[_])*): JsonObject =
    new JsonObject(Map(values:_*))
  
  lazy val empty = fromSeq(Nil)
  
  def fromSeq(values: Seq[(String, JsonValue[_])]): JsonObject =
    new JsonObject(Map(values:_*))
  
  implicit def canBuildFrom: CanBuildFrom[JsonObject, (String, JsonValue[_]), JsonObject] = 
    new CanBuildFrom[JsonObject, (String, JsonValue[_]), JsonObject] { 
      def apply(from: JsonObject) = apply
      def apply() = newBuilder
  }
  
  def newBuilder: Builder[(String, JsonValue[_]), JsonObject] = {
    new JsonObjectBuilder
  }
}

private[json] class JsonObjectBuilder
extends MapBuilder[String, JsonValue[_], JsonObject](JsonObject.empty)


/**
 * Represents the JSON object texts.
 * Values are stored as JsonValue instances.
 */
case class JsonObject(map: Map[String, JsonValue[_]])
extends JsonTop[Map[String, JsonValue[_]]]
with Map[String, JsonValue[_]]
with MapLike[String, JsonValue[_], JsonObject] {
  
  override def value = map
  
  override def get(key: String) = map.get(key)
  
  override def iterator = map.iterator
  
  override def + [B1 >: JsonValue[_]](kv: (String, B1)) = kv match {
    case (s: String, j: JsonValue[_]) => {
      val newEntry: (String, JsonValue[_]) = (s, j)
      new JsonObject(map + newEntry)
    }
    case _ => map + kv
  }
  
  /**
   * Merge the supplied key-value pairs with this JSON object to create a 
   * new JSON object.
   */
  def merge(kv: (String, JsonValue[_])*): JsonObject = JsonObject(map ++ kv)
  
  override def - (key: String) = JsonObject(map - key)
  
  override def empty = JsonObject()
  
  override lazy val toString = toStringBase(false)
  
  override def toCompactString = toStringBase(true)
  
  private def toStringBase(compact: Boolean): String = {
    val builder = new StringBuilder
    builder += '{'
    var first = true
    map foreach { pair =>
      if (first) {
        first = false
      } else {
        builder append (if (compact) "," else ", ")
      }
      builder append { '"' + pair._1 + '"' }
      builder append (if (compact) ":" else ": ")
      builder append {
        if (compact) {
          pair._2 match {
            case top: JsonTop[_] => top.toCompactString
            case other => other.toString
          }
        } else {
          pair._2.toString
        }
      }
    }
    builder += '}'
    builder.toString
  }
  
  override def toPrettyString_(margin: String, indent: String): String = {
    val builder = new StringBuilder
    builder += '{'
    var first = true
    val newMargin = margin + indent
    map foreach { pair =>
      if (first) {
        first = false
      } else {
        builder += ','
      }
      builder += '\n'
      builder append newMargin
      builder append { '"' + pair._1 + '"' }
      builder append ": "
      builder append {
        pair._2 match {
          case top: JsonTop[_] => top.toPrettyString_(newMargin, indent)
          case other => other.toString
        }
      }
    }
    builder += '\n'
    builder append margin
    builder += '}'
    builder.toString
    
  }
}


object JsonArray {
  
  lazy val empty = fromSeq(Nil)
  
  def apply(values: JsonValue[_]*): JsonArray =
    fromSeq(values)
  
  def fromSeq(values: Seq[JsonValue[_]]): JsonArray =
    new JsonArray(Vector(values:_*))
  
  implicit def canBuildFrom: CanBuildFrom[JsonArray, JsonValue[_], JsonArray] = 
    new CanBuildFrom[JsonArray, JsonValue[_], JsonArray] { 
      override def apply(from: JsonArray) = apply
      override def apply = newBuilder
  }
  
  def newBuilder: Builder[JsonValue[_], JsonArray] = new JsonArrayBuilder
}

private[json] class JsonArrayBuilder private(inner: VectorBuilder[JsonValue[_]])
extends Builder[JsonValue[_], JsonArray] {
  def this() = this(new VectorBuilder[JsonValue[_]])
  override def +=(elem: JsonValue[_]) = {
    inner += elem
    this
  }
  override def clear = inner.clear
  override def result = JsonArray(inner.result) 
}


/**
 * Represents the JSON array texts.
 * Elements are stored as JsonValue instances.
 */
case class JsonArray(values: Vector[JsonValue[_]])
extends JsonTop[Vector[JsonValue[_]]]
with IndexedSeq[JsonValue[_]]
with IndexedSeqLike[JsonValue[_], JsonArray] {
  
  override def value = values
  
  override def apply(i: Int): JsonValue[_] = values(i)
  
  override def length = values.length
  
  override def iterator = values.iterator
  
  override def newBuilder: Builder[JsonValue[_], JsonArray] =
    JsonArray.newBuilder
  
  override lazy val toString = toStringBase(false)
  
  override def toCompactString = toStringBase(true)
  
  override def toPrettyString_(margin: String, indent: String) = {
    val builder = new StringBuilder
    builder += '['
    var first = true
    val newMargin = margin + indent
    values foreach { v =>
      if (first) {
        first = false
      } else {
        builder += ','
      }
      builder += '\n'
      builder append newMargin
      builder append {
        v match {
          case top: JsonTop[_] => top.toPrettyString_(newMargin, indent)
          case other => other.toString
        }
      }
    }
    builder += '\n'
    builder append margin
    builder += ']'
    builder.toString
  }
  
  private def toStringBase(compact: Boolean) = {
    val builder = new StringBuilder
    builder += '['
    var first = true
    values foreach { v =>
      if (first) {
        first = false
      } else {
        builder append { if (compact) "," else ", " }
      }
      builder append {
        if (compact) {
          v match {
            case top: JsonTop[_] => top.toCompactString
            case other => other.toString
          }
        } else {
          v.toString
        }
      }
    }
    builder += ']'
    builder.toString
  }
}