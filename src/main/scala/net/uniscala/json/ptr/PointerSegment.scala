package net.uniscala.json.ptr


object PointerSegment {
  
  private val allDigits = "^([0-9]+)$".r
  
  def apply(id: String): PointerSegment = id match {
    case allDigits(nid) => NumericSegment(nid)
    case sid => StringSegment(sid)
  }
  
  def apply(id: Int): PointerSegment = NumericSegment(id.toString)
}


sealed abstract class PointerSegment {
  val id: String
}


case class StringSegment private[ptr] (id: String) extends PointerSegment


case class NumericSegment private[ptr] (id: String) extends PointerSegment {
  val asIndex: Int = {
    val idx = Integer.parseInt(id)
    assert(idx >= 0, "invalid index")
    idx
  }
}
