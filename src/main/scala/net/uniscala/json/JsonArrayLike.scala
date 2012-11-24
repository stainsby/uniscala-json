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
import _root_.scala.collection.immutable.{Vector, VectorBuilder}
import _root_.scala.collection.mutable.Builder


private[json] object JsonArrayLike {
  
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


private[json] trait JsonArrayLike
extends IndexedSeq[JsonValue[_]]
with IndexedSeqLike[JsonValue[_], JsonArray] {
  
  this: JsonArray =>
  
  def value: Vector[JsonValue[_]]
  
  override def apply(i: Int): JsonValue[_] = value(i)
  
  override def length = value.length
  
  override def iterator = value.iterator
  
  override def newBuilder: Builder[JsonValue[_], JsonArray] =
    JsonArrayLike.newBuilder
  
  override lazy val toString = toStringBase(false)
  
  override lazy val toCompactString: String = toStringBase(true)
  
  override lazy val toPrettyString: String = toPrettyString_("", "  ")
  
  private[json] def toPrettyString_(margin: String, indent: String): String = {
    val builder = new StringBuilder
    builder += '['
    var first = true
    val newMargin = margin + indent
    value foreach { v =>
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
  
  private def toStringBase(compact: Boolean): String = {
    val builder = new StringBuilder
    builder += '['
    var first = true
    value foreach { v =>
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