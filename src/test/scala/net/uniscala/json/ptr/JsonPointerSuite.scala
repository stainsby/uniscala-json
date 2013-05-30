package net.uniscala.json.ptr

import org.scalatest.FunSuite

import net.uniscala.json._
import Json._


object JsonPointerSuite {
  
  def assertApplicationsPtr[J <: JsonValue[_] : Manifest](
    pointerStr: String,
    applications: (JsonValue[_], Option[JsonValue[_]])*
  ): Unit = {
    val ptr = Pointer(pointerStr)
    applications.foreach { case (source, expectedResult) =>
      val result = ptr.apply[J](source)
      assert(
        result == expectedResult,
        "pointer " + ptr + " applied to " + source + " yielded " + result +
        " but the expected result is " + expectedResult
        
      )
    }
  }
  
  def assertApplications[J <: JsonValue[_] : Manifest](
    value: JsonValue[_],
    applications: (String, Option[JsonValue[_]])*
  ): Unit = {
    applications.foreach { case (pointerStr, expectedResult) =>
      val ptr = Pointer(pointerStr)
      val result = ptr.apply[J](value)
      assert(
        result == expectedResult,
        "pointer " + ptr + " applied to " + value + " yielded " + result +
        " but the expected result is " + expectedResult
        
      )
    }
  }
  
}


class JsonPointerSuite extends FunSuite {
  
  import JsonPointerSuite._
  
  test("pointer application") {
    
    val value1 = Json(
      "xx" -> 99,
      "" -> "EMPTY",
      "aa" -> Json(
        "bb" -> "S",
        "yy" -> Json(Jnull, true, false)
      ),
      "a~a" -> Json(
        "bb" -> 22,
        "y/y" -> Json(56.78, 45.3, 66.1),
        "y/~/y" -> Json(16.78, 15.3, 16.1)
      )
    )
    
    assertApplications[JsonValue[_]](
      value1,
      ("/xx", Some(99)),
      ("/kk", None),
      ("/", Some("EMPTY")),
      ("", Some(value1)),
      ("/aa/yy/0", Some(Jnull)),
      ("/aa/yy/1", Some(true)),
      ("/aa/yy/2", Some(false)),
      ("/aa/yy/3", None),
      ("/aa/yy/-1", None),
      ("/a~0a/y~1y/1", Some(45.3)),
      ("/a~0a/y~1~0~1y/0", Some(16.78))
    )
    
    assertApplications[JsonFloat](
      value1,
      ("/xx", None),
      ("/kk", None),
      ("", None),
      ("/aa/yy/0", None),
      ("/aa/yy/1", None),
      ("/aa/yy/2", None),
      ("/aa/yy/3", None),
      ("/aa/yy/-1", None),
      ("/a~0a/y~1y/1", Some(45.3)),
      ("/a~0a/y~1~0~1y/0", Some(16.78))
    )
    
    assertApplications[JsonValue[_]](
      JsonObject(),
      ("/xx", None),
      ("/kk", None),
      ("", Some(JsonObject())),
      ("/aa/yy/0", None)
    )
    
    assertApplications[JsonValue[_]](
      JsonArray(),
      ("/xx", None),
      ("/kk", None),
      ("", Some(JsonArray())),
      ("/aa/yy/0", None)
    )
    
    assertApplications[JsonValue[_]](
      JsonInteger(116),
      ("/xx", None),
      ("/kk", None),
      ("", Some(JsonInteger(116))),
      ("/aa/yy/0", None)
    )
    
    assertApplications[JsonBoolean](
      JsonInteger(116),
      ("/xx", None),
      ("/kk", None),
      ("", None),
      ("/aa/yy/0", None)
    )
    
    // examples from:
    // http://tools.ietf.org/html/draft-ietf-appsawg-json-pointer-07
    
    val valueIetf = Json(
      "foo" -> Json("bar", "baz"),
      "" -> 0,
      "a/b" -> 1,
      "c%d" -> 2,
      "e^f" -> 3,
      "g|h" -> 4,
      "i\\j" -> 5,
      "k\"l" -> 6,
      " " -> 7,
      "m~n" -> 8
    )
    
    assertApplications[JsonValue[_]](
      valueIetf,
      ("", Some(valueIetf)),
      ("/foo",Some(Json("bar", "baz"))),
      ("/foo/0", Some("bar")),
      ("/",  Some(0)),
      ("/a~1b", Some(1)),
      ("/c%d", Some(2)),
      ("/e^f", Some(3)),
      ("/g|h", Some(4)),
      ("/i\\j", Some(5)),
      ("/k\"l", Some(6)),
      ("/ ", Some(7)),
      ("/m~0n", Some(8))
    )
  }
}
