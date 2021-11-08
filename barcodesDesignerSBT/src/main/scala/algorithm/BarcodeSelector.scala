package main.code
package algorithm

import algorithm.InitType._

import org.json4s._
import org.json4s.native.JsonMethods._

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParSeq
import scala.collection.{immutable, mutable}
import scala.io.Source
import scala.reflect.ClassTag
import scala.util.Random

/**
 * An enum class specifying the possible
 * initialization modes for the GA
 */
sealed class InitType protected (name: String, ordinal: Int) extends java.lang.Enum[InitType](name, ordinal)
object InitType {
  /**
   * Random generation of sets
   */
  val INIT_RANDOM = new InitType("RANDOM", 0)

  /**
   * Forward selection
   */
  val INIT_FORWARD = new InitType("FORWARD", 1)

  /**
   * Backward selection
   */
  val INIT_BACKWARD = new InitType("BACKWARD", 2)
}

/**
 * Static methods for class BarcodeSelectionResult
 */
object BarcodeSelectionResult {

  /**
   * Parses a JSON file and returns the structures
   * needed to construct a BarcodeSelectionResult
   * @param file	The file to load
   * @return A tuple comprising an array of barcodes and
   * an array of subsets of these barcodes
   */
  private def parseJSON(file: String): (Array[String], Array[BarcodeSubset]) = {

    val content = parse(Source.fromFile(file).mkString)

    // barcodes are stored as an array of tuples (1-element maps), each mapping a 
    // barcode name to the barcode sequence
    val barcodeMap = (content \ "barcodes").values.asInstanceOf[List[Map[String, String]]].map(m => m.iterator.next)
    // extract names
    val barcodeNames = barcodeMap.map(_._1).toArray
    val barcodeNameSet = barcodeNames.toSet
    // extract sequences
    val barcodes = barcodeMap.map(_._2).toArray

    // create BarcodeSelector object to calculate the fitness values
    val selector = new BarcodeSelector(barcodes)

    // read the subsets
    // each subset is an array of names of the barcodes included in the set
    val barcodeSets = (content \ "sets").values.asInstanceOf[List[List[String]]].map(set => {
      // check if all names in the set are known
      if (!(set.toSet subsetOf barcodeNameSet))
        throw new Exception("Invalid barcode names in set!")

      // create Boolean vector by iterating over all barcodes
      // and checking whether they are included in the set
      val bits = barcodeNames.map(barcode => {
        set.contains(barcode)
      })

      // construct BarcodeSet object and calculate its fitness
      new BarcodeSubset(selector, bits, true)
    }).toArray
    (barcodes, barcodeSets)

  }
}

/**
 * A barcode set class that stores subsets of an initial barcode set
 * represented as a logical vector. Objects of this class are used
 * as individuals in the barcode selection algorithm 
 * @param parent The parent barcode selector for the individual
 * @param set The set as a Boolean vector, where a true value means
 * 						that the corresponding barcode is included
 */
class BarcodeSubset(val parent: BarcodeSelector, var set: Array[Boolean]) extends MOOIndividual[BarcodeSubset] {

  var fitness: (Int, Double, Int) = (0, 0, 0)

  /**
   * Auxiliary constructor that allows for a calculation of the fitness
   * @param parent The parent barcode selector for the individual
   * @param set The set as a Boolean vector, where a true value means
   * 						that the corresponding barcode is included
   * @param updateFitness If set to true, the fitness of the individual is calculated
   */
  def this(parent: BarcodeSelector, set: Array[Boolean], updateFitness: Boolean) = {
    this(parent, set)
    if (updateFitness)
      this.updateFitness()
  }

  /**
   * Auxiliary constructor that creates a copy of another individual
   * @param other The individual to copy
   */
  def this(other: BarcodeSubset) = {
    this(other.parent, other.set.clone())
    this.fitness = other.fitness

  }

  /**
   * A sorted set containing the integer indices
   * that correspond to the Boolean index vector
   */
  private[algorithm] var indices: mutable.SortedSet[Int] = collection.mutable.SortedSet[Int]() ++ BarcodeSelector.setToIndex(set)

  /**
   * minDists: A vector storing the minimum distances of all barcodes to
   * the barcodes in the set
   * distTable: A vector that comprises a contingency table of distances
   * to the barcodes in the set for all barcodes
   */
  private[algorithm] var (distTable, minDists) = 
    BarcodeDistanceCalculator.getDistanceTable(parent.dist, parent.barcodes(0).length, indices)

  /**
   * Sets the specified bit,
   * and updates all internal structures
   * that keep track of the distances
   * @param index The index of the bit to be set
   */
  def setBit(index: Int): Unit = {
    // only perform the operation if the bit is not yet set
    if (!this.set(index)) {
      // set bit vector entry
      this.set(index) = true
      // add to index vector
      indices += index
      for (i <- minDists.indices) {
        // update distance table
        val d = parent.dist(i)(index)
        if (d != Integer.MAX_VALUE) {
          // increment count of distances
          distTable(i)(d) += 1
          if (d < minDists(i))
            // this is the new minimum distance!
            minDists(i) = d
        }
      }
    }
  }

  /**
   * Clears the specified bit,
   * and updates all internal structures
   * that keep track of the distances
   * @param index The index of the bit to be cleared
   */
  def clearBit(index: Int): Unit = {
    // only perform the operation if the bit is not yet cleared
    if (this.set(index)) {
      // clear bit vector entry
      this.set(index) = false
      // remove from index vector
      indices -= index
      for (i <- minDists.indices) {
        // update distance table
        var d = parent.dist(i)(index)
        if (d != Integer.MAX_VALUE) {
          // decrement count of distances
          distTable(i)(d) -= 1
          if (d == minDists(i)) {
            // an element with minimum distance has
            // been removed => 
            // check whether the overall minimum distance has changed
            if (indices.isEmpty || (indices.size == 1 && i == indices.firstKey))
              minDists(i) = Integer.MAX_VALUE
            else {
              while (distTable(i)(d) == 0)
                d += 1
              minDists(i) = d
            }
          }
        }
      }
    }
  }

  /**
   * Compares this individual to another individual based on the
   * Pareto fronts and crowding distances
   * @param that	The individual to which the current individual should be compared
   * @return The individual that "won" the comparison
   */
  def crowdedComparison(that: BarcodeSubset): BarcodeSubset = {
    // if one individual has the smaller Pareto front, it wins
    if (this.paretoFrontIndex < that.paretoFrontIndex)
      this
    else if (this.paretoFrontIndex > that.paretoFrontIndex)
      that
    // if the Pareto fronts are equal, the individual with the
    // larger crowding distance wins 
    else if (this.crowdingDistance > that.crowdingDistance)
      this
    else if (this.crowdingDistance < that.crowdingDistance)
      that
    // if everything is equal, decide randomly
    else if (Random.nextBoolean())
      this
    else
      that
  }

  /**
   * Performs a local optimization on the barcode set
   * by removing or adding barcodes
   * @param add	If set to true, all barcodes with the same minimum distance as the set
   * are added to the set. Otherwise, all barcodes with the same minimum distance as the
   * set are removed from the set.
   */
  private[algorithm] def optimizeSetSize(add: Boolean): Unit = {
    // determine the minimum distance between any two barcodes in the current set

    if (indices.nonEmpty) {
      val globalMin = BarcodeSelector.intSubset(minDists, indices).min

      if (add) {
        // first local improvement: add all barcodes with the same minimum distance to any barcode
        for (i <- Random.shuffle(0 to minDists.length - 1))
          if (minDists(i) >= globalMin)
            setBit(i)
      } else {
        // second local improvement: remove all barcodes that have the minimum distance to get 
        // a higher minimum distance      
        for (i <- Random.shuffle(0 to minDists.length - 1))
          if (minDists(i) == globalMin)
            clearBit(i)
      }
    }
  }

  /**
   *  Optimizes the set's color balance by removing codes
   */
  private[algorithm] def optimizeColorBalance(): Unit = {
    if (indices.nonEmpty) {
      val balance = parent.getColorBalance(set)
      do {
        // determine the barcode that achieves the maximum improvement in color balance
        // when removed from the set
        val improvement = indices.map(i => {
          Array.tabulate(balance.length)(j => {
            //math.abs(balance(j)) compare math.abs(balance(j) - parent.colorCodes(i)(j))
            math.pow(balance(j), 2) - math.pow(balance(j) - parent.colorCodes(i)(j), 2)
          }).sum
        }).zip(indices).maxBy(_._1)

        if (improvement._1 <= 0)
          // no more greedy improvement possible => terminate
          return
        else {
          // remove the barcode and update the color balance table
          // for the next iteration
          clearBit(improvement._2)
          for (j <- balance.indices)
            balance(j) -= parent.colorCodes(improvement._2)(j)
        }
      } while (indices.nonEmpty)
    }
  }

  /**
   * Applies a mutation to the individual, and performs a local optimization
   * based on the distance matrix of barcodes
   */
  def mutate(): Unit = {

    // determine 1% of the bits to be flipped
    val shuffleIdx = Random.shuffle(0 to set.length - 1).take(scala.math.max(1, scala.math.round(set.length * 0.1).toInt))

    // flip these bits
    for (i <- shuffleIdx) {
      if (set(i))
        clearBit(i)
      else
        setBit(i)
    }
    // perform local improvements
    val choice = Random.nextDouble
    if (choice < 0.25) {
      // first local improvement: add all barcodes with the same minimum distance to any barcode
      optimizeSetSize(true)
    } else if (choice < 0.5) {
      // second local improvement: remove all barcodes that have the minimum distance to get 
      // a higher minimum distance      
      optimizeSetSize(false)
    } else if (choice < 0.75 && parent.balanceColors) {
      //third local improvement: improve color balance by removing barcodes
      optimizeColorBalance()
    }
    // no local improvement with p=0.25/0.5

  }

  /**
   * Recombines this individual with another individual
   * @param that The second parent individual
   * @return A tuple of two children resulting from exchanging the tails
   * in a one-point cross-over
   */
  def recombine(that: BarcodeSubset): (BarcodeSubset, BarcodeSubset) = {
    // define a random split point
    val splitPoint = Random.nextInt(set.length - 1)

    val joinedParents = (this.set zip that.set).zipWithIndex
    // first individual: first head, second tail
    (new BarcodeSubset(parent, joinedParents.map(gene => {
      if (gene._2 <= splitPoint)
        gene._1._1
      else
        gene._1._2
    })), new BarcodeSubset(parent, joinedParents.map(gene => {
      // second individual: second tail, first head
      if (gene._2 <= splitPoint)
        gene._1._2
      else
        gene._1._1
    })))
  }

  /**
   * More efficient implementation of fitness calculation
   * based on internal distance table structures
   */
  def updateFitness(): Unit = {

    if (indices.size < 2)
      fitness = (Int.MinValue, Double.MinValue, indices.size)
    else {
      val globalMin = BarcodeSelector.intSubset(minDists, indices).min

      if (parent.balanceColors) {
        // return color balance score as second objective
        val balance = parent.getColorBalance(set)
        val balScore = 1.0 - balance.map(x => x * x).sum.toDouble / (indices.size * indices.size * balance.length)
        fitness = (globalMin,
          balScore,
          indices.size)
      } else {
        // return the mean value of the smallest 10% of the distances as second objective

        // determine the number of distances that corresponds to 10%
        val threshold = scala.math.round(indices.size * (indices.size - 1) * 0.1).toInt

//        var meanDist = 0
//        var remaining = threshold
//        var it = indices.iterator
//        var d = 0
//        // extract those 10% from the distance count table by first iterating over all
//        // zero distances for all barcodes in the set, then over all 1 distances, etc.
//        while (remaining > 0) {
//          if (it.hasNext) {
//            // go to next barcode in the set
//            val i = it.next
//            if (remaining < distTable(i)(d)) {
//              meanDist += d * remaining
//              remaining = 0
//            } else {
//              meanDist += d * distTable(i)(d)
//              remaining -= distTable(i)(d)
//            }
//          } else {
//            // increase distance
//            it = indices.iterator
//            d += 1
//          }
//        }
        val minDistCount = distTable.map(x => -x(globalMin)).sum

        // create fitness vector
        fitness = (globalMin,
          minDistCount,
          indices.size)

      }
    }
  }

  /**
   * Compares this individual to another individual based on
   * Pareto dominance
   * @param that	The other individual
   * @return true if this individual dominates the other individual, false otherwise
   */
  def dominates(that: BarcodeSubset): Boolean = {
    ((this.fitness._1 > that.fitness._1 ||
      (this.fitness._1 == that.fitness._1 && this.fitness._2 - that.fitness._2 > 0.0001)) &&
      this.fitness._3 >= that.fitness._3) ||
      ((this.fitness._1 > that.fitness._1 ||
        (this.fitness._1 == that.fitness._1 && (this.fitness._2 - that.fitness._2 > -0.0001))) && this.fitness._3 > that.fitness._3)
  }

  def getFitnessValue(index: Int): Double = {
    index match {
      case 0 => fitness._1.toDouble
      case 1 => fitness._2
      case 2 => fitness._3.toDouble
      case defaultCase => throw new IndexOutOfBoundsException
    }
    //fitness.productElement(index).asInstanceOf[Number].doubleValue
  }

  def getFitnessGrouping: Product = {
    if (parent.balanceColors)
      fitness
    else
      (fitness._1, fitness._3)
  }

  def copy(): BarcodeSet = {
    new BarcodeSubset(this)
  }

  def getBarcodes(): Array[String] = {
    BarcodeSelector.booleanSubset(parent.barcodes, set).toArray
  }

  def getNumBarcodes(): Int = {
    fitness._3
  }

  def getMinDist(): Int = {
    fitness._1
  }

  override def toString: String = {
    //return "(" + fitness._1 + ", " + fitness._2 + ", " + fitness._3 + ")";
    if (fitness._3 == 0)
      "(empty barcode set)"
    else if (fitness._3 == 1)
      "1 barcode"
    else
      fitness._3 + " barcodes with minimum distance " + fitness._1
  }
}

/**
 * Static methods of the BarcodeSelector class
 */
object BarcodeSelector {

  /**
   * Indexes an array using a Boolean index vector that specifies which elements should be kept
   * @param x	The array that is indexed
   * @param idx	The index vector, where true means that the element is kept
   * @return A sequence containing only the elements specified by the index vector
   */
  def booleanSubset[T: ClassTag](x: Array[T], idx: Array[Boolean]): Seq[T] =
    {
      // old variant - more functional, but slower
      //for ((el, i) <- (x zip idx) if i) yield el
      for (i <- x.indices if idx(i)) yield x(i)
    }

  /**
   * Indexes an array using a set of indices that specifies which elements should be kept
   * @param x	The array that is indexed
   * @param idx	The sorted set of indices
   * @return A sequence containing only the elements specified by the index set
   */
  def intSubset[T: ClassTag](x: Array[T], idx: mutable.SortedSet[Int]): Seq[T] =
    {
      idx.map(x(_)).toSeq
    }

  /**
   * Converts a Boolean index vector into an integer index vector
   *  @param set	The Boolean index vector, where true means that the element is kept
   *  @return A sequence of integer values specifying the indices that are included
   */
  def setToIndex[T: ClassTag](set: Array[Boolean]): Seq[Int] =
    {
      for (i <- set.indices if set(i)) yield i
    }

}

/**
 * The main barcode selector class providing methods to extract subsets of
 * barcodes with maximum pairwise distances
 * @param barcodes	The full set of barcode candidates from which subsets are chosen
 */
class BarcodeSelector(val barcodes: Array[String]) {
  // Map that stores known fitness values to avoid recalculation
  val storedFitness = scala.collection.mutable.Map.empty[IndexedSeq[Any], (Int, Double, Int)]
  // Stored distance matrix for the barcode set 
  val dist: Array[Array[Int]] = BarcodeDistanceCalculator.getBarcodeDists(barcodes)
  // A map mapping barcode strings to color indices (1/-1)
  val colorCodes: Array[immutable.IndexedSeq[Int]] = barcodes.map(barcode => {
    barcode.map(c => if (c == 'A' || c == 'C') 1 else -1)
  })
  var balanceColors: Boolean = false

  /**
   * Calculates the color balance of a barcode set
   * @param set	A logical vector specifying the barcode subset
   * @return An array of color balance sums for each barcode position
   */
  def getColorBalance(set: Array[Boolean]): Array[Int] = {
    val balance = BarcodeSelector.booleanSubset(colorCodes, set).reduce((c1, c2) => {
      for (i <- c1.indices) yield c1(i) + c2(i)
    }).toArray
    balance
  }

  /**
   * Recalculates the fitness vector of the supplied individual
   * @param ind	The individual to update
   */
  private def updateFitness(ind: BarcodeSubset) = {
    if (storedFitness.contains(ind.set.deep))
      // return the fitness value that has been calculated previously
      storedFitness(ind.set.deep)
    else
      ind.updateFitness()
  }

  /**
   * Performs a forward/backward selection of barcode subsets
   * @param	direction The direction of the selection
   * 									(either InitType.INIT_FORWARD for a forward selection or
   * 									InitType.INIT_BACKWARD for a backward selection)
   * @param bestPerDist	If set to true, only the best subset for each minimum distance
   * is added to the set. Otherwise, each step yields one subset in the result list
   * @return A list of barcode subsets chosen according to the selection strategy
   */
  def stepwiseBarcodeSelection(direction: InitType, bestPerDist: Boolean = true): BarcodeSetCollection = {
    assert(direction == INIT_FORWARD || direction == INIT_BACKWARD)
    val result: ArrayBuffer[BarcodeSubset] = new ArrayBuffer[BarcodeSubset]

    val subset = new BarcodeSubset(this,
      if (direction == INIT_FORWARD) {
        // forward selection: start with empty set
        Array.fill(barcodes.length)(false)
      } else {
        // backward selection: start with full set
        Array.fill(barcodes.length)(true)
      })

    //result += new BarcodeSet(this,subset.set,true)
    var oldDist = if (subset.indices.isEmpty)
      -1
    else
      subset.indices.map(subset.minDists(_)).min

    if (direction == INIT_BACKWARD)
      result += new BarcodeSubset(this, subset.set.clone, true)

    for (i <- barcodes.indices) {

      if (direction == INIT_FORWARD) {
        var max = (0, -1)
        for (i <- Random.shuffle(0 to barcodes.length - 1))
          if (!subset.set(i) && max._2 < subset.minDists(i))
            max = (i, subset.minDists(i))

        if (!bestPerDist || oldDist != max._2) {
          result += new BarcodeSubset(this, subset.set.clone, true)
          oldDist = max._2
        }

        subset.setBit(max._1)
      } else {

        var min = (0, Integer.MAX_VALUE)
        if (subset.indices.size == 1) {
          min = (subset.indices.firstKey, Integer.MAX_VALUE)
        } else {
          for (i <- Random.shuffle(0 to barcodes.length - 1))
            if (subset.set(i) && min._2 > subset.minDists(i))
              min = (i, subset.minDists(i))
        }
        subset.clearBit(min._1)
        if (!bestPerDist || oldDist != min._2) {
          result += new BarcodeSubset(this, subset.set.clone, true)
          oldDist = min._2
        }
      }

    }

    if (direction == INIT_FORWARD)
      result += new BarcodeSubset(this, subset.set, true)

    new BarcodeSetCollection(result.toArray[BarcodeSet])
  }

  /**
   * Extracts the best barcode subset for each minimum distance
   * @param sets	The barcode subsets from which elements should be extracted
   * @param	bySize	If set to true, extracts only those subsets that comprise the maximum number of barcodes for each
   * possible minimum distance. Otherwise, extracts only those subsets with the best color balance for each
   * possible minimum distance
   * @return The extracted subsets
   */
  private def selectBestSubsetsByMinDist[T <: BarcodeSubset: ClassTag](sets: Array[T], bySize: Boolean = true): Array[T] = {

    // sort sets lexicographically according to the minimum distance and the number of elements
    val sortedInput = if (bySize)
      sets.sortWith((x1: T, x2: T) => x1.fitness._1 > x2.fitness._1 ||
        (x1.fitness._1 == x2.fitness._1 &&
          x1.fitness._3 > x2.fitness._3) ||
          (x1.fitness._1 == x2.fitness._1 && x1.fitness._3 == x2.fitness._3 && x1.fitness._2 - x2.fitness._2 > 0.0001))
    else
      sets.sortWith((x1: T, x2: T) => x1.fitness._1 > x2.fitness._1 ||
        (x1.fitness._1 == x2.fitness._1 &&
          x1.fitness._2 - x2.fitness._2 > 0.0001) ||
          (x1.fitness._1 == x2.fitness._1 && math.abs(x1.fitness._2 - x2.fitness._2) < 0.01 && x1.fitness._3 > x2.fitness._3))

    val result: ArrayBuffer[T] = new ArrayBuffer[T]
    var currentMinDist = -1

    for (el <- sortedInput) {
      // only add sets if no other set with the same minimum distance
      // has been added previously
      if (el.fitness._1 != currentMinDist) {
        result += el
        currentMinDist = el.fitness._1
      }
    }
    result.toArray[T]
  }

  /**
   * Selects barcode sets with maximum pairwise distances between the members
   * according to a multi-objective Genetic Algorithm
   * @param numIndividuals	The population size for the GA
   * @param numOffspring		The number of offspring in each generation of the GA
   * @param numRuns					The number of independent GA runs
   * @param numGenerations	The number of generations for which the GA is run
   * @param balanceColors		Specifies that colors (A/C versus G/T nucleotides) should
   * 							be balanced for each position across the barcode set
   * @param initialization	The initialization method for the population (see InitType)
   * @param progress	An optional listener that receives the progress of the barcode selection
   */
  def  selectBarcodesGA(numIndividuals: Int = 100, numOffspring: Int = 200,
    numRuns: Int = 1, numGenerations: Int = 1000, balanceColors: Boolean = false,
                        numStreams: Int = 8,
    initialization: InitType = InitType.INIT_FORWARD, progress: BarcodeProgressListener = null): BarcodeSetCollection =
    {
      this.balanceColors = balanceColors

      // create tuples of objectives and maximum values of these objectives for crowding distance calculation
      val maxDist = dist.flatten.filter(_ != Int.MaxValue).max.toDouble
      val crowdingObjectives = Array(0, 2) zip Array(maxDist, dist.length.toDouble)

      // store the best solutions across all runs in a separate object
      val allSolutions = new PopulationManager[BarcodeSubset](Array(), crowdingObjectives)

      for (run <- 1 to numRuns) {
        if (progress != null)
          // update progress
          if (!progress.progress(((run - 1) * numGenerations) * 1.0 / (numGenerations * numRuns) * 100,
            "Initializing..."))
            return null
        // initialize population according to the initialization type
        val individuals = new PopulationManager[BarcodeSubset](initialization match {
          case INIT_RANDOM => Array.tabulate(numIndividuals)((i: Int) => {
            // random generation of individuals
            val set = Array.fill(barcodes.length)(Random.nextBoolean())
            new BarcodeSubset(this, set, true)
          })
          case defaultCase =>
            // forward/backward selection
            val candidates = stepwiseBarcodeSelection(initialization).sets

            Array.tabulate(numIndividuals)((i: Int) => {
              val set =
                if (candidates.length > numIndividuals)
                // more candidates than required individuals => sample randomly
                  candidates(Random.nextInt(candidates.length)).asInstanceOf[BarcodeSubset].set
                else // not more candidates than individuals => take all candidates
                // and add random individuals if required
                  if (i < candidates.length)
                    candidates(i).asInstanceOf[BarcodeSubset].set
                  else
                    Array.fill(barcodes.length)(Random.nextBoolean())
              new BarcodeSubset(this, set, true)
            })
        }, crowdingObjectives)

        for (gen <- 1 to numGenerations) {

          if (progress != null)
            // update progress each generation
            if (!progress.progress(((run - 1) * numGenerations + gen) * 1.0 / (numGenerations * numRuns) * 100,
              "Run " + run + "/" + numRuns + " Iteration " + gen + "/" + numGenerations))
              return null

          // generate offspring in parallel
          val offspring = ParSeq.range(1, numOffspring / numStreams).flatMap(i => {
          //val offspring = ParSeq.range(1, numOffspring).flatMap((i) => {
            val parent1 = individuals.chooseRandom() crowdedComparison individuals.chooseRandom()
            val parent2 = individuals.chooseRandom() crowdedComparison individuals.chooseRandom()

            // generate two offspring by cross-over of the parents
            val (offspring1, offspring2) = parent1 recombine parent2

            // mutate offspring
            offspring1.mutate()
            offspring2.mutate()

            // calculate fitness of offspring, and add them to the population
            updateFitness(offspring1)
            updateFitness(offspring2)
            Array(offspring1, offspring2)
          })

          individuals.population ++= offspring.toIndexedSeq

          // determine Pareto fronts, and select the survivors
          // for the next generation by non-dominated sorting          
          individuals.nonDominatedSorting(numIndividuals)

          // update fitness map for quick calculation
          // (cannot be done in parallel)
          for (ind <- individuals.population)
            if (!storedFitness.contains(ind.set.deep))
              storedFitness(ind.set.deep) = ind.fitness

        }
        // add best individuals of the run to the result list
        allSolutions.population ++= individuals.population

      }

      // extract the non-dominated solutions from the full result set,
      // and make it unique by removing duplicates
      allSolutions.removeFitnessDuplicates(true)

      if (balanceColors)
        new BarcodeSetCollection(
          allSolutions.getNonDominated().sortWith((x1, x2) => x1.fitness._1 > x2.fitness._1 ||
            (x1.fitness._1 == x2.fitness._1 &&
              x1.fitness._3 < x2.fitness._3)).toArray[BarcodeSet])
      else
        new BarcodeSetCollection(selectBestSubsetsByMinDist(allSolutions.getNonDominated()).toArray[BarcodeSet]);
    }

}