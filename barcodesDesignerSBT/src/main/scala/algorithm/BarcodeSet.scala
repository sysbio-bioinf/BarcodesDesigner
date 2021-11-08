package main.code
package algorithm

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.io.Source

/**
 * An interface specifying the basic
 * functions of a barcode set
 */
trait BarcodeSet {

  /**
   * Extracts the barcodes belonging to the set
   * @return An array of strings comprising the barcodes in the set
   */
  def getBarcodes: Array[String]

  /**
   * Gets the number of barcodes in the set
   * @return The number of barcodes
   */
  def getNumBarcodes: Int

  /**
   * Gets the minimum distance between any two barcodes in the set
   * @return The minimum distance
   */
  def getMinDist: Int
  
  /**
   * Obtains a copy of the current barcode set
   * @return The copy
   */
  def copy() : BarcodeSet
}

/**
 * An interface specifying additional methods
 * a barcode set must implement to serve
 * as an individual of a Genetic Algorithm
 */
trait Individual[T] extends BarcodeSet {
  
  /**
   * Determines whether this individual is better than another individual
   * @param that	The other individual
   * @return true if this individual dominates the other individual, false otherwise
   */
  def dominates(that : T) : Boolean
  
  /**
   * Extracts the <index>th fitness value
   * @param index	The index of the fitness value
   * @return The fitness value
   */
  def getFitnessValue(index : Int) : Double
  
  /**
   * Returns those values that should be used
   * to identify and eliminate duplicates
   * (usually the fitness values or a subset thereof)
   * @return The values for fitness grouping
   */
  def getFitnessGrouping : Product  
}

/**
 * An extended interface for individuals in
 * a multi-objective optimization
 */
trait MOOIndividual[T] extends Individual[T] {
  
  /**
   * The crowding distance of the individual
   */
  var crowdingDistance : Double = 0.0

  /**
   * The index of the Pareto front to which the individual belongs
   */
  var paretoFrontIndex : Integer = 0
}

/**
 * A basic class for storing barcode sets
 * @param	barcodes  The barcodes comprised in the set
 */
class SimpleBarcodeSet(val barcodes: Array[String]) extends BarcodeSet {
  // calculate the minimum distance between any two barcodes in the set
  val minDist: Int = BarcodeDistanceCalculator.getMinDist(barcodes)

  def getBarcodes: Array[String] = {
    barcodes
  }

  def getMinDist: Int = {
    minDist
  }

  def getNumBarcodes: Int = {
    barcodes.length
  }

  override def toString(): String =
    {
      if (barcodes.length == 0)
        "(empty barcode set)"
      else if (barcodes.length == 1)
        "1 barcode"
      else
        barcodes.length + " barcodes with minimum distance " + minDist
    }  

  def copy(): BarcodeSet = {
    new SimpleBarcodeSet(barcodes)
  } 
  
  
}

object BarcodeSetCollection {

  /**
   * Parses a JSON file and returns the structures
   * needed to construct a BarcodeSelectionResult
   * @param file	The file to load
   * @return A tuple comprising an array of barcodes and
   * an array of subsets of these barcodes
   */
  private def parseJSON(file: String): Array[BarcodeSet] = {

    val content = parse(Source.fromFile(file).mkString)

    val barcodeSets: Array[BarcodeSet] = (content \ "sets").values.asInstanceOf[List[List[String]]].map(set => {

      // construct BarcodeSet object and calculate its fitness
      new SimpleBarcodeSet(set.toArray)
    }).toArray
    barcodeSets
  }
}

/**
 * A class that maintains a collection of barcode sets
 * and can read them from and write them to JSON files
 * @param sets	The collection of sets
 */
class BarcodeSetCollection(val sets: Array[BarcodeSet]) {

  /**
   * Reads a collection of barcodes from a file
   * @param file	The name of the BarcodeDesigner JSON file
   */
  def this(file : String) = this(BarcodeSetCollection.parseJSON(file))
  
  override def toString(): String = {
    val ret = new StringBuilder
    for (set <- sets) {
      ret ++= set.toString
      ret += '\n'
      for (barcode <- set.getBarcodes) {
        ret += '\t'
        ret ++= barcode
        ret += '\n'
      }
      ret += '\n'
    }
    ret.toString
  }

  /**
   * Exports the specified sets to a JSON string
   * @param sets	The sets to export (must be a subset of the sets field)
   * @return A JSON string
   */
  def toJSON(sets: Array[BarcodeSet]): String = {
    assert(sets.toSet subsetOf this.sets.toSet)
    val jsonObj = 
      "sets" -> sets.map(set => {
        set.getBarcodes.toList
      }).toList
    pretty(render(jsonObj))
  }

  /**
   * Exports the complete selection result to a JSON string
   * @return A JSON string
   */
  def toJSON: String = {
    toJSON(sets)
  }
  
  

}