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
   * Add or override key-value pairs in this JSON object.
   */
  def merge(kv: (String, JsonValue[_])*): JsonObject = JsonObject(value ++ kv)
  
  override def - (key: String) = JsonObject(value - key)
  
  override def empty = JsonObject.empty
  
  def getAt[J <: JsonValue[_] : Manifest](path: JsonPath): Option[J] =
    applyAt[J, J](path, (j) => j)
  
  def transform(
    changes: Map[JsonPath, JsonValue[_] => JsonValue[_]]
  ): JsonObject = {
    pathMap {
      case (path, json) => {
        //println("TX @ path=" + path + " value=" + json)
        val changeOpt = changes.get(path)
        if (changeOpt.isDefined)
          println("TX changes: " + changeOpt + " found at \"" + path + "\" in " + changes)
        changeOpt.map(_(json)).getOrElse(json)
      }
    }
  }
  
  def transform(
    changes: (JsonPath, JsonValue[_] => JsonValue[_])*
  ): JsonObject =
    transform(Map(changes:_*))
  
  def replace(
    changes: (JsonPath, JsonValue[_])*
  ): JsonObject = {
    val fnalChanges = changes map { case (path, value) =>
      (path, (j: JsonValue[_]) => value)
    }
    transform(fnalChanges:_*)
  }
  
  def applyAt[J <: JsonValue[_] : Manifest, T](
    path: JsonPath, f: J => T
  ): Option[T] = 
    path.at[J, T](this, f)
  
  def treeMap(f: JsonValue[_] => JsonValue[_]) =
    treeCollect(_ match { case j => f(j) })
  
  def treeCollect(
    f: PartialFunction[JsonValue[_], JsonValue[_]]
  ): JsonObject = {
    
    def treeCollect_(
      jobj: JsonObject,
      f: PartialFunction[JsonValue[_], JsonValue[_]]
    ): JsonObject = {
      JsonObject(
        jobj flatMap { kv: (String, JsonValue[_]) =>
          val key: String = kv._1
          val subjson: JsonValue[_] = kv._2
          val newSubjsonOpt = f.lift(subjson)
          newSubjsonOpt map { newSubjson =>
            val changed = !(subjson eq newSubjson)
            // note: we only recurse into original sub-objects preserved
            // under the mapping function, not into ones we are creating
            val preserved: Boolean = newSubjson eq subjson
            if (preserved) {
              subjson match {
                case subobj: JsonObject => (key, treeCollect_(subobj, f))
                case _ => kv
              }
            } else {
              (key, newSubjson)
            }
          }
        }
      )
    }
    
    treeCollect_(this, f)
  }
  
  def pathMap(f: Function1[(JsonPath, JsonValue[_]), JsonValue[_]]) =
    pathCollect(_ match { case j => f(j) })
  
  def pathCollect(
    f: PartialFunction[(JsonPath, JsonValue[_]), JsonValue[_]]
  ): JsonObject = {
    
    def pathCollect_(
      root: JsonPath,
      jobj: JsonObject,
      f: PartialFunction[(JsonPath, JsonValue[_]), JsonValue[_]]
    ): JsonObject = {
      JsonObject(
        jobj flatMap { kv: (String, JsonValue[_]) =>
          val key: String = kv._1
          val subjson: JsonValue[_] = kv._2
          val path = root / key
          val newSubjsonOpt = f.lift((path, subjson))
          newSubjsonOpt map { newSubjson =>
            val changed = !(subjson eq newSubjson)
            // note: we only recurse into original sub-objects preserved
            // under the mapping function, not into ones we are creating
            val preserved: Boolean = newSubjson eq subjson
            if (preserved) {
              subjson match {
                case subobj: JsonObject => {
                  val subpath = path / key
                  (key, pathCollect_(subpath, subobj, f))
                }
                case _ => kv
              }
            } else {
              (key, newSubjson)
            }
          }
        }
      )
    }
    
    pathCollect_(JsonPath.root, this, f)
  }
  
  override lazy val toString = toStringBase(false)
  
  override lazy val toCompactString = toStringBase(true)
  
  override lazy val toPrettyString: String = toPrettyString_("", "  ")
  
  def toPrettyString_(margin: String, indent: String): String = {
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
  
  protected def toStringBase(compact: Boolean): String = {
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
