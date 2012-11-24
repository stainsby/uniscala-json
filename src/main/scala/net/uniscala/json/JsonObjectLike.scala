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

import _root_.scala.collection.generic.CanBuildFrom
import _root_.scala.collection.immutable.MapLike
import _root_.scala.collection.mutable.{Builder, MapBuilder}


private[json] object JsonObjectLike {
  
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


private[json] trait JsonObjectLike
extends Map[String, JsonValue[_]]
with MapLike[String, JsonValue[_], JsonObject] {
  
  this: JsonObject =>
  
  def value: Map[String, JsonValue[_]]
  
  override def get(key: String) = value.get(key)
  
  override def iterator = value.iterator
  
  override def + [B1 >: JsonValue[_]](kv: (String, B1)) = kv match {
    case (s: String, j: JsonValue[_]) => {
      val newEntry: (String, JsonValue[_]) = (s, j)
      new JsonObject(value + newEntry)
    }
    case _ => value + kv
  }
  
  /**
   * Merge the supplied key-value pairs with this JSON object to create a 
   * new JSON object.
   */
  def merge(kv: (String, JsonValue[_])*): JsonObject = JsonObject(value ++ kv)
  
  override def - (key: String) = JsonObject(value - key)
  
  override def empty = JsonObject.empty
  
  override lazy val toString = toStringBase(false)
  
  override lazy val toCompactString = toStringBase(true)
  
  override lazy val toPrettyString: String = toPrettyString_("", "  ")
  
  override def toPrettyString_(margin: String, indent: String): String = {
    val builder = new StringBuilder
    builder += '{'
    var first = true
    val newMargin = margin + indent
    value foreach { pair =>
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
  
  private def toStringBase(compact: Boolean): String = {
    val builder = new StringBuilder
    builder += '{'
    var first = true
    value foreach { pair =>
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

}