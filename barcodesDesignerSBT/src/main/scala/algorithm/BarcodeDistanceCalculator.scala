package main.code
package algorithm

import algorithm.Levenshtein.levenshtein

import scala.collection.parallel.mutable.ParArray

/**
 * Static methods to calculate distance matrices and 
 * nucleotide frequencies for barcode sets
 */
object BarcodeDistanceCalculator {

  var distanceType = 1
  
  /**
   * Calculates the pairwise distance between two barcodes
   * @param barcode1 the first barcode
   * @param barcode2 the second barcode
   * @return the pairwise distance between barcode 1 and 2
   */
  def dist(barcode1 : IndexedSeq[Char], barcode2 : IndexedSeq[Char]) : Int = {
    if (distanceType == 0) {
      (barcode1 zip barcode2).count(c => c._1 != c._2)
    } else {
      levenshtein(barcode1.mkString(""), barcode2.mkString(""))
    }

  }

  
  /**
   * Calculates a matrix of pairwise distances between any two barcodes in the provided
   * barcode set
   * @param	barcodes The barcodes as an array of strings
   * @return The distance matrix consisting of one array of distances for each barcode
   */
  def getBarcodeDists(barcodes : Array[String]): Array[Array[Int]] = {
    val d = ParArray.tabulate(barcodes.length)((i1: Int) => {
      Array.tabulate(barcodes.length)((i2: Int) => {
        if (i1 == i2)
          // set diagonal to maximum value
          Int.MaxValue
        else
          // count number of different nucleotides
          dist(barcodes(i1), barcodes(i2))
      })
    }).toArray
    d
  }
  
  /**
   * Calculates a matrix of pairwise distances between any two barcodes in the provided
   * barcode set
   * @param barcodes The barcodes as an array of char arrays
   * @return The distance matrix consisting of one array of distances for each barcode
   */
  def getBarcodeDists(barcodes : Array[Array[Char]]): Array[Array[Int]] = {

    val d = ParArray.tabulate(barcodes.length)((i1: Int) => {
      Array.tabulate(barcodes.length)((i2: Int) => {
        if (i1 == i2)
          // set diagonal to maximum value
          Int.MaxValue
        else
          // count number of different nucleotides
          dist(barcodes(i1), barcodes(i2))
      })
    }).toArray
    d
  }
  
  /**
   * Determines the frequencies of nucleotides at each position in the barcode set
   * @param barcodes The barcodes as an array of character arrays
   * @return An array that contains a 4-element array for each nucleotide position,
   * where the four elements correspond to the frequencies of the nucleotides 
   * A, C, G and T at the respective position 
   */
  def getNucleotideFrequencies(barcodes : Array[Array[Char]]) : Array[Array[Int]] = {    
    // initialize array with zeros
    val frequencies = Array.tabulate(barcodes(0).length)(_ => Array.fill(4)(0))
    for (i  <- barcodes.indices) {
      //iterate over barcodes
      for (j <- barcodes(i).indices) {
        // iterate over nucleotide positions
        val nuc = barcodes(i)(j) match {
          case 'A' => 0
          case 'C' => 1
          case 'G' => 2
          case 'T' => 3
        }
        frequencies(j)(nuc) += 1
      }
    }
    frequencies
  }

  /**
  * Updates the user-defined distance metric
  * @param metric the distance metric, 0 for hamming, 1 for levenshtein distance
  */
  def setDistanceType(metric: Int) = {
    distanceType = metric
  }
  /**
   * Calculates a matrix containing the distance tables
   * and the minimum distance vector
   * @param dist Stored distance matrix for the barcode set
   * @param maxDist maximal distance
   * @param indices the indices of the included barcodes
   * @return A matrix of distance counts for each barcode
   * and an integer vector containing the minimum distances 
   */
  def getDistanceTable(dist: Array[Array[Int]], maxDist: Int, indices : Iterable[Int]) :
    (Array[Array[Int]], Array[Int]) = {

    // initialize minimum distances with infinity
    val minDists = Array.fill(dist.length)(Integer.MAX_VALUE)

    // create a matrix that contains a table of distances
    // of *all* barcodes to all barcodes in the set
    val distTable = Array.tabulate(dist.length)(i => {
      // for each barcode, create a table counting the distances
      // and initialize with 0
      val counts = Array.fill(maxDist + 1)(0)
      if (indices.isEmpty || (indices.size == 1 && i == indices.seq.head))
      // not possible to calculate minimum distance of one barcode to itself
      // or for an empty set
        minDists(i) = Integer.MAX_VALUE
      else {
        for (j <- indices) {
          // iterate over all barcodes in the set
          // and increment the corresponding count for the distance
          // between barcode i and barcode j
          val d = dist(i)(j)
          if (d != Integer.MAX_VALUE) {
            counts(d) += 1
            if (d < minDists(i))
            // this is the new minimum distance for barcode i
              minDists(i) = d
          }
        }
      }
      counts
    })
    (distTable, minDists)
  }

  /**
  * Determines the minimum distance of the given barcodes
  * @param barcodes the set from which the minimum distance is calculated
  * @return the minimum distance of this set
  */
  def getMinDist(barcodes : Array[String]) : Int = {
    var min = Integer.MAX_VALUE
    for (i <- barcodes.indices) {
      for (j <- i+1 until barcodes.length) {
        val d = dist(barcodes(i),barcodes(j))
        if (d == 0)
          return 0
        if (d < min)
          min = d
      }      
    }
    min
  }
}