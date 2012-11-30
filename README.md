# README - Uniscala JSON library

## About

Uniscala JSON is a compact JSON library written in Scala. Other 
than the Scala runtime libraries, it has no other dependencies. The library 
is purely for [generating](#Generating), [parsing](#Parsing), 
[rendering](#Rendering) and [transforming](#Transforming) JSON.
There are no domain data/object mapping or binding features.

Uniscala JSON parses and generates [strict JSON][json]. For example, keys 
in JSON object texts must be enclosed in `"` characters (unlike Javascript).

Internally, for simplicity, JSON integers and floats are converted into 
Scala longs and doubles. **Thus, this library may not be suitable for 
applications requiring more digits or accuracy than the Scala Long and 
Double types supply.**

The library has been tested under Scala 2.9.2:

    > test
    ...
    [info] Passed: : Total 2039, Failed 0, Errors 0, Passed 2039, Skipped 0
    

## License

Copyright 2012 Sustainable Software Pty Ltd.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0][license].
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## Contributors

Uniscala JSON is managed and developed by Sam Stainsby at 
[Sustainable Software Pty Ltd][ss]


## Usage


### The JsonValue class hierarchy

JSON is stored in immutable case class/object subclasses of the `JsonValue`
class. The  hierarchy can be used in exhaustive matching. Each `JsonValue` 
instance wraps a native Scala type, which can be access by the `value` member.
The example below illustrates the `JsonValue` inheritance tree and the 
corresponding Scala types that are wrapped by each subclass:

    import net.uniscala.json._
    
    def matchAll(json: JsonValue[_]) = json match {
      case str: JsonString => val s: String = str.value
      case int: JsonInteger=> val i: Long = int.value
      case flo: JsonFloat => val f: Double = flo.value
      case arr: JsonArray => val v: Vector[JsonValue[_]] = arr.value
      case obj: JsonObject => val m: Map[String, JsonValue[_]] = obj.value
      case JsonFalse => assert(JsonFalse.value == false)
      case JsonTrue => assert(JsonTrue.value == false)
      case JsonNull => assert(JsonNull.value eq null)
    }
    
    def matchNumber(num: JsonNumber[_]) = num match {
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


The classes `JsonArray` and `JsonObject` represent top-level JSON texts, and
also fully implement Scala collections, allowing collection operations such 
as `map`, `filter` and `slice`. In many cases, these operations preserve the 
type, yielding JSON-valued results:

    scala> import net.uniscala.json._
    import net.uniscala.json._
    
    scala> import Json._
    import Json._
    
    scala> val jarr1 = Json(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) // see the 'Generating' section
    jarr1: net.uniscala.json.JsonArray = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
    
    scala> val jint = jarr1(3).asInstanceOf[JsonInteger]
    jint: net.uniscala.json.JsonInteger = 3
    
    scala> val jarr2: JsonArray = jarr1.slice(1, 8)
    jarr2: net.uniscala.json.JsonArray = [1, 2, 3, 4, 5, 6, 7]
    
    scala> val jarr3: JsonArray = jarr2 collect { case i: JsonInteger if i.value % 2 == 0 => i }
    jarr3: net.uniscala.json.JsonArray = [2, 4, 6]
    
    scala> jarr2 span { _ match { case i: JsonInteger if i.value < 4 => true; case _ => false } }
    res6: (net.uniscala.json.JsonArray, net.uniscala.json.JsonArray) = ([1, 2, 3],[4, 5, 6, 7])
    
    scala> val jobj1 = Json("a" -> "AAA", "b" -> 333, "c" -> true) // see the 'Generating' section
    jobj1: net.uniscala.json.JsonObject = {"a": "AAA", "b": 333, "c": true}
    
    scala> val jobj2 = jobj1 filter { kv => kv._1 == "b" || kv._1 == "c" }
    jobj2: net.uniscala.json.JsonObject = {"b": 333, "c": true}

Also see the [Transforming](#Transforming) section.


### <a id="Generating"></a>Generating

JsonValue instances can be created directly from the case classes.
For example:

    scala> import net.uniscala.json._
    import net.uniscala.json._

    scala> val jarr = JsonArray(JsonInteger(1), JsonInteger(3), JsonString("abc"), JsonFalse)
    jarr: net.uniscala.json.JsonArray = [1, 3, "abc", false]


However, there are tools for easily generating `JsonValue` instances. These
are accessed by importing `Json._`. This imports implicits to wrap native 
Scala types, the alias `Jnull` for `JsonNull`, and convenient `Json` 
apply methods for creating `JsonArray` and `JsonObject` instances:

    scala> import net.uniscala.json._
    import net.uniscala.json._
    
    scala> import Json._
    import Json._
    
    scala> val jarr = Json(Jtrue, "foo", -1.5e4) 
    jarr: net.uniscala.json.JsonArray = [true, "foo", -15000.0]
    
    scala> val jobj = Json("foo" -> "bar", "x" -> 23)
    jobj: net.uniscala.json.JsonObject = {"foo": "bar", "x": 23}

Note that `Jnull` is needed due to the difficulties of wrapping a Scala `null`:

    scala> val jarr = Json(true, "foo", -1.5e4, null)
    java.lang.NullPointerException ...
    
    scala> val jarr = Json(true, "foo", -1.5e4, Jnull) 
    jarr: net.uniscala.json.JsonArray = [true, "foo", -15000.0, null]


### <a id="Rendering"></a>Rendering

A `JsonValue` instance can be converted into a string in three ways:

  * `toString` - a succinct, single-line format
  * `toCompactString` - a compact, single-line format
  * `toPrettyString` - a multi-line, indented format
  
Example:

    scala> Json("x" -> Json("apple", "banana", "orange"), "y" -> "parrot") toString
    res1: String = {"x": ["apple", "banana", "orange"], "y": "parrot"}
    
    scala> Json("x" -> Json("apple", "banana", "orange"), "y" -> "parrot") toCompactString
    res2: String = {"x":["apple","banana","orange"],"y":"parrot"}
    
    scala> Json("x" -> Json("apple", "banana", "orange"), "y" -> "parrot") toPrettyString
    res3: String = 
    {
      "x": [
        "apple",
        "banana",
        "orange"
      ],
      "y": "parrot"
    }

In each case, the string produced is valid JSON. So, for example, you
can feed top-level JSON back into the parser (see also 'Parsing' below):


    scala> val jobjStr = Json("x" -> Json("apple", "banana", "orange"), "y" -> "parrot").toPrettyString
    jobjStr: String = 
    {
      "x": [
        "apple",
        "banana",
        "orange"
      ],
      "y": "parrot"
    }
    
    scala> val jobt = JsonParser.parseObject(jobjStr)
    jobt: net.uniscala.json.JsonObject = {"x": ["apple", "banana", "orange"], "y": "parrot"}



### <a id="Parsing"></a>Parsing

The parser assumes the text you are going to parse consists of (or at 
least begins with) a top-level JSON text - that is, a JSON array 
or JSON object. If you are not sure which type of top-level object you may
have, use the `parseTop` method, otherwise use `parseArray` if you're
sure you'll get a JSON array, or `parseObject` for a JSON object.

    scala> import net.uniscala.json._
    import net.uniscala.json._
    
    scala> def feed = scala.io.Source.fromURL(
         |    "https://api.twitter.com/1/statuses/user_timeline.json?" +
         |    "include_entities=true&include_rts=true&screen_name=stainsby&count=2"
         | ).reader
    feed: java.io.InputStreamReader

    scala> JsonParser.parseTop(feed).toPrettyString
    res1: String = 
    [
      {
        "coordinates": null,
        "retweeted": false,
        "source": "web",
        "entities": {
          "hashtags": [
            {
              "text": "Natio",
              "indices": [
                130,
                136
              ]
            }
          ],...


### <a id="Transforming"></a>Transforming

`JsonArray` and `JsonObject` instances are collections. `JsonArray` is 
backed by an immutable `Vector` and `JsonObject` is mapped by an
immutable `Map`. This means that they can be easily transformed to new
instances using familiar collection operations. For example:

    scala> import net.uniscala.json._
    import net.uniscala.json._
    
    scala> import Json._
    import Json._
    
    scala> val jarr = Json(1, 2, 3)
    jarr: net.uniscala.json.JsonArray = [1, 2, 3]
    
    scala> jarr :+ JsonInteger(1)
    res24: net.uniscala.json.JsonArray = [1, 2, 3, 1]
    
    scala> val jobj = Json("x" -> Json("apple", "banana", "orange"), "y" -> "parrot")
    jobj: net.uniscala.json.JsonObject = {"x": ["apple", "banana", "orange"], "y": "parrot"}
    
    scala> val jobj1 = jobj - "y"
    res5: net.uniscala.json.JsonObject = {"x": ["apple", "banana", "orange"]}

    scala> Json(1, 2, 3) ++ Json("a", "b", "c")
    res29: net.uniscala.json.JsonArray = [1, 2, 3, "a", "b", "c"]

The is a minor inconvenience though. The `+` and `++` operations for 
`JsonObject`, despite returning a `JsonObject` instance, have a return 
type of `Map`, due to their inherited method signatures:

    scala> val jobj2 = jobj + ("x" -> JsonInteger(1223))
    jobj2: scala.collection.immutable.Map[String,net.uniscala.json.JsonValue[_]] = {"x": 1223, "y": "parrot"}
    
    scala> Json("x" -> 123) ++ Json("y" -> 99, "x" -> "cc")
    res27: scala.collection.immutable.Map[String,net.uniscala.json.JsonValue[_]] = {"x": "cc", "y": 99}
    
Since this could lead to more verbose handling code, or dangerous casting,
we supply a type-preserving `:+` operation:

    scala>  val jobj2 = jobj :+ ("x" -> 1223)
    jobj2: net.uniscala.json.JsonObject = {"x": 1223, "y": "parrot"}
    
    scala> val jobj2 = jobj :+ ("x" -> 1223, "z" -> "lizard")
    jobj2: net.uniscala.json.JsonObject = {"x": 1223, "y": "parrot", "z": "lizard"}

Like `+` and `++`, this operation adds or overrides values corresponding to 
the keys at the highest level only. It doesn't recurse through any objects 
that the keys point to.

So far we've covered basic collection-based operations. For more transformation
techniques, continue on the 'Paths' and  'Tree operations' below.


### Paths

Nested JSON objects give JSON a hierarchical structure. SInce version 0.2, 
there is dedicated support for operations on JSON trees and paths through them.

A `JsonPath` instance represents a path through a JSON data structure, 
starting from the root and specifying the JSON object keys. So, for 
example, for a JSON object like this:

    scala> val user1 = Json(
         |   "type" -> "user",
         |   "profiles" -> Json(
         |     "mybook"   -> Json("key" -> "AGW45HWH", "secret" -> "g4juh43ui9g929k4"),
         |     "alt"      -> Json("key" -> "ER45DFE3", "secret" -> "0867986769de68")        
         |   ),
         |   "and" -> 123,
         |   "even" -> 456,
         |   "more" -> Json("uninteresting" -> 678, "stuff" -> 999)
         | )
    user1: net.uniscala.json.JsonObject = {"even": 456, "more": ...

we can specify the path to the `"mybook"` profile thus:

    scala> val path = new JsonPath("profiles", "mybook")

allowing us the retrieve that profile like this:

    scala> val profile = user1.getAt[JsonObject](path)
    profile: Option[net.uniscala.json.JsonObject] = Some({"key": "AGW45HWH", "secret": "g4juh43ui9g929k4"})

If the path was invalid, we get `None`:

    scala> val x = user1.getAt[JsonObject](new JsonPath("no", "such", "thing"))
    x: Option[net.uniscala.json.JsonObject] = None

Also, we specify the expect JSON type as a type parameter to the `getAt`
method. If the different type is found, `None` is returned:

    scala> val jstring = user1.getAt[JsonString](path)
    jstring: Option[net.uniscala.json.JsonString] = None
    
As a minor convenience, you can also apply a function at a value at the 
end of a path and get the result:

    scala> def reverse(json: JsonValue[_]): JsonValue[_] = json match {
         |   case JsonString(str) => JsonString("REVERSED: " + str.reverse)
         |   case json => json
         | }
    reverse: (json: net.uniscala.json.JsonValue[_])net.uniscala.json.JsonValue[_]
    
    scala> user1.applyAt(profiles / "mybook" / "secret", reverse)
    res7: Option[net.uniscala.json.JsonValue[_]] = Some("REVERSED: 4k929g9iu34huj4g")

Path segments can also be constructed or appended using `/`:

    scala> val path = JsonPath.root / "profiles" / "mybook"
    path: net.uniscala.json.JsonPath = profiles:mybook
    
    scala> val path2 = path / "secret"
    path2: net.uniscala.json.JsonPath = profiles:mybook:secret

Importing the contents of `JsonPath` also allows you to abbreviate
`JsonPath.root` to `/`:

    scala> import JsonPath._
    import JsonPath._
    
    scala> val path = / / "one" / "more" / "path"
    path: net.uniscala.json.JsonPath = one:more:path


### Tree operations

The are methods to deal with the tree-like nature of JSON. The `treeMap`
methods operates in a similar way to the familiar functional `map`, but 
traversing the tree structure and applying the map function to the
values at each key. Continuing to use `user1` declared in 'Paths' above:

    scala> user1.treeMap( _ match { case js: JsonString  => "ASTRING"; case j => j } ) toPrettyString
    res14: String = 
    {
      "even": 456,
      "more": {
        "uninteresting": 678,
        "stuff": 999
      },
      "type": "ASTRING",
      "and": 123,
      "profiles": {
        "mybook": {
          "key": "ASTRING",
          "secret": "ASTRING"
        },
        "alt": {
          "key": "ASTRING",
          "secret": "ASTRING"
        }
      }
    }

The `treeMap` method is based on the even more powerful `treeCollect` method,
which is analogous to the `collect` method in Scala collections - it's
like `treeMap` but takes a partial function:

    scala> user1 treeCollect { case o: JsonObject => o; case j: JsonInteger  => "ANINT" } toPrettyString
    res15: String = 
    {
      "even": "ANINT",
      "more": {
        "uninteresting": "ANINT",
        "stuff": "ANINT"
      },
      "and": "ANINT",
      "profiles": {
        "mybook": {
        },
        "alt": {
        }
      }
    }

Sometimes it's useful to be able to access the path while doing a 
map or collect operation. For this, we have `pathMap` and `pathCollect`
methods, where the supplied function takes both the path at which the
function is being applied, and the JSON value:

    scala> user1.pathMap {
         |   _ match {
         |     case (path, js: JsonString) if path.last == "secret" => "xxxxxxxx"
         |     case (_, json) => json
         |   }
         | } toPrettyString
    res9: String = 
    {
      "even": 456,
      "more": {
        "uninteresting": 678,
        "stuff": 999
      },
      "type": "user",
      "and": 123,
      "profiles": {
        "mybook": {
          "key": "AGW45HWH",
          "secret": "xxxxxxxx"
        },
        "alt": {
          "key": "ER45DFE3",
          "secret": "xxxxxxxx"
        }
      }
    }

There are also more convenient methods for transforming JSON at known 
locations in the tree. One is the `replace` method:

    scala> val profiles = / / "profiles"
    profiles: net.uniscala.json.JsonPath = profiles
    
    scala> user1.replace(
         |   profiles / "mybook" / "secret" -> "asecret",
         |   profiles / "alt" / "secret" -> "asecret2"
         | ).toPrettyString
    res18: String = 
    {
      "even": 456,
      "more": {
        "uninteresting": 678,
        "stuff": 999
      },
      "type": "user",
      "and": 123,
      "profiles": {
        "mybook": {
          "key": "AGW45HWH",
          "secret": "asecret"
        },
        "alt": {
          "key": "ER45DFE3",
          "secret": "asecret2"
        }
      }
    }

Another is the slightly more general `transform` method, which, instead of 
replacing values, applies a function:

    def reverse(json: JsonValue[_]): JsonValue[_] = json match {
      case JsonString(str) => JsonString("REVERSED: " + str.reverse)
      case json => json
    }
    reverse: (json: net.uniscala.json.JsonValue[_])net.uniscala.json.JsonValue[_]
    
    scala> user1.transform(
         |   profiles / "mybook" / "secret" -> reverse,
         |   profiles / "alt" / "secret" -> reverse
         | ).toPrettyString
    res6: String = 
    {
      "even": 456,
      "more": {
        "uninteresting": 678,
        "stuff": 999
      },
      "type": "user",
      "and": 123,
      "profiles": {
        "mybook": {
          "key": "AGW45HWH",
          "secret": "REVERSED: 4k929g9iu34huj4g"
        },
        "alt": {
          "key": "ER45DFE3",
          "secret": "REVERSED: 86ed9676897680"
        }
      }
    }


## Parsing long JSON texts

The is no specific support for parsing very long JSON texts. This might cause
excessive memory usage when attempting to use the parser in such situations.
However, depending on your use case, you may be able to subclass the JSON 
parser to operate in a way that avoids this issue. Below is an example for 
streaming the results of a CouchDB view query.


### Example: streaming CouchDB results

A couchdb view generates a JSON object, consisting of some preamble fields
and a potentially very long array of rows. Each row is a JSON object:

    {"total_rows": 433039, "offset": 0,"rows": [
      {"id": "50015430000", "key": [3,2,1,0,2,2,3,3,1,0], "value": [[115...
      {"id": "50015530000", "key": [3,2,1,0,2,2,3,3,1,0], "value": [[115...
      ... maybe thousands or millions more rows ...
    }

Parsing this all into memory could present a problem. To avoid this, we 
create a customised parser that will read in each row when we request it:
    
    import java.io.Reader
    import net.uniscala.json._
    import scala.annotation.tailrec
    
    class CouchViewResults(r: java.io.Reader, resultsKey: String = "rows") extends JsonParser(r) {
      
      private var atEnd = false
      
      init()
      
      private def init(): Unit = {
        
        // skip preamble info like "total_rows" etc. (or we could use these!)
        @tailrec def preamble(): Unit = {
          skipWhitespace
          val key: String = string;
          if (key != resultsKey) {
            skipKeyValue
            preamble
          }
        }
        
        skipWhitespace
        consumeChar('{')
        preamble
        skipWhitespace
        consumeChar(':')
        skipWhitespace
        consumeChar('[')
      }
      
      def skipKeyValue(): Unit = {
        skipWhitespace
        consumeChar(':')
        val skipped = jvalue
        skipWhitespace
        consumeChar(',')
      }
      
      def nextResult(): Option[JsonObject] = {
        if (atEnd) {
          None
        } else {
          val row = parseObject
          advance
          skipWhitespace
          if (currentChar != ',') {
            atEnd = true
          } else {
            advance
          }
          Some(row)
        }
      }
    }

We can now use this to stream the rows (which are JSON objects) from the 
results of a query:

    scala> val r = scala.io.Source.fromURL("http://localhost:5984/asgs_2011/_design/abs/_view/coverings?reduce=false").reader
    r: java.io.InputStreamReader = java.io.InputStreamReader@25dbb4b8
    
    scala> val s = new CouchViewResults(r)
    s: CouchViewResults = CouchViewResults@654b665d
    
    scala> s.nextResult
    res1: Option[net.uniscala.json.JsonObject] = Some({"id": "50015430000", "key": [3, 2, 1, 0, 2, 2, 3, 3, 1, 0], "value": [[115.029553, 115.029976], [-34.309662, -34.309455]]})
    
    scala> s.nextResult
    res2: Option[net.uniscala.json.JsonObject] = Some({"id": "50015530000", "key": [3, 2, 1, 0, 2, 2, 3, 3, 1, 0], "value": [[115.06153, 115.062106], [-34.334516, -34.333426]]})

Note this view is from an statistical data set and results in more than 
400,000 rows. Using our custom parser, only one row is parsed into memory 
at a time.

Disclaimer: This example is very simplistic and hardly tested at all. It 
may not work for views using a `reduce` function. With a little more work we 
could probably create something more general that results in something like 
a Scala iterable or stream, and also captures the premable data.

## API documentation

See the [scaladoc].


## Project

Uniscala JSON is currently hosted on [GitHub].

The project is built using fairly standard Maven. However, there is also 
an SBT shim that depends on the `pom.xml` which allows SBT to be run for 
testing and similar:

    $ sbt console
    ...
    scala> import net.uniscala.json._
    import net.uniscala.json._
    
    scala> def feed = scala.io.Source.fromURL("https://api.twitter.com/1/statuses/user_timeline.json?include_entities=true&include_rts=true&screen_name=stainsby&count=2").reader
    feed: java.io.InputStreamReader
    
    scala> JsonParser.parseTop(feed).toPrettyString
    res0: String = ...


[GitHub]: https://github.com/stainsby/uniscala-json "Uniscala JSON on GitHub"
[json]: http://json.org/ "Introducing JSON"
[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License Version 2.0, January 2004"
[scaladoc]: ./scaladocs/index.html "Scaladoc"
[ss]: http://sustainablesoftware.com.au/ "Sustainable Software Pty Ltd"