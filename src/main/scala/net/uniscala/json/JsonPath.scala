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

import scala.annotation.tailrec
import scala.collection.SeqProxy


object JsonPath {
  val root = new JsonPath()
  lazy val / = root
  implicit def wrapStringIntoPath(s: String) = new JsonPath(s)
}


/**
 * A path into a hierarchy of JSON objects specified as a sequence of
 * keys.
 */
class JsonPath(val self: List[String]) extends SeqProxy[String] {
  
  def this() = this(Nil)
  
  def this(segments: String*) = this(List(segments:_*))
  
  /**
   * Appends path segments to this path.
   */
  def /(segments: String*): JsonPath = {
    new JsonPath(self ++ segments)
  }
  
  private def filterByClass[J <: JsonValue[_] : Manifest](subjson: JsonValue[_]): Option[J] = {
    val targetClass: Class[_] = manifest[J].erasure
    val foundClass = subjson.getClass
    if (targetClass.isAssignableFrom(foundClass)) {
      Some(subjson.asInstanceOf[J])
    } else {
      None
    }
  }
  
  /**
   * Applies a function to the value, if any, that this path points to in
   * the supplied JSON object. If the path is invalid, that is, if it 
   * doesn't point to a  value, or if the type pointed to is not assignable 
   * to J, then None is returned. Otherwise the result of applying the 
   * function is returned, wrapped in Some.
   */
  def at[J <: JsonValue[_] : Manifest, T](
    jobj: JsonObject,
    f: J=>T
  ): Option[T] = {
    
    @tailrec def at_(
      json: JsonValue[_],
      segs: List[String]
    ): Option[T] = {
      json match {
        case jobj: JsonObject => {
          jobj.get(segs.head) match {
            case None => None
            case Some(subjson) => {
              if (segs.size == 1) {
                filterByClass[J](subjson).map(f(_))
              } else {
                at_(subjson, segs.tail)
              }
            }
          }
        }
        case _ => None
      }
    }
    
    if (this.isEmpty) {
      filterByClass[J](jobj).map(f(_))
    } else {
      at_(jobj, self)
    }
  }
  
  override def toString() = this mkString ":"
}