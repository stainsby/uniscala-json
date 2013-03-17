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
  
  /**
   * The wrapped map that hold the JSON object data.
   */
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
  
  override def - (key: String) = JsonObject(value - key)
  
  override def empty = JsonObject.empty
  
  /**
   * Gets a value of type J, if present, at a particular path.
   */
  def getAt[J <: JsonValue[_] : Manifest](path: JsonPath): Option[J] =
    applyAt[J, J](path, (j) => j)
  
  /**
   * Gets a value of type J, if present, at a particular path.
   */
  def getAt[J <: JsonValue[_] : Manifest](path: String*): Option[J] =
    getAt[J](new JsonPath(path:_*))
  
  /**
   * Gets an (unwrapped) String, if present, at a particular path.
   */
  def getString(path: JsonPath): Option[String] =
    getAt[JsonString](path).map(_.value)
  
  /**
   * Gets an (unwrapped) String, if present, at a particular path.
   */
  def getString(path: String*): Option[String] =
    getAt[JsonString](path:_*).map(_.value)
  
  /**
   * Gets an (unwrapped) Boolean, if present, at a particular path.
   */
  def getBoolean(path: JsonPath): Option[Boolean] =
    getAt[JsonBoolean](path).map(_.value)
  
  /**
   * Gets an (unwrapped) String, if present, at a particular path.
   */
  def getBoolean(path: String*): Option[Boolean] =
    getAt[JsonBoolean](path:_*).map(_.value)
  
  /**
   * Gets an (unwrapped) Long, if present, at a particular path.
   */
  def getLong(path: JsonPath): Option[Long] =
    getAt[JsonInteger](path).map(_.value)
  
  /**
   * Gets an (unwrapped) Long, if present, at a particular path.
   */
  def getLong(path: String*): Option[Long] =
    getAt[JsonInteger](path:_*).map(_.value)
  
  /**
   * Gets an (unwrapped) Double, if present, at a particular path.
   */
  def getDouble(path: JsonPath): Option[Double] =
    getAt[JsonFloat](path).map(_.value)
  
  /**
   * Gets an (unwrapped) Double, if present, at a particular path.
   */
  def getDouble(path: String*): Option[Double] =
    getAt[JsonFloat](path:_*).map(_.value)
  
  /**
   * Applies a function to the value at a particular path.
   */
  protected def applyAt[J <: JsonValue[_] : Manifest, T](
    path: JsonPath,
    f: J => T
  ): Option[T] = 
    path.at[J, T](this, f)
  
  /**
   * Builds a new JsonObject by applying the specified changes. Each change
   * is specified by the path where it occurs and a function to make the change.
   */
  def transform(
    changes: Map[JsonPath, JsonValue[_] => JsonValue[_]]
  ): JsonObject = {
    pathMap {
      case (path, json) => changes.get(path).map(_(json)).getOrElse(json)
    }
  }
  
  /**
   * Builds a new JsonObject by applying the specified changes. Each change
   * is the path where it occurs and a function to make the change.
   */
  def transform(
    changes: (JsonPath, JsonValue[_] => JsonValue[_])*
  ): JsonObject =
    transform(Map(changes:_*))
  
  /**
   * Builds a new JsonObject by applying the specified replacements.
   * Each replacement is specified by the path where it occurs and a function
   * to replace the existing value with.
   */
  def replace(
    replacements: (JsonPath, JsonValue[_])*
  ): JsonObject = {
    val fnalChanges = replacements map { case (path, value) =>
      (path, (j: JsonValue[_]) => value)
    }
    transform(fnalChanges:_*)
  }
  
  /**
   * Applies a function over the hierarchy of JSON objects under this object.
   * The hierarchy ("tree") is is traversed depth-first. The traversal will 
   * only descend into original sub-objects preserved under the mapping 
   * function, not into ones we are creating.
   */
  def treeMap(f: JsonValue[_] => JsonValue[_]) =
    treeCollect(_ match { case j => f(j) })
  
  /**
   * Applies a partial function over the hierarchy of JSON objects under 
   * this object. Only values where the partial function is defined
   * are included in the resulting tree. In all other respects, the traversal 
   * behaviour is as described for 'treeMap'.
   */
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
  
  /**
   * The same as 'treeMap' except the path at the current point in the 
   * tree traversal is also supplied to the map function.
   */
  def pathMap(f: Function1[(JsonPath, JsonValue[_]), JsonValue[_]]) =
    pathCollect(_ match { case j => f(j) })
  
  /**
   * The same as 'treeCollect' except the path at the current point in the 
   * tree traversal is also supplied to the collect function.
   */
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
                case subobj: JsonObject => (key, pathCollect_(path, subobj, f))
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
