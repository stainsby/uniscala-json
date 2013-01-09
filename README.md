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

The library has been tested under Scala 2.9.2 and 2.10.0:

    > test
    ...
    [info] Passed: : Total 2055, Failed 0, Errors 0, Passed 2055, Skipped 0
    

## License

Copyright 2012 Sustainable Software Pty Ltd.
Licensed under the Apache License, Version 2.0 (the "License").
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


## API documentation

See the [scaladoc].


## Usage

See the [Usage][ghusage] document.


## Including the library in your project

Uniscala JSON releases are available in the mainstream repositories. Depending
on your Scala version, use:

    <dependency>
      <groupId>net.uniscala</groupId>
      <artifactId>uniscala-json_2.9.2</artifactId>
      <version>0.3</version>
    </dependency>
  
or

    <dependency>
      <groupId>net.uniscala</groupId>
      <artifactId>uniscala-json_2.10</artifactId>
      <version>0.3</version>
    </dependency>

in your `pom.xml`, or using SBT, in your `build.sbt`:

    "net.uniscala" %% "uniscala-json" % "0.3"
    
There are builds available for Scala versions 2.9.2 and 2.10.0.


## Building

As of version 0.3, Uniscala JSON is built in a fairly standard way using 
SBT. In previous versions, we used Maven.


[ghproject]: https://github.com/stainsby/uniscala-json "Uniscala JSON on GitHub"
[ghusage]: https://github.com/stainsby/uniscala-json/wiki/Usage "Uniscala JSON library - Usage"
[json]: http://json.org/ "Introducing JSON"
[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License Version 2.0, January 2004"
[scaladoc]: http://stainsby.github.com/uniscala-json/scaladocs/index.html "Scaladoc"
[ss]: http://www.sustainablesoftware.com.au/ "Sustainable Software Pty Ltd"