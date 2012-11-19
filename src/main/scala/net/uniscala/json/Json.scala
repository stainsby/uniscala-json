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


object Json {
  
  val Jnull = JsonNull
  
  def apply(values: JsonValue[_]*) = JsonArray(values:_*)
  
  def apply(values: (String, JsonValue[_])*) = JsonObject(values:_*)
  
  implicit def implicitJsonValueStringWrap(s: String) = JsonString(s)
  
  implicit def implicitJsonValueBooleanWrap(b: Boolean) =
    if (b) JsonTrue else JsonFalse
  
  implicit def implicitJsonValueIntegerWrap(i: Int) = JsonInteger(i)
  
  implicit def implicitJsonValueLongWrap(l: Long) = JsonInteger(l)
  
  implicit def implicitJsonValueFloatWrap(f: Float) = JsonFloat(f)
  
  implicit def implicitJsonValueDoubleWrap(d: Double) = JsonFloat(d)
  
  implicit def implicitJsonValueStringPairWrap(ss: (String, String)) =
    (ss._1, JsonString(ss._2))
  
  implicit def implicitJsonValueBooleanPairWrap(sb: (String,  Boolean)) =
    (sb._1, if (sb._2) JsonTrue else JsonFalse)
  
  implicit def implicitJsonValueIntPairWrap(si: (String,  Int)) =
    (si._1, JsonInteger(si._2))
  
  implicit def implicitJsonValueLongPairWrap(sl: (String,  Long)) =
    (sl._1, JsonInteger(sl._2))
  
  implicit def implicitJsonValueFloatPairWrap(sf: (String,  Float)) =
    (sf._1, JsonFloat(sf._2))
  
  implicit def implicitJsonValueDoublePairWrap(sd: (String,  Double)) =
    (sd._1, JsonFloat(sd._2))
  
  private[json] def encode(s: String): String = {
    val builder = new StringBuilder
    s foreach { ch: Char =>
      ch match {
        case '"' => builder append "\\\""
        case '/' => builder append "\\/"
        case '\\' => builder append "\\\\"
        case '\b' => builder append "\\b"
        case '\f' => builder append "\\f"
        case '\n' => builder append "\\n"
        case '\r' => builder append "\\r"
        case '\t' => builder append "\\t"
        case c if c < 32 || c > 126 => unicodeEscape(c, builder)
        case _ => builder += ch
      }
    }
    builder.toString
  }
  
  private[json] def unicodeEscape(ch: Char, builder: StringBuilder): Unit = {
    var hexStr = Integer.toHexString(ch)
    val n = hexStr.length
    if (n < 4) {
      hexStr = "0000".substring(n) + hexStr
    }
    assert(n <= 4)
    builder append "\\u" + hexStr
  }
}