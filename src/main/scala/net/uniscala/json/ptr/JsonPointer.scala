package net.uniscala.json.ptr

import net.uniscala.json._
import scala.collection.SeqProxy


// TODO: try case class Pointer(segments: JsonPointerSegment*) approach,
// similar to JsonPath?

/**
 * A JSON Pointer.
 * As per http://tools.ietf.org/html/draft-ietf-appsawg-json-pointer-07
 */
class JsonPointer (val self: List[JsonPointerSegment])
extends SeqProxy[JsonPointerSegment] {
  
  def this(segs: JsonPointerSegment*) = this(List(segs:_*))
  
  def segments: List[JsonPointerSegment] = self
  
  /**
   * Appends segments to this pointer.
   */
  def /(segments: String*): JsonPointer =
    new JsonPointer(self ++ segments.map(JsonPointerSegment(_)))
  
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
            case (ns: JsonNumericSegment, jarr: JsonArray) => {
              val idx = ns.asIndex
              jvalOpt = if (idx < jarr.length) Some(jarr(idx)) else None
            }
            case (JsonStringSegment(nid), jarr: JsonArray) => jvalOpt = None
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
      bld.append(JsonPointer.encodeSegment(seg.id))
    }
    bld.toString
  }
}


object JsonPointer {
  
  private val encodedEntities = "~0|~1".r
  private val encodingEntities = "~|/".r
  
  val root = JsonPointer("")
  
  def apply(pointerStr: String): JsonPointer = {
    
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
    new JsonPointer(parts.map { part => JsonPointerSegment(decodeSegment(part)) }:_*)
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