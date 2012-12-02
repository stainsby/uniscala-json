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
  
  /**
   * An alias for JsonNull.
   */
  val Jnull = JsonNull
  
  /**
   * Creates a JSON array with the specified elements.
   */
  def apply(values: JsonValue[_]*) = JsonArray(values:_*)
  
  /**
   * Creates a JSON array with the supplied list.
   */
  def apply(values: (String, JsonValue[_])*) = JsonObject(values:_*)
  
  /**
   * Implicitly constructs a JSONString from a Scala String. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueStringWrap(s: String) = JsonString(s)
  
  /**
   * Implicitly constructs a JSONBoolean from a Scala Boolean. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueBooleanWrap(b: Boolean) =
    if (b) JsonTrue else JsonFalse
  
  /**
   * Implicitly constructs a JsonInteger from a Scala Int. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueIntegerWrap(i: Int) = JsonInteger(i)
  
  /**
   * Implicitly constructs a JsonInteger from a Scala Long. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueLongWrap(l: Long) = JsonInteger(l)
  
  /**
   * Implicitly constructs a JSONString from a Scala String. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueFloatWrap(f: Float) = JsonFloat(f)
  
  /**
   * Implicitly constructs a JSONString from a Scala String. This is 
   * typically useful when constructing JSON arrays.
   */
  implicit def implicitJsonValueDoubleWrap(d: Double) = JsonFloat(d)
  
  /**
   * Implicitly constructs a String-JSONString pair from a Scala 
   * String-String pair. This is typically useful when constructing JSON 
   * objects.
   */
  implicit def implicitJsonValueStringPairWrap(ss: (String, String)) =
    (ss._1, JsonString(ss._2))
  
  /**
   * Implicitly constructs a String-JSONBoolean pair from a Scala 
   * String-Boolean pair. This is typically useful when constructing JSON 
   * objects.
   */
  implicit def implicitJsonValueBooleanPairWrap(sb: (String,  Boolean)) =
    (sb._1, if (sb._2) JsonTrue else JsonFalse)
  
  /**
   * Implicitly constructs a String-JSONInteger pair from a Scala 
   * String-Int pair. This is typically useful when constructing JSON 
   * objects.
   */
  implicit def implicitJsonValueIntPairWrap(si: (String,  Int)) =
    (si._1, JsonInteger(si._2))
  
  /**
   * Implicitly constructs a String-JSONInteger pair from a Scala 
   * String-Long pair. This is typically useful when constructing JSON 
   * objects.
   */
  implicit def implicitJsonValueLongPairWrap(sl: (String,  Long)) =
    (sl._1, JsonInteger(sl._2))
  
  /**
   * Implicitly constructs a String-JSONFloat pair from a Scala 
   * String-Float pair. This is typically useful when constructing JSON 
   * objects.
   */
  implicit def implicitJsonValueFloatPairWrap(sf: (String,  Float)) =
    (sf._1, JsonFloat(sf._2))
  
  /**
   * Implicitly constructs a String-JSONFloat pair from a Scala 
   * String-Double pair. This is typically useful when constructing JSON 
   * objects.
   */
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