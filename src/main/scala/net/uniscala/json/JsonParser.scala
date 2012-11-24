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


import java.io.{Reader, StringReader}

import scala.annotation.tailrec
import scala.collection.mutable.Buffer


object JsonParser {
  
  /**
   * Parses a JSON top-level object - that is, a JSON array or JSON 
   * object.
   */
  def parseTop(s: String): JsonTop[_] =
    (new JsonParser(s)).parseTop
  
  /**
   * Parses the next JSON top-level object - that is, a JSON array or JSON 
   * object. Note that the read position will end up on
   * the closing ']' or '}' character (and not after it).
   */
  def parseTop(reader: Reader): JsonTop[_] =
    (new JsonParser(reader)).parseTop
  
  /**
   * Parses a JSON object.
   */
  def parseObject(s: String): JsonObject = (new JsonParser(s)).parseObject
  
  /**
   * Parses the next JSON object. Note that the read position will end up on
   * the closing '}' character (and not after it).
   */
  def parseObject(reader: Reader): JsonObject =
    (new JsonParser(reader)).parseObject
  
  /**
   * Parses a JSON array.
   */
  def parseArray(s: String): JsonArray =
    (new JsonParser(s)).parseArray
  
  /**
   * Parses the next JSON array. Note that the read position will end up on
   * the closing ']' character (and not after it).
   */
  def parseArray(reader: Reader): JsonArray = (new JsonParser(reader)).parseArray
}

class JsonParser(reader: Reader) {
  
  def this(s: String) = this(new StringReader(s))
  
  private var ch: Char = ' '
  private var charCount = 0
  private var lineCount = 1
  private var lineChar = 0
  
  /**
   * Gets the last character that was read from the reader.
   */
  protected def currentChar(): Char = ch
  
  /**
   * Parses the next JSON top-level object - that is, a JSON array or JSON 
   * object. Note that the read position will end up on
   * the closing ']' or '}' character (and not after it).
   */
   final def parseTop: JsonTop[_] = {
    skipWhitespace
    ch match {
      case '[' => parseArray
      case '{' => parseObject
      case _ => throwError("unexpected start of JSON array or object")
    }
  }
  
  /**
   * Parses the next JSON object. Note that the read position will end up on
   * the closing '}' character (and not after it).
   */
  final def parseObject: JsonObject = {
    skipWhitespace
    consumeChar('{')
    @tailrec def jobject(nameValuePairs: Buffer[(String, JsonValue[_])]): JsonObject = {
      skipWhitespace
      if (ch == '}') {
        JsonObject.empty
      } else {
        val name: String = string
        skipWhitespace
        consumeChar(':')
        val value: JsonValue[_] = jvalue
        value match {
          case _: JsonObject => advance
          case _: JsonArray => advance
          case _ =>
        }
        nameValuePairs.append((name, value))
        skipWhitespace
        if (!(ch == ',' || ch == '}'))
          throwError("expected ',' or '}', got '" + ch + "'")
        if (ch == '}') {
          JsonObject(nameValuePairs:_*) 
        } else {
          advance
          jobject(nameValuePairs)
        }
      }
    }
    jobject(Buffer[(String, JsonValue[_])]())
  }
  
  /**
   * Parses the next JSON array. Note that the read position will end up on
   * the closing ']' character (and not after it).
   */
  final def parseArray: JsonArray = {
    skipWhitespace
    consumeChar('[')
    @tailrec def jarray(values: Buffer[JsonValue[_]]): JsonArray = {
      skipWhitespace
      if (ch == ']') {
        JsonArray.empty
      } else {
        val value: JsonValue[_] = jvalue
        value match {
          case _: JsonObject => advance
          case _: JsonArray => advance
          case _ =>
        }
        values.append(value)
        skipWhitespace
        if (!(ch == ',' || ch == ']'))
          throwError("expected ',' or ']', got '" + ch + "'")
        if (ch == ']') {
          JsonArray(values:_*) 
        } else {
          advance
          jarray(values)
        }
        
      }
    }
    jarray(Buffer[JsonValue[_]]())
  }
  
  /**
   * Throws a JsonFormatException with the supplied message. Approximate 
   * character and line position information for the error will be 
   * incorporated.
   */
  protected def throwError(msg: String): Nothing = {
    throw new JsonFormatException(
      msg + " (near char " + lineChar + " on line " + lineCount + ")"
    )
  }
  
  /**
   * Throws a JsonFormatException if the character at the current position 
   * doesn't match the supplied value.
   */
  protected def expectChar(c: Char): Unit = {
    if (ch != c) throwError("expected '" + c + "' but got '" + ch +"'")
  }
  
  /**
   * If the character at the current position matches the supplied value,
   * advance to the next character.
   */
  protected def consumeChar(c: Char): Unit = {
    expectChar(c)
    advance
  }
  
  /**
   * Read the next character into the internal 'ch' variable.
   * The next character is expected to exist, otherwise a JsonFormatException 
   * is thrown if the end of the stream is encountered instead.
   */
  protected def advance: Unit = {
    val c = reader.read
    charCount += 1
    lineChar += 1
    if (ch == '\n') {
      lineChar = 0
      lineCount += 1
    }
    if (c == -1) throwError("unexpected end of input")
    ch = c.toChar
  }
  
  /**
   * Reads the next JSON value.
   */
  protected def jvalue: JsonValue[_] = {
    skipWhitespace
    ch match {
      case '[' => parseArray
      case '"' => jstring
      case 't' | 'f' => jboolean
      case 'n' => jnull
      case d if d.isDigit | d == '-' => jnumber
      case '{' => parseObject
      case _ => throwError("expected start of JSON value but got: '" + ch + "'")
    }
  }
  
  /**
   * Reads the next JSON string value. Note that the read position will end up 
   * after (not on) the closing '"' character.
   */
  protected def jstring: JsonString = JsonString(string)
  
  protected def jnumber: JsonNumber[_] = {
    // refer to http://www.json.org/ for format explanations
    skipWhitespace
    val isNegative = ch == '-'
    if (isNegative) advance
    val hasZeroStart = (ch == '0') //throwError("cannot start number with '0'")
    val int = digits
    if (hasZeroStart && int.length > 1) throwError("illegal leading '0'")
    if (ch == '.') {
      advance
      val frac = digits
      if (ch == 'e' || ch == 'E') {
        advance
        // format is: int frac exp
        val exp = expPart
        var d = (int + '.' + frac + 'E' + exp).toDouble
        if (isNegative) d = -d
        new JsonFloat(d)
      } else {
        // format is: int frac 
        var d = (int + '.' + frac).toDouble
        if (isNegative) d = -d
        new JsonFloat(d)
      }
    } else if (ch == 'e' || ch == 'E') {
      // format is: int exp
      val exp = expPart
      var d = (int + 'E' + exp).toDouble
      if (isNegative) d = -d
      new JsonFloat(d)
    } else {
      // format is: int
      var i = int.toLong
      if (isNegative) i = -i
      new JsonInteger(i)
    }
  }
  
  /**
   * Reads the next JSON null value. Note that the read position will end up 
   * after (not on) the trailing 'l' character.
   */
  protected def jnull: JsonNull.type = {
    skipWhitespace
    consumeChars("null")
    JsonNull
  }
  
  /**
   * Reads the next JSON boolean value. Note that the read position will end up 
   * after (not on) the 'true' or 'false' characters.
   */
  protected def jboolean: JsonBoolean = {
    skipWhitespace
    val isTrue = ch == 't'
    val isFalse = ch == 'f'
    if (isTrue) {
      consumeChars("true")
      JsonTrue
    } else if (isFalse) {
      consumeChars("false")
      JsonFalse
    } else {
      throwError("invalid JSON boolean")
    }
  }
  
  /**
   * Reads the next JSON string. Note that the read position will end up 
   * after (not on) the trailing 'l' character.
   */
  protected def skipWhitespace: Unit = {
    while (ch.isWhitespace) {
      advance
    }
  }
  
  /**
   * Reads the next quoted string. Note that the read position will end up 
   * after (not on) the trailing '"' character.
   */
  protected def string: String = {
    skipWhitespace
    consumeChar('"')
    @tailrec def string(builder: StringBuilder): String = {
      if (ch == '"') {
        val str = builder.toString
        advance
        str
      } else {
        if (ch == '\\') {
          advance
          ch match {
            case '"' | '\\' | '/' => builder += ch
            case 'b' => builder += '\b'
            case 'f' => builder += '\f'
            case 'n' => builder += '\n'
            case 'r' => builder += '\r'
            case 't' => builder += '\t'
            case 'u' => builder += {
              advance
              unicode
            }
            case _ => throwError("invalid string escape")
          }
        } else {
          builder += ch
        }
        advance
        string(builder)
      }
    }
    string(new StringBuilder)
  }
  
  /**
   * Reads a 4 hex digit Unicode escape sequence and returns the corresponding 
   * character. Note that the read position will end up on (not after) 
   * the last hex digit.
   */
  private def unicode: Char = {
    var i = 0
    val builder = new StringBuilder
    for (i <- 1 to 4) {
      if (!isHexDigit(ch))
        throwError("only hex digits allowed in unicode escape")
      builder += ch
      if (i != 4) advance
    }
    val codeStr = builder.toString
    try {
      Integer.parseInt(codeStr, 16).toChar
    } catch {
      case _: NumberFormatException => throwError("expected unicode code")
    }
  }
  
  private def isHexDigit(c: Char): Boolean =
    c.isDigit || "abcdefABCDEF".contains(c)
  
  /**
   * Reads digit characters until the first non-digit character is reached. 
   * Note that the read position will end up after (not on) the last 
   * digit.
   */
  private def digits: String = consumeChars(_.isDigit)
  
  private def expPart: String = {
    val sign = if (ch == '+') "+" else if (ch == '-') "-" else ""
    if (sign != "") advance
    sign + digits
  }
  
  /**
   * Reads characters that must match what is in the 'token' character sequence.
   * Note that the read position will end up after (not on) the last token 
   * character.
   */
  private def consumeChars(chars: Seq[Char]): Unit = {
    val lastIdx = chars.length - 1
    var idx = 0
    chars foreach { c =>
      expectChar(c)
      if (idx != lastIdx) advance
      idx += 1
    }
    advance
  }
  
  /**
   * Reads characters accepted by the 'accept' function until the first
   * non-accepted character is reached. Note that the read position will 
   * end up after (not on) the last accepted character.
   */
  private def consumeChars(accept: Char => Boolean): String = {
    skipWhitespace
    if (!accept(ch)) throwError("unexpected character: " + ch)
    val builder = new StringBuilder
    while(accept(ch)) {
     builder += ch
     advance
    }
    builder.toString
  }
}
