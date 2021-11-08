package main.code
package algorithm

import scala.annotation.tailrec
import scala.runtime.BoxesRunTime

/**
 * Enumeration for different command line argument types
 */
sealed class ArgType protected (name: String, ordinal: Int) extends java.lang.Enum[ArgType](name, ordinal)
object ArgType {

  /**
   * A string argument
   */
  val TYPE_STRING = new ArgType("STRING", 0)

  /**
   * An integer argument
   */
  val TYPE_INT = new ArgType("INT", 1)

  /**
   * A floating-point number argument
   */
  val TYPE_DOUBLE = new ArgType("DOUBLE", 2)

  /**
   * A Boolean flag (without a value)
   */
  val TYPE_BOOL = new ArgType("BOOL", 3)

}

class ParseException(msg: String) extends Exception(msg){
  
}

class FileException(msg: String) extends Exception(msg){

}

/**
 * Internal class wrapping command line arguments
 * @param allKeys	An array of possible alternative keys for this argument
 * @param argType	The type of the value of this argument (see ArgType)
 * @param description	A description of the value shown in the usage text
 * @param defaultValue	A standard value for the parameter
 * @param optional	Specifies whether the parameter can be omitted or not
 */
case class CommandLineArgument(allKeys : Array[String],
                               argType: ArgType, description: String, 
                               defaultValue: Object, optional: Boolean = false) {
  var set = false
  var value: Object = defaultValue

  /**
   * Returns the integer value of this parameter
   * (or its default value) if it is 
   * of type ArgType.TYPE_INT
   * @return The integer value
   */
  def toInt: Int = {
    value.asInstanceOf[Integer].intValue()
  }

  /**
   * Returns the double value of this parameter
   * (or its default value) if it is 
   * of type ArgType.TYPE_DOUBLE
   * @return The double value
   */
  def toDouble: Double = {
    value.asInstanceOf[Double].doubleValue()
  }

  /**
   * Returns the Boolean value of this parameter
   * (or its default value) if it is 
   * of type ArgType.TYPE_Boolean
   * @return The Boolean value
   */
  def toBoolean: Boolean = {
    value.asInstanceOf[Boolean].booleanValue()
  }

  /**
   * Returns the string representation of this parameter
   * (or its default value). This works for all value types. 
   * @return The string representation
   */
  override def toString(): String = {
    value.toString
  }

}

/**
 * A class managing the storage and interpretation of 
 * command line arguments
 */
class CommandLineParser {

  /**
   * A map storing a mapping of parameter names (keys) and the corresponding
   * parameter information (type, default value, current value...)
   */
  val argMap = scala.collection.mutable.Map.empty[String, CommandLineArgument]
  
  /**
   * A list of all unique parameters (there may be multiple alternative names for one parameter)
   */
  val uniqueList = new scala.collection.mutable.ArrayBuffer[CommandLineArgument]

  /**
   * Adds a parameter with a single name to the parameter list
   * @param command	The parameter name/key
   * @param description	A description of the parameter value for the usage line
   * @param argType	The parameter value type (see ArgType)
   * @param defaultValue	The default value of this parameter if it is not set on the command line
   * @param optional	Specifies whether the parameter has to be specified or not
   */
  def add(command: String, description: String, argType: ArgType, defaultValue: Object, optional: Boolean): uniqueList.type = {
    val arg = CommandLineArgument(Array(command), argType, description, defaultValue, optional)
    argMap(command) = arg
    uniqueList += arg
  }
  
  /**
   * Adds a parameter with a multiple alternative names to the parameter list
   * @param commands	An array of alternative names/keys for this parameter
   * @param description	A description of the parameter value for the usage line
   * @param argType	The parameter value type (see ArgType)
   * @param defaultValue	The default value of this parameter if it is not set on the command line
   * @param optional	Specifies whether the parameter has to be specified or not
   */
  def add(commands: Array[String], description: String, argType: ArgType, defaultValue: Object, optional: Boolean): uniqueList.type = {
    val arg = CommandLineArgument(commands, argType, description, defaultValue, optional)
    for (command <- commands)
      argMap(command) = arg
    uniqueList += arg    
  }

  /**
   * Returns the usage text for the program
   * @return The usage text
   */
  def usage(): String = {
    var path = getClass.getResource(getClass.getSimpleName + ".class").getFile
    var progName = "bin"
    if(!path.startsWith("/")) {
        path = ClassLoader.getSystemClassLoader.getResource(path).getFile
      progName = "java -jar " + new java.io.File(path.substring(0, path.lastIndexOf('!'))).getName
    }

    val ret = new StringBuilder("Usage: ")
    ret ++= progName

    for (value <- uniqueList) {

      if (value.optional)
        ret ++= "\n\t["
      else
        ret ++= "\n\t"        
      ret ++= value.allKeys.mkString(" | ")

      if (value.argType != ArgType.TYPE_BOOL) {
        ret ++= " <"
        ret ++= value.description
        ret += '>'
      }

      if (value.optional)
        ret += ']'

    }

    ret.toString
  }

  /**
   * Parses and verifies the supplied command line arguments
   * @param args	The command line arguments
   */
  def parseArgs(args: Array[String]): Unit = {

    if (args.length == 1 && 
          (args(0) == "-help" || 
           args(0) == "--help" || 
           args(0) == "-h" || 
           args(0) == "-?")) {
      // print usage if help is requested
      System.err.println(usage())
      System.exit(0)
    }

    // recursively parse the arguments
    parseNext(args.toList)

    // check whether all required parameters are supplied
    for ((key, value) <- argMap) {
      if (!value.set && !value.optional)
        throw new ParseException("Required parameter " + key + " not set!")
    }
  }

  /**
   * Internal recursive function parsing a single parameter
 *
   * @param args	The list of remaining parameters
    */
  @tailrec
private def parseNext(args: List[String]): Unit = {
    if (args.isEmpty)
      return

    // check whether the parameter is known
    if (!argMap.contains(args.head))
      throw new ParseException("Unknown argument: " + args.head)

    // look up info on the parameter
    val argInfo = argMap(args.head)

    if (argInfo.argType != ArgType.TYPE_BOOL) {
      // String and numeric parameters are followed by their values
      if (args.size == 1)
        throw new ParseException("Missing value for parameter " + args.head)
      try {
        // try to convert the supplied values to the specified type
        argInfo.value = argInfo.argType match {
          case ArgType.TYPE_STRING => args.tail.head
          case ArgType.TYPE_INT => BoxesRunTime.boxToInteger(args.tail.head.toInt)
          case ArgType.TYPE_DOUBLE => BoxesRunTime.boxToDouble(args.tail.head.toDouble)
          case _ => null
        }
      } catch {
        // conversion failed
        case _: Exception => throw new ParseException("Invalid value for parameter " + args.head + "!");
      }
      argInfo.set = true
      parseNext(args.tail.tail)
    } else {
      // Boolean flags do not specify a subsequent value 
      // - they are active if the parameter is supplied and inactive otherwise
      argInfo.value = BoxesRunTime.boxToBoolean(true)
      argInfo.set = true
      parseNext(args.tail)
    }
  }

  /**
   * Gets the specified value for the supplied parameter
   * or its default value (if it is an integer parameter)
   * @param key	The parameter name
   * @return The integer value
   */
  def getInt(key: String): Int = {
    argMap(key).toInt
  }

   /**
   * Gets the specified value for the supplied parameter
   * or its default value (if it is a double parameter)
   * @param key	The parameter name
   * @return The double value
   */
  def getDouble(key: String): Double = {
    argMap(key).toDouble
  }

   /**
   * Gets the specified value for the supplied parameter
   * or its default value (if it is a Boolean flag)
   * @param key	The parameter name
   * @return The Boolean value
   */
  def getBoolean(key: String): Boolean = {
    argMap(key).toBoolean
  }

   /**
   * Gets the specified value for the supplied parameter
   * or its default value as a string
   * (applicable to all types of parameters)
   * @param key	The parameter name
   * @return The string value
   */
  def getString(key: String): String = {
    argMap(key).toString()
  }

   /**
   * Sets a new  value for the supplied parameter
   * (without touching the "set" flag)
   * @param key	The parameter name
   * @param newVal The new value
   */
  def setInt(key: String, newVal: Int): Unit = {
    argMap(key).value = BoxesRunTime.boxToInteger(newVal)
  }

   /**
   * Sets a new  value for the supplied parameter
   * (without touching the "set" flag)
   * @param key	The parameter name
   * @param newVal The new value
   */
  def setDouble(key: String, newVal: Double): Unit = {
    argMap(key).value = BoxesRunTime.boxToDouble(newVal)
  }

   /**
   * Sets a new  value for the supplied parameter
   * (without touching the "set" flag)
   * @param key	The parameter name
   * @param newVal The new value
   */
  def setBoolean(key: String, newVal: Boolean): Unit = {
    argMap(key).value = BoxesRunTime.boxToBoolean(newVal)
  }

   /**
   * Sets a new  value for the supplied parameter
   * (without touching the "set" flag)
   * @param key	The parameter name
   * @param newVal The new value
   */
  def setString(key: String, newVal: String): Unit = {
    argMap(key).value = newVal
  }

  /**
   * Checks whether the supplied parameter has been
   * set by the user
   * @param key	The parameter name
   * @return true if the parameter is set or false otherwise
   */
  def isSet(key: String): Boolean = {
    argMap(key).set
  }
}