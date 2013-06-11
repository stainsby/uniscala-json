package net.uniscala.json.ptr


object JsonPointerSegment {
  
  private val allDigits = "^([0-9]+)$".r
  
  def apply(id: String): JsonPointerSegment = id match {
    case allDigits(nid) => JsonNumericSegment(nid)
    case sid => JsonStringSegment(sid)
  }
  
  def apply(id: Int): JsonPointerSegment = JsonNumericSegment(id.toString)
}


sealed abstract class JsonPointerSegment {
  val id: String
}


case class JsonStringSegment private[ptr] (id: String) extends JsonPointerSegment


case class JsonNumericSegment private[ptr] (id: String) extends JsonPointerSegment {
  val asIndex: Int = {
    val idx = Integer.parseInt(id)
    assert(idx >= 0, "invalid index")
    idx
  }
}
