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

import org.specs2.mutable._
import org.specs2.specification.Scope
import org.specs2.specification.Outside

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
          "key"    -> "ER45DFE3",
          "secret" -> "0867986769de8"
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
}


class JsonObjectSpec extends Specification {
  
  import JsonObjectSpec._
  import TestData._
  import Json._
  
  sequential
  
  "getAt" should {
    "act like identity for the root path" in {
      user1.getAt[JsonObject](JsonPath.root) must beEqualTo(Some(user1))
    }
    "work for all depths" in {
      user1.getAt[JsonValue[_]](JsonPath.root / "type") must
        beEqualTo(Some(JsonString("user")))
      user1.getAt[JsonValue[_]](JsonPath.root / "profiles" / "alt") must
        beEqualTo(Some(Json("key"    -> "ER45DFE3", "secret" -> "0867986769de8")))
      user1.getAt[JsonValue[_]](JsonPath.root / "and") must
        beEqualTo(Some(JsonInteger(123)))
      user1.getAt[JsonValue[_]](JsonPath.root / "n") must
        beEqualTo(Some(JsonNull))
      user1.getAt[JsonValue[_]](JsonPath.root / "t") must
        beEqualTo(Some(JsonTrue))
      user1.getAt[JsonValue[_]](JsonPath.root / "f") must
        beEqualTo(Some(JsonFalse))
      user1.getAt[JsonValue[_]](JsonPath.root / "d") must
        beEqualTo(Some(JsonFloat(34.567)))
      user1.getAt[JsonValue[_]](JsonPath.root / "more" / "stuff") must
        beEqualTo(Some(Json(1, "aa", Json("bb" -> "BB"))))
    }
  }
  
  "the tree map operation" should {
    
    "always map an empty object to an empty object" in {
      JsonObject().treeMap((j) => JsonString("NEVER")) must
        beEqualTo(JsonObject())
    }
    
    "be idempotent with the identity function" in {
      user1.treeMap((j) => j) must beEqualTo(user1)
    }
    
    "be able to map over a tree structure" in {
      (user1 treeMap { j => 
        j match {
          case jobj: JsonObject => jobj
          case _ => JsonString(toClassName(j))
        }
      }) must
        beEqualTo(user1MappedToTypeNamesTree)
    }
    
    "be able to flatten a tree" in {
      (user1 treeMap { j => JsonString(toClassName(j)) }) must
        beEqualTo(user1MappedToTypeNamesFlat)
    }
  }
  
  "the tree collect operation" should {
    
    "always collect an empty object to an empty object" in {
      JsonObject() treeCollect {case j: JsonValue[_] =>
        JsonString("NEVER")
      } must beEqualTo(JsonObject())
    }
    
    "be idempotent with the identity function" in {
      user1.treeCollect { case j: JsonValue[_] => j } must beEqualTo(user1)
    }
    
    "be able to collect over a tree structure" in {
      (user1 treeCollect {
        case jobj: JsonObject => jobj
        case j: JsonInteger => JsonString(toClassName(j))
      }) must
        beEqualTo(user1CollectedIntegersTree)
    }
    
    "be able to flatten a tree" in {
      (user1 treeCollect { case j: JsonInteger => JsonString(toClassName(j)) }) must
        beEqualTo(user1CollectedIntegersFlat)
    }
  }
  
  "the path map operation" should {
    
    "always map an empty object to an empty object" in {
      JsonObject().pathMap((pj) => JsonString("NEVER")) must
        beEqualTo(JsonObject())
    }
    
    "be idempotent with the identity function" in {
      user1.pathMap((pj) => pj._2) must beEqualTo(user1)
    }
    
    "be able to map over a tree structure" in {
      (user1 pathMap { pj => 
        pj match {
          case (_, jobj: JsonObject) => jobj
          case (path, json) =>
            JsonString(path.toString + "_" + toClassName(json))
        }
      }) must
        beEqualTo(user1PathMappedToTypeNamesTree) 
    }
  }
  
  "the path collect operation" should {
    
    "always collect an empty object to an empty object" in {
      JsonObject() pathCollect { case pj: JsonValue[_] =>
        JsonString("NEVER")
      } must beEqualTo(JsonObject())
    }
    
    "be idempotent with the identity function" in {
      user1.pathCollect { case (p, j) => j } must beEqualTo(user1)
    }
    
    "be able to collect over a tree structure" in {
      (user1 pathCollect {
        case (path, jobj: JsonObject) => jobj
        case (path, jint: JsonInteger) =>
          JsonString(path.toString+ "_" + toClassName(jint))
      }) must
        beEqualTo(user1PathCollectedIntegersTree)
    }
  }

}