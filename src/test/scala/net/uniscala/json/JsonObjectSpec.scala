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

import org.scalatest.FunSuite


object JsonObjectSpec {
  
  import Json._
  
  object TestData {
    
    val user1 = Json(
      "type" -> "user",
      "profiles" -> Json(
        "mybook" -> Json(
          "key"    -> "AGW45HWH",
          "secret" -> "g4juh43ui9g92"
        ),
        "alt" -> Json(
          "key"    -> "ER45D",
          "secret" -> "769d8"
        )        
      ),
      "and" -> 123,
      "even" -> 456,
      "more" -> Json(
        "uninteresting" -> 678,
        "stuff"         -> Json(1, "aa", Json("bb" -> "BB"))
      ),
      "n" -> Jnull,
      "t" -> true,
      "f" -> false,
      "d" -> 34.567
    )
    
    val user1MappedToTypeNamesTree = Json(
      "type" -> "JsonString",
      "profiles" -> Json(
        "mybook" -> Json(
          "key"    -> "JsonString",
          "secret" -> "JsonString"
        ),
        "alt" -> Json(
          "key"    -> "JsonString",
          "secret" -> "JsonString"
        ) 
      ),
      "and" -> "JsonInteger",
      "even" -> "JsonInteger",
      "more" -> Json(
        "uninteresting" -> "JsonInteger",
        "stuff"         -> "JsonArray"
      ),
      "n" -> "JsonNull",
      "t" -> "JsonTrue",
      "f" -> "JsonFalse",
      "d" -> "JsonFloat"
    )
    
    val user1MappedToTypeNamesFlat = Json(
      "type"          -> "JsonString",
      "profiles"      -> "JsonObject",
      "and"           -> "JsonInteger",
      "even"          -> "JsonInteger",
      "more"          -> "JsonObject",
      "n"             -> "JsonNull",
      "t"             -> "JsonTrue",
      "f"             -> "JsonFalse",
      "d"             -> "JsonFloat"
    )
    
    val user1CollectedIntegersTree = Json(
      "profiles" -> Json(
        "mybook" -> JsonObject(),
        "alt"    -> JsonObject() 
      ),
      "and" -> "JsonInteger",
      "even" -> "JsonInteger",
      "more" -> Json(
        "uninteresting" -> "JsonInteger"
      )
    )
    
    val user1CollectedIntegersFlat = Json(
      "and"           -> "JsonInteger",
      "even"          -> "JsonInteger"
    )
    
    val user1PathMappedToTypeNamesTree = Json(
      "type" -> "type_JsonString",
      "profiles" -> Json(
        "mybook" -> Json(
          "key"    -> "profiles:mybook:key_JsonString",
          "secret" -> "profiles:mybook:secret_JsonString"
        ),
        "alt" -> Json(
          "key"    -> "profiles:alt:key_JsonString",
          "secret" -> "profiles:alt:secret_JsonString"
        ) 
      ),
      "and" -> "and_JsonInteger",
      "even" -> "even_JsonInteger",
      "more" -> Json(
        "uninteresting" -> "more:uninteresting_JsonInteger",
        "stuff"         -> "more:stuff_JsonArray"
      ),
      "n" -> "n_JsonNull",
      "t" -> "t_JsonTrue",
      "f" -> "f_JsonFalse",
      "d" -> "d_JsonFloat"
    )
    
    val user1PathCollectedIntegersTree = Json(
      "profiles" -> Json(
        "mybook" -> JsonObject(),
        "alt"    -> JsonObject() 
      ),
      "and" -> "and_JsonInteger",
      "even" -> "even_JsonInteger",
      "more" -> Json(
        "uninteresting" -> "more:uninteresting_JsonInteger"
      )
    )
  }
  
  def toClassName(obj: AnyRef): String =
    obj.getClass.getSimpleName.replace("$", "")
  
  def assertPathValue(
    src: JsonObject,
    pathValues: (JsonPath, JsonValue[_])*
  ) = {
    pathValues.foreach { case(path, expected) =>
      val result = src.getAt[JsonValue[_]](path)
      assert(
        if (expected == None) result == None else result == Some(expected),
        "path " + path + " applied to " + src + " yielded " + result +
          " but the expected result is " + expected
      )
    }
  }
}



class JsonObjectSpec extends FunSuite {
  
  import Json._
  import JsonPath._
  import JsonObjectSpec._
  import TestData._
  
  test("test getAt") {
    assertPathValue(
      user1,
      (/ / "type", "user"),
      (/ / "profiles" / "alt", Json("key" -> "ER45D", "secret" -> "769d8")),
      (/ / "and", 123),
      (/ / "n", Jnull),
      (/ / "t", true),
      (/ / "f", false),
      (/ / "d", 34.567d),
      (/ / "more" / "stuff", Json(1, "aa", Json("bb" -> "BB")))
    )
  }
  
  test("test treeMap") {
    assert(
      JsonObject().treeMap((j) => JsonString("NEVER")) == JsonObject(),
      "always maps an empty object to an empty object"
    )
    assert(
      user1.treeMap((j) => j) == user1,
      "idempotent with the identity function"
    )
    assert(
      user1.treeMap { j => 
        j match {
          case jobj: JsonObject => jobj
          case _ => JsonString(toClassName(j))
        }
      } == user1MappedToTypeNamesTree,
      "able to map over a tree structure"
    )   
    assert(
      user1.treeMap { j =>
        JsonString(toClassName(j)) }
       == user1MappedToTypeNamesFlat,
      "able to flatten a tree"
    )
  }
  
  test("test treeCollect") {
    assert(
      JsonObject().treeCollect {case j: JsonValue[_] =>
        JsonString("NEVER") } == JsonObject(),
      "always collect an empty object to an empty object"
    )
    assert(
      user1.treeCollect { case j: JsonValue[_] => j } == user1,
      "idempotent with the identity function"
    )
    assert(
      (user1 treeCollect {
        case jobj: JsonObject => jobj
        case j: JsonInteger => JsonString(toClassName(j))
      }) == user1CollectedIntegersTree,
      "able to collect over a tree structure"
    )
    assert(
      user1.treeCollect { case j: JsonInteger => JsonString(toClassName(j)) }
      == user1CollectedIntegersFlat,
      "able to flatten a tree"
    )
  }
  
  test("test pathMap") {
    assert(
      JsonObject().pathMap((pj) => JsonString("NEVER")) == JsonObject(),
      "always map an empty object to an empty object"
    )
    assert(
      user1.pathMap((pj) => pj._2) == user1,
      "idempotent with the identity function"
    )
    assert(
      user1.pathMap { pj => 
        pj match {
          case (_, jobj: JsonObject) => jobj
          case (path, json) =>
            JsonString(path.toString + "_" + toClassName(json))
        }
      } == user1PathMappedToTypeNamesTree,
      "able to map over a tree structure"
    )
  }
  
  test("test pathCollect") {
    assert(
      JsonObject().pathCollect { case (p, j) =>
        JsonString("NEVER")
      } == JsonObject(),
      "always collect an empty object to an empty object"
    )
    assert(
      JsonObject().pathCollect { case (p, j) =>
        JsonString("NEVER")
      } == JsonObject(),
      "always collect an empty object to an empty object"
    )
    assert(
      user1.pathCollect { case (p, j) => j } == user1,
      "idempotent with the identity function"
    )
    assert(
      (user1.pathCollect {
        case (path, jobj: JsonObject) => jobj
        case (path, jint: JsonInteger) =>
          JsonString(path.toString+ "_" + toClassName(jint))
      }) == user1PathCollectedIntegersTree,
      "able to collect over a tree structure"
    )
  }
}
