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

See the [Usage][ghusage] document.


## API documentation

See the [scaladoc].


## Project

Uniscala JSON is currently hosted on [GitHub][ghproject].

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


[ghproject]: https://github.com/stainsby/uniscala-json "Uniscala JSON on GitHub"
[ghusage]: http://stainsby.github.com/uniscala-json/Usage.md "Uniscala JSON library - Usage"
[json]: http://json.org/ "Introducing JSON"
[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License Version 2.0, January 2004"
[scaladoc]: http://stainsby.github.com/uniscala-json/scaladocs/index.html "Scaladoc"
[ss]: http://sustainablesoftware.com.au/ "Sustainable Software Pty Ltd"