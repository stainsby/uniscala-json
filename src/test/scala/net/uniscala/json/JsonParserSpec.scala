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

import scala.collection.immutable.TreeMap

import org.scalatest.FunSuite


object JsonParserSpec {
  
  private def nongenericClass[T](cls: Class[_]): Class[AnyRef] =
    cls.asInstanceOf[Class[AnyRef]]
  
  val JOBJ = nongenericClass(classOf[JsonObject])
  val JARR = nongenericClass(classOf[JsonArray])
  val THBL = nongenericClass(classOf[Throwable])
  val JSER  = nongenericClass(classOf[JsonFormatException])
  
  // map from an arbitrary JSON text case name to a tuple consisting of:
  //   - the JSON text or null if the case name should be used as the text
  //   - result class (including exception class)
  //   - size if applicable or 1
  //   - value of the first element (name-value pair for JSON objects) if
  //     applicable, or null; if the expected value is not a JsonValue instance
  //     then this is a value from unwrapping the parsed object
  //     by calling its 'value' method
  lazy val texts: Map[String, (String, Class[AnyRef], Int, Any)] = TreeMap(
    
    "{}"   -> (null, JOBJ, 0, null),
    "{ }"  -> (null, JOBJ, 0, null),
    "{  }" -> (null, JOBJ, 0, null),
    " {}"  -> (null, JOBJ, 0, null),
    " {} " -> (null, JOBJ, 0, null),
    "{} "  -> (null, JOBJ, 0, null),
    
    "[]"   -> (null, JARR, 0, null),
    "[ ]"  -> (null, JARR, 0, null),
    "[  ]" -> (null, JARR, 0, null),
    " []"  -> (null, JARR, 0, null),
    " [] " -> (null, JARR, 0, null),
    "[] "  -> (null, JARR, 0, null),
    
    "[true]"    -> (null, JARR, 1, true),
    "[ true]"   -> (null, JARR, 1, true),
    "[true ]"   -> (null, JARR, 1, true),
    "[ true ]"  -> (null, JARR, 1, true),
    
    "[false]"   -> (null, JARR, 1, false),
    "[ false]"  -> (null, JARR, 1, false),
    "[false ]"  -> (null, JARR, 1, false),
    "[ false ]" -> (null, JARR, 1, false),
    
    "[null]"   -> (null, JARR, 1, null),
    "[ null]"  -> (null, JARR, 1, null),
    "[null ]"  -> (null, JARR, 1, null),
    "[ null ]" -> (null, JARR, 1, null),
    
    "[\"a\"]"   -> (null, JARR, 1, "a"),
    "[ \"a\"]"  -> (null, JARR, 1, "a"),
    "[\"a\" ]"  -> (null, JARR, 1, "a"),
    "[ \"a\" ]" -> (null, JARR, 1, "a"),
    
    "[\"ab\"]"   -> (null, JARR, 1, "ab"),
    "[ \"ab\"]"  -> (null, JARR, 1, "ab"),
    "[\"ab\" ]"  -> (null, JARR, 1, "ab"),
    "[ \"ab\" ]" -> (null, JARR, 1, "ab"),
    
    "[\"\\\"\"]"   -> (null, JARR, 1, "\""),
    """["\/"]"""   -> (null, JARR, 1, "/"),
    """["\\"]"""   -> (null, JARR, 1, "\\"),
    "[\"a\\\"\"]"  -> (null, JARR, 1, "a\""),
    """["a\/"]"""  -> (null, JARR, 1, "a/"),
    """["a\\"]"""  -> (null, JARR, 1, "a\\"),
    "[\"\\\"b\"]"  -> (null, JARR, 1, "\"b"),
    """["\/b"]"""  -> (null, JARR, 1, "/b"),
    """["\\b"]"""  -> (null, JARR, 1, "\\b"),
    "[\"a\\\"b\"]" -> (null, JARR, 1, "a\"b"),
    """["a\/b"]""" -> (null, JARR, 1, "a/b"),
    """["a\\b"]""" -> (null, JARR, 1, "a\\b"),
    
    "[\"\\b\"]"   -> (null, JARR, 1, "\b"),
    "[\"\\f\"]"   -> (null, JARR, 1, "\f"),
    "[\"\\n\"]"   -> (null, JARR, 1, "\n"),
    "[\"\\r\"]"   -> (null, JARR, 1, "\r"),
    "[\"\\t\"]"   -> (null, JARR, 1, "\t"),
    "[ \"\\b\"]"  -> (null, JARR, 1, "\b"),
    "[ \"\\f\"]"  -> (null, JARR, 1, "\f"),
    "[ \"\\n\"]"  -> (null, JARR, 1, "\n"),
    "[ \"\\r\"]"  -> (null, JARR, 1, "\r"),
    "[ \"\\t\"]"  -> (null, JARR, 1, "\t"),
    "[\"\\b\" ]"  -> (null, JARR, 1, "\b"),
    "[\"\\f\" ]"  -> (null, JARR, 1, "\f"),
    "[\"\\n\" ]"  -> (null, JARR, 1, "\n"),
    "[\"\\r\" ]"  -> (null, JARR, 1, "\r"),
    "[\"\\t\" ]"  -> (null, JARR, 1, "\t"),
    "[ \"\\b\" ]" -> (null, JARR, 1, "\b"),
    "[ \"\\f\" ]" -> (null, JARR, 1, "\f"),
    "[ \"\\n\" ]" -> (null, JARR, 1, "\n"),
    "[ \"\\r\" ]" -> (null, JARR, 1, "\r"),
    "[ \"\\t\" ]" -> (null, JARR, 1, "\t"),
    
    "[\"a\\n\"]"   -> (null, JARR, 1, "a\n"),
    "[ \"a\\n\"]"  -> (null, JARR, 1, "a\n"),
    "[\"a\\n\" ]"  -> (null, JARR, 1, "a\n"),
    "[ \"a\\n\" ]" -> (null, JARR, 1, "a\n"),
    
    "[\"\\na\"]"   -> (null, JARR, 1, "\na"),
    "[ \"\\na\"]"  -> (null, JARR, 1, "\na"),
    "[\"\\na\" ]"  -> (null, JARR, 1, "\na"),
    "[ \"\\na\" ]" -> (null, JARR, 1, "\na"),
    
    "[\"\u8899\"]"   -> (null, JARR, 1, "\u8899"),
    "[ \"\u8899\"]"  -> (null, JARR, 1, "\u8899"),
    "[\"\u8899\" ]"  -> (null, JARR, 1, "\u8899"),
    "[ \"\u8899\" ]" -> (null, JARR, 1, "\u8899"),
    "[\"a\u8899\"]"  -> (null, JARR, 1, "a\u8899"),
    "[\"\u8899b\"]"  -> (null, JARR, 1, "\u8899b"),
    "[\"a\u8899b\"]" -> (null, JARR, 1, "a\u8899b"),
    
    "[\"\\u8899\"]"   -> (null, JARR, 1, "\u8899"),
    "[\"a\\u8899\"]"  -> (null, JARR, 1, "a\u8899"),
    "[\"\\u8899b\"]"  -> (null, JARR, 1, "\u8899b"),
    "[\"a\\u8899b\"]" -> (null, JARR, 1, "a\u8899b"),
    
    "[\"\u0298\"]"   -> (null, JARR, 1, "\u0298"),
    "[\"\u0029\"]"   -> (null, JARR, 1, "\u0029"),
    "[\"\u000d\"]"   -> (null, JARR, 1, "\u000d"),
    "[\"\u000dX\"]"  -> (null, JARR, 1, "\u000dX"),
    "[\"Z\u000dX\"]" -> (null, JARR, 1, "Z\u000dX"),
    
    """["\u8899\u0298"]"""   -> (null, JARR, 1, "\u8899\u0298"),
    """["\u8899H\u0298"]"""  -> (null, JARR, 1, "\u8899H\u0298"),
    
    "[\"\\u8899\\u0298\"]"   -> (null, JARR, 1, "\u8899\u0298"),
    "[\"\\u8899H\\u0298\"]"  -> (null, JARR, 1, "\u8899H\u0298"),
    
    "[1]"     -> (null, JARR, 1, 1L),
    "[12]"    -> (null, JARR, 1, 12L),
    "[123]"   -> (null, JARR, 1, 123L),
    "[ 1]"    -> (null, JARR, 1, 1L),
    "[ 12]"   -> (null, JARR, 1, 12L),
    "[ 123]"  -> (null, JARR, 1, 123L),
    "[1 ]"    -> (null, JARR, 1, 1L),
    "[12 ]"   -> (null, JARR, 1, 12L),
    "[123 ]"  -> (null, JARR, 1, 123L),
    "[ 1 ]"   -> (null, JARR, 1, 1L),
    "[ 12 ]"  -> (null, JARR, 1, 12L),
    "[ 123 ]" -> (null, JARR, 1, 123L),
    
    "[-1]"     -> (null, JARR, 1, -1L),
    "[-12]"    -> (null, JARR, 1, -12L),
    "[-123]"   -> (null, JARR, 1, -123L),
    "[ -1]"    -> (null, JARR, 1, -1L),
    "[ -12]"   -> (null, JARR, 1, -12L),
    "[ -123]"  -> (null, JARR, 1, -123L),
    "[-1 ]"    -> (null, JARR, 1, -1L),
    "[-12 ]"   -> (null, JARR, 1, -12L),
    "[-123 ]"  -> (null, JARR, 1, -123L),
    "[ -1 ]"   -> (null, JARR, 1, -1L),
    "[ -12 ]"  -> (null, JARR, 1, -12L),
    "[ -123 ]" -> (null, JARR, 1, -123L),
    
    "[1.5]"     -> (null, JARR, 1, 1.5D),
    "[1.5 ]"    -> (null, JARR, 1, 1.5D),
    "[ 1.5]"    -> (null, JARR, 1, 1.5D),
    "[ 1.5 ]"   -> (null, JARR, 1, 1.5D),
    "[1.52]"    -> (null, JARR, 1, 1.52D),
    "[ 1.52]"   -> (null, JARR, 1, 1.52D),
    "[1.52 ]"   -> (null, JARR, 1, 1.52D),
    "[ 1.52 ]"  -> (null, JARR, 1, 1.52D),
    "[31.52]"   -> (null, JARR, 1, 31.52D),
    "[ 31.52]"  -> (null, JARR, 1, 31.52D),
    "[31.52 ]"  -> (null, JARR, 1, 31.52D),
    "[ 31.52 ]" -> (null, JARR, 1, 31.52D),
    
    "[0]"    -> (null, JARR, 1, 0L),
    "[0.1]"  -> (null, JARR, 1, 0.1D),
    "[0.15]" -> (null, JARR, 1, 0.15D),
    
    "[-1.5]"     -> (null, JARR, 1, -1.5D),
    "[-1.5 ]"    -> (null, JARR, 1, -1.5D),
    "[ -1.5]"    -> (null, JARR, 1, -1.5D),
    "[ -1.5 ]"   -> (null, JARR, 1, -1.5D),
    "[-1.52]"    -> (null, JARR, 1, -1.52D),
    "[ -1.52]"   -> (null, JARR, 1, -1.52D),
    "[-1.52 ]"   -> (null, JARR, 1, -1.52D),
    "[ -1.52 ]"  -> (null, JARR, 1, -1.52D),
    "[-31.52]"   -> (null, JARR, 1, -31.52D),
    "[ -31.52]"  -> (null, JARR, 1, -31.52D),
    "[-31.52 ]"  -> (null, JARR, 1, -31.52D),
    "[ -31.52 ]" -> (null, JARR, 1, -31.52D),
    
    "[1.5e3]"     -> (null, JARR, 1, 1.5e3D),
    "[1.5E3]"     -> (null, JARR, 1, 1.5E3D),
    "[1.5e23]"    -> (null, JARR, 1, 1.5e23D),
    "[1.5E23]"    -> (null, JARR, 1, 1.5E23D),
    "[ 1.5e3]"    -> (null, JARR, 1, 1.5e3D),
    "[ 1.5E3]"    -> (null, JARR, 1, 1.5E3D),
    "[ 1.5e23]"   -> (null, JARR, 1, 1.5e23D),
    "[ 1.5E23]"   -> (null, JARR, 1, 1.5E23D),
    "[1.5e3 ]"    -> (null, JARR, 1, 1.5e3D),
    "[1.5E3 ]"    -> (null, JARR, 1, 1.5E3D),
    "[1.5e23 ]"   -> (null, JARR, 1, 1.5e23D),
    "[1.5E23 ]"   -> (null, JARR, 1, 1.5E23D),
    "[ 1.5e3 ]"   -> (null, JARR, 1, 1.5e3D),
    "[ 1.5E3 ]"   -> (null, JARR, 1, 1.5E3D),
    "[ 1.5e23 ]"  -> (null, JARR, 1, 1.5e23D),
    "[ 1.5E23 ]"  -> (null, JARR, 1, 1.5E23D),
    
    "[-1.5e3]"     -> (null, JARR, 1, -1.5e3D),
    "[-1.5E3]"     -> (null, JARR, 1, -1.5E3D),
    "[-1.5e23]"    -> (null, JARR, 1, -1.5e23D),
    "[-1.5E23]"    -> (null, JARR, 1, -1.5E23D),
    "[ -1.5e3]"    -> (null, JARR, 1, -1.5e3D),
    "[ -1.5E3]"    -> (null, JARR, 1, -1.5E3D),
    "[ -1.5e23]"   -> (null, JARR, 1, -1.5e23D),
    "[ -1.5E23]"   -> (null, JARR, 1, -1.5E23D),
    "[-1.5e3 ]"    -> (null, JARR, 1, -1.5e3D),
    "[-1.5E3 ]"    -> (null, JARR, 1, -1.5E3D),
    "[-1.5e23 ]"   -> (null, JARR, 1, -1.5e23D),
    "[-1.5E23 ]"   -> (null, JARR, 1, -1.5E23D),
    "[ -1.5e3 ]"   -> (null, JARR, 1, -1.5e3D),
    "[ -1.5E3 ]"   -> (null, JARR, 1, -1.5E3D),
    "[ -1.5e23 ]"  -> (null, JARR, 1, -1.5e23D),
    "[ -1.5E23 ]"  -> (null, JARR, 1, -1.5E23D),
    
    "[1.5e+3]"     -> (null, JARR, 1, 1.5e3D),
    "[1.5E+3]"     -> (null, JARR, 1, 1.5E3D),
    "[1.5e+23]"    -> (null, JARR, 1, 1.5e23D),
    "[1.5E+23]"    -> (null, JARR, 1, 1.5E23D),
    "[ 1.5e+3]"    -> (null, JARR, 1, 1.5e3D),
    "[ 1.5E+3]"    -> (null, JARR, 1, 1.5E3D),
    "[ 1.5e+23]"   -> (null, JARR, 1, 1.5e23D),
    "[ 1.5E+23]"   -> (null, JARR, 1, 1.5E23D),
    "[1.5e+3 ]"    -> (null, JARR, 1, 1.5e3D),
    "[1.5E+3 ]"    -> (null, JARR, 1, 1.5E3D),
    "[1.5e+23 ]"   -> (null, JARR, 1, 1.5e23D),
    "[1.5E+23 ]"   -> (null, JARR, 1, 1.5E23D),
    "[ 1.5e+3 ]"   -> (null, JARR, 1, 1.5e3D),
    "[ 1.5E+3 ]"   -> (null, JARR, 1, 1.5E3D),
    "[ 1.5e+23 ]"  -> (null, JARR, 1, 1.5e23D),
    "[ 1.5E+23 ]"  -> (null, JARR, 1, 1.5E23D),
    
    "[-1.5e+3]"     -> (null, JARR, 1, -1.5e3D),
    "[-1.5E+3]"     -> (null, JARR, 1, -1.5E3D),
    "[-1.5e+23]"    -> (null, JARR, 1, -1.5e23D),
    "[-1.5E+23]"    -> (null, JARR, 1, -1.5E23D),
    "[ -1.5e+3]"    -> (null, JARR, 1, -1.5e3D),
    "[ -1.5E+3]"    -> (null, JARR, 1, -1.5E3D),
    "[ -1.5e+23]"   -> (null, JARR, 1, -1.5e23D),
    "[ -1.5E+23]"   -> (null, JARR, 1, -1.5E23D),
    "[-1.5e+3 ]"    -> (null, JARR, 1, -1.5e3D),
    "[-1.5E+3 ]"    -> (null, JARR, 1, -1.5E3D),
    "[-1.5e+23 ]"   -> (null, JARR, 1, -1.5e23D),
    "[-1.5E+23 ]"   -> (null, JARR, 1, -1.5E23D),
    "[ -1.5e+3 ]"   -> (null, JARR, 1, -1.5e3D),
    "[ -1.5E+3 ]"   -> (null, JARR, 1, -1.5E3D),
    "[ -1.5e+23 ]"  -> (null, JARR, 1, -1.5e23D),
    "[ -1.5E+23 ]"  -> (null, JARR, 1, -1.5E23D),
    
    "[1.5e-3]"     -> (null, JARR, 1, 1.5e-3D),
    "[1.5E-3]"     -> (null, JARR, 1, 1.5E-3D),
    "[1.5e-23]"    -> (null, JARR, 1, 1.5e-23D),
    "[1.5E-23]"    -> (null, JARR, 1, 1.5E-23D),
    " [1.5e-3]"    -> (null, JARR, 1, 1.5e-3D),
    " [1.5E-3]"    -> (null, JARR, 1, 1.5E-3D),
    " [1.5e-23]"   -> (null, JARR, 1, 1.5e-23D),
    " [1.5E-23]"   -> (null, JARR, 1, 1.5E-23D),
    "[1.5e-3] "    -> (null, JARR, 1, 1.5e-3D),
    "[1.5E-3] "    -> (null, JARR, 1, 1.5E-3D),
    "[1.5e-23] "   -> (null, JARR, 1, 1.5e-23D),
    "[1.5E-23] "   -> (null, JARR, 1, 1.5E-23D),
    " [1.5e-3] "   -> (null, JARR, 1, 1.5e-3D),
    " [1.5E-3] "   -> (null, JARR, 1, 1.5E-3D),
    " [1.5e-23] "  -> (null, JARR, 1, 1.5e-23D),
    " [1.5E-23] "  -> (null, JARR, 1, 1.5E-23D),
    
    "[-1.5e-3]"     -> (null, JARR, 1, -1.5e-3D),
    "[-1.5E-3]"     -> (null, JARR, 1, -1.5E-3D),
    "[-1.5e-23]"    -> (null, JARR, 1, -1.5e-23D),
    "[-1.5E-23]"    -> (null, JARR, 1, -1.5E-23D),
    " [-1.5e-3]"    -> (null, JARR, 1, -1.5e-3D),
    " [-1.5E-3]"    -> (null, JARR, 1, -1.5E-3D),
    " [-1.5e-23]"   -> (null, JARR, 1, -1.5e-23D),
    " [-1.5E-23]"   -> (null, JARR, 1, -1.5E-23D),
    "[-1.5e-3] "    -> (null, JARR, 1, -1.5e-3D),
    "[-1.5E-3] "    -> (null, JARR, 1, -1.5E-3D),
    "[-1.5e-23] "   -> (null, JARR, 1, -1.5e-23D),
    "[-1.5E-23] "   -> (null, JARR, 1, -1.5E-23D),
    " [-1.5e-3] "   -> (null, JARR, 1, -1.5e-3D),
    " [-1.5E-3] "   -> (null, JARR, 1, -1.5E-3D),
    " [-1.5e-23] "  -> (null, JARR, 1, -1.5e-23D),
    " [-1.5E-23] "  -> (null, JARR, 1, -1.5E-23D),
    
    "[7,8]"     -> (null, JARR, 2, 7L),
    "[ 7,8]"    -> (null, JARR, 2, 7L),
    "[7,8 ]"    -> (null, JARR, 2, 7L),
    "[7, 8]"    -> (null, JARR, 2, 7L),
    "[7 , 8]"   -> (null, JARR, 2, 7L),
    " [7, 8]"   -> (null, JARR, 2, 7L),
    " [7, 8] "  -> (null, JARR, 2, 7L),
    
    "[17,18]"     -> (null, JARR, 2, 17L),
    "[ 17,18]"    -> (null, JARR, 2, 17L),
    "[17,18 ]"    -> (null, JARR, 2, 17L),
    "[17, 18]"    -> (null, JARR, 2, 17L),
    "[17 , 18]"   -> (null, JARR, 2, 17L),
    " [17, 18]"   -> (null, JARR, 2, 17L),
    " [17, 18] "  -> (null, JARR, 2, 17L),
    
    """["a","b"]"""     -> (null, JARR, 2, "a"),
    """["ax","by"]"""     -> (null, JARR, 2, "ax"),
    
    "[7,8, 9]"     -> (null, JARR, 3, 7L),
    """["ax","by", "cz"]"""     -> (null, JARR, 3, "ax"),
    
    "[[]]"   -> (null, JARR, 1, JsonArray.empty),
    "[ []]"  -> (null, JARR, 1, JsonArray.empty),
    "[[] ]"  -> (null, JARR, 1, JsonArray.empty),
    "[ [] ]" -> (null, JARR, 1, JsonArray.empty),
    
    "[[],[]]"   -> (null, JARR, 2, JsonArray.empty),
    "[[], []]"  -> (null, JARR, 2, JsonArray.empty),
    "[[] ,[]]"  -> (null, JARR, 2, JsonArray.empty),
    "[[] , []]" -> (null, JARR, 2, JsonArray.empty),
    
    "[[23],[]]"   ->
      (null, JARR, 2, JsonArray(JsonInteger(23))),
    "[[23, 45], []]" ->
      (null, JARR, 2, JsonArray(JsonInteger(23), JsonInteger(45))),
    "[[23], [45]]" ->
      (null, JARR, 2, JsonArray(JsonInteger(23))),
    "[[], [23, 45]]" ->
      (null, JARR, 2, JsonArray.empty),
    
    "[{}]"   -> (null, JARR, 1, JsonObject.empty),
    "[ {}]"  -> (null, JARR, 1, JsonObject.empty),
    "[{} ]"  -> (null, JARR, 1, JsonObject.empty),
    "[ {} ]" -> (null, JARR, 1, JsonObject.empty),
    
    "[{},{}]"   -> (null, JARR, 2, JsonObject.empty),
    "[{}, {}]"  -> (null, JARR, 2, JsonObject.empty),
    "[{} ,{}]"  -> (null, JARR, 2, JsonObject.empty),
    "[{} , {}]" -> (null, JARR, 2, JsonObject.empty),
    
    """{"x": 5}"""    -> (null, JOBJ, 1, ("x", 5)),
    """{"x": 123}"""  -> (null, JOBJ, 1, ("x", 123)),
    """{"xy": 123}""" -> (null, JOBJ, 1, ("xy", 123)),
    """{"xy": []}"""  -> (null, JOBJ, 1, ("xy", JsonArray.empty)),
    """{"xy": [6]}""" -> (null, JOBJ, 1, ("xy", JsonArray(JsonInteger(6)))),
    """{"xy": {}}"""  -> (null, JOBJ, 1, ("xy", JsonObject.empty)),
    
    """{"xy": {"z": 97}}""" -> (null, JOBJ, 1, ("xy", JsonObject("z" -> JsonInteger(97)))),
    """{"xy": [3,4,5]}""" -> (null, JOBJ, 1, ("xy", JsonArray(JsonInteger(3), JsonInteger(4), JsonInteger(5)))),
    
    "{"   -> failure,
    "{ "  -> failure,
    "{  " -> failure,
    " {"  -> failure,
    " { " -> failure,
    "{ "  -> failure,
    
    "["   -> failure,
    "[ "  -> failure,
    "[  " -> failure,
    " ["  -> failure,
    " [ " -> failure,
    "[ "  -> failure,
    
    "}"   -> failure,
    " }"  -> failure,
    "  }" -> failure,
    " }"  -> failure,
    " } " -> failure,
    "} "  -> failure,
    
    "]"   -> failure,
    " ]"  -> failure,
    "  ]" -> failure,
    " ]"  -> failure,
    " ] " -> failure,
    "] "  -> failure,
    
    "1"    -> failure,
    "1 "   -> failure,
    " 1"   -> failure,
    " 1 "  -> failure,
    "12"   -> failure,
    " 12"  -> failure,
    "12 "  -> failure,
    " 12 " -> failure,
    
    "\"a\""    -> failure,
    "\"a\" "   -> failure,
    " \"a\""   -> failure,
    " \"a\" "  -> failure,
    "\"ab\""   -> failure,
    " \"ab\""  -> failure,
    "\"ab\" "  -> failure,
    " \"ab\" " -> failure,
    
    "true"   -> failure,
    "true "  -> failure,
    " true"  -> failure,
    " true " -> failure,
    
    "empty"     -> ("", JSER, 0, null),
    "empty1spc" -> (" ", JSER, 0, null),
    "empty2spc" -> ("  ", JSER, 0, null)
  )
  val failure = (null, JSER, 0, null)
}

class JsonParserSpec extends FunSuite {
  
  import JsonParserSpec._
  
  test("matching of JsonValue subclasses must be exhaustive") {
    
    // doesn't do any runtime tests- just checks this compiles
    
    def matchAll(json: JsonValue[_]) = json match {
      case str: JsonString => val s: String = str.value
      case int: JsonInteger=> val i: Long = int.value
      case flo: JsonFloat => val f: Double = flo.value
      case arr: JsonArray => val v: Vector[JsonValue[_]] = arr.value
      case obj: JsonObject => val m: Map[String, JsonValue[_]] = obj.value
      case JsonFalse => assert(JsonFalse.value == false)
      case JsonTrue => assert(JsonTrue.value == false)
      case JsonNull => // JsonNull.value == null
    }
    
    def matchNumber(num: JsonNumber[_ <: AnyVal]) = num match {
      case int: JsonInteger=> val i: Long = int.value
      case flo: JsonFloat => val f: Double = flo.value
    }
    
    def matchBoolean(boo: JsonBoolean) = boo match {
      case JsonFalse => assert(JsonFalse.value == false)
      case JsonTrue => assert(JsonTrue.value == true)
    }
    
    def matchTop(top: JsonTop[_]) = top match {
      case arr: JsonArray => val v: Vector[JsonValue[_]] = arr.value
      case obj: JsonObject => val m: Map[String, JsonValue[_]] = obj.value
    }
  }
  
  texts.keys foreach { caseName: String =>
    test("test parsing of JSON text '" + caseName + "'") {
      val expected = texts(caseName)
      val text: String = if (expected._1 == null) caseName else expected._1
      val expectedClass: Class[AnyRef] = expected._2
      val expectedSize: Int = expected._3
      val expectedValue: Any = expected._4
      val expectedClassName = expectedClass.getSimpleName
      var jval: JsonTop[_] = null
      val res: Either[Throwable, JsonTop[_]] = try {
        Right(JsonParser.parseTop(text))
      } catch {
        case err: Throwable => Left(err)
      }
      if (THBL.isAssignableFrom(expectedClass)) {
        assert(
          res.isLeft && {
            val gotClass = nongenericClass(res.left.get.getClass)
            gotClass == expectedClass
          },
          "throw an exception with class: " + expectedClassName
        )
      } else {
        var gotSize: Int = 0;
        assert( res.isRight, "parse properly")
        assert(
          {
            jval = res.right.get
            val gotClass = nongenericClass(res.right.get.getClass)
            gotClass == expectedClass
          },
          "have a result with the class: " + expectedClassName
        )
        
        assert(
          {
            gotSize = jval match {
              case jarr: JsonArray => jarr.length
              case jobj: JsonObject => jobj.size
              case _ => 0
            }
            gotSize == expectedSize
          },
          "have a result with size: " + expectedSize + " (jval=" + jval + ")"
        )
        
        {
          if (gotSize > 0) {
            val dontUnwrap1 = expectedValue.isInstanceOf[JsonValue[_]]
            val dontUnwrap2 = expectedValue.isInstanceOf[Tuple2[_,_]] && {
              val v = expectedValue.asInstanceOf[Tuple2[_,_]]._2
              v.isInstanceOf[JsonValue[_]]
            }
            val dontUnwrap = dontUnwrap1 || dontUnwrap2
            val unwrap = !dontUnwrap
            val firstElem: Any = jval match {
              case jarr: JsonArray =>
                if (unwrap) jarr.head.value else jarr.head
              case jobj: JsonObject => {
                val pair = jobj.head
                if (unwrap) (pair._1, pair._2.value) else pair
              }
              case _ => null
            }
            if (firstElem != null) {
              assert(nongenericClass(firstElem.getClass) ==
                nongenericClass(expectedValue.getClass))
            }
            assert(firstElem == expectedValue)
          } else {
            assert(gotSize == 0)
          }
        }
        
        assert(
          {
            val jval2Either: Either[Throwable, JsonValue[_]] = try {
              Right(JsonParser.parseTop(jval.toString))
            } catch {
              case err: Throwable => Left(err)
            }
            jval2Either.isRight && {
              val jval2: JsonValue[_] = jval2Either.right.get
              (jval2: AnyRef) == (jval: AnyRef)
            }
          },
          "toString and then parse again must be idempotent"
        )
        assert(
          {
            val jval2Either: Either[Throwable, JsonValue[_]] = try {
              Right(JsonParser.parseTop(jval.toCompactString))
            } catch {
              case err: Throwable => Left(err)
            }
            jval2Either.isRight && {
              val jval2: JsonValue[_] = jval2Either.right.get
              (jval2: AnyRef) == (jval: AnyRef)
            }
          },
          "toCompactString and then parse again must be idempotent"
        )
        assert(
          {
            val jval2Either: Either[Throwable, JsonValue[_]] = try {
              Right(JsonParser.parseTop(jval.toPrettyString))
            } catch {
              case err: Throwable => Left(err)
            }
            jval2Either.isRight && {
              val jval2: JsonValue[_] = jval2Either.right.get
              (jval2: AnyRef) == (jval: AnyRef)
            }
          },
          "toPrettyString and then parse again must be idempotent"
        )
      }
    }
  }
  
}