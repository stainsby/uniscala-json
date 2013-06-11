package net.uniscala.json.ptr

import net.uniscala.json._
import scala.collection.SeqProxy


// TODO: try case class Pointer(segments: PointerSegment*) approach,
// similar to JsonPath?

/**
 * A JSON Pointer.
 * As per http://tools.ietf.org/html/draft-ietf-appsawg-json-pointer-07
 */
class Pointer (val self: List[PointerSegment])
extends SeqProxy[PointerSegment] {
  
  def this(segs: PointerSegment*) = this(List(segs:_*))
  
  def segments: List[PointerSegment] = self
  
  /**
   * Appends segments to this pointer.
   */
  def /(segments: String*): Pointer =
    new Pointer(self ++ segments.map(PointerSegment(_)))
  
  /**
   * Applies this pointer to a JSON value, extracting the value pointed to if
   * that value is assignable to the runtime class of J.
   */
  def apply[J <: JsonValue[_] : Manifest](json: JsonValue[_]): Option[J] = {
    var segs = segments.view
    var jvalOpt: Option[JsonValue[_]] = Some(json)
    while (!segs.isEmpty && jvalOpt.isDefined) {
      segs.headOption.foreach { seg =>
        val id = seg.id // property name or array index
        jvalOpt.foreach { jval =>
          (seg, jval) match {
            case (ns: NumericSegment, jarr: JsonArray) => {
              val idx = ns.asIndex
              jvalOpt = if (idx < jarr.length) Some(jarr(idx)) else None
            }
            case (StringSegment(nid), jarr: JsonArray) => jvalOpt = None
            case (s, jobj: JsonObject) =>
              jvalOpt = jobj.getAt[JsonValue[_]](s.id)
            case _ => jvalOpt = None
          }
        }
      }
      segs = segs.tail
    }
    jvalOpt match {
      case Some(j: J) if manifest[J].erasure.isAssignableFrom(j.getClass) => Some(j)
      case _ => None
    }
  }
  
  override lazy val toString = {
    val bld = new StringBuilder
    segments.foreach { seg =>
      bld.append("/")
      bld.append(Pointer.encodeSegment(seg.id))
    }
    bld.toString
  }
}


object Pointer {
  
  private val encodedEntities = "~0|~1".r
  private val encodingEntities = "~|/".r
  
  def apply(pointerStr: String): Pointer = {
    
    assert(
      pointerStr == "" || pointerStr.startsWith("/"),
      "invalid pointer string"
    )
    var parts: Array[String] = {
      if (pointerStr == "") Array.empty else {
        if (pointerStr == "/") Array("") else
          pointerStr.split("/").drop(1)
      }
    }
    new Pointer(parts.map { part => PointerSegment(decodeSegment(part)) }:_*)
  }
  
  def decodeSegment(encoded: String): String = {
    encodedEntities.replaceAllIn(
      encoded,
      _.matched match { case "~0" => "~" ; case "~1" => "/" }
    )
  }
  
  def encodeSegment(segment: String): String = {
    encodingEntities.replaceAllIn(
      segment,
      _.matched match { case "~" => "~0" ; case "/" => "~1" }
    )
  }
}