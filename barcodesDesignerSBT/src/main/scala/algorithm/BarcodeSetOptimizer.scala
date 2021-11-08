package main.code
package algorithm

import scala.collection.mutable.ArrayBuffer
import scala.collection.parallel.immutable.ParSeq
import scala.util.Random
import math.Ordered.orderingToOrdered

/**
 * A barcode set class that serves as an individual of the barcode generation algorithm
 */
class RandomBarcodeSet(val parent : BarcodeSetOptimizer, var barcodes: Array[Array[Char]], var dist: Array[Array[Int]] = null, var minDists: Array[Int] = null, var distTable: Array[Array[Int]] = null) extends Individual[RandomBarcodeSet] {

  var fitness: (Int, Int) = (0, 0)
  var fitnessList: List[Double] = List(0.0, 0.0)
  var fitnessSize: Int = 2
  if (dist == null) {
    // calculate distance matrix and distance table
    dist = BarcodeDistanceCalculator.getBarcodeDists(barcodes)
    val x = BarcodeDistanceCalculator.getDistanceTable(dist, barcodes(0).length, 0 until barcodes.length)
    distTable = x._1
    minDists = x._2
  }

  /**
   * Creates an individual for the specified barcodes, and update the fitness if required.
   * @param barcodes	The barcodes
   * @param updateFitness	If set to true, the fitness will be recalculated
   */
  def this(parent : BarcodeSetOptimizer, barcodes: Array[Array[Char]], updateFitness: Boolean) = {
    this(parent, barcodes)
    if (updateFitness)
      this.updateFitness()
  }

  /**
   * Constructor that copies another barcode set object
   * @param copy	The other barcode set
   */
  def this(copy: RandomBarcodeSet) = this(copy.parent, copy.barcodes.clone(),
    copy.dist.map(_.clone()),
    copy.minDists.clone(),
    copy.distTable.map(_.clone()))

  def copy(): BarcodeSet = {
    new RandomBarcodeSet(this)
  }

  /**
   * Determines how often the minimum distance
   * occurs in each barcode
   * @return An array of counts with one entry for each barcode
   */
  private def getMinDistCounts: Array[Int] = {
    val min = minDists.min
    distTable.map(x => -x(min))
  }

  /**
   * Sets the distance between two barcodes,
   * and updates all internal structures
   * @param i	The index of the first barcode
   * @param j The index of the second barcode
   * @param d	The new distance
   */
  private def updateDist(i: Int, j: Int, d: Int): Unit = {
    if (i != j) {      
      val oldDist = dist(i)(j)

      // update the distance table
      distTable(i)(oldDist) -= 1
      distTable(j)(oldDist) -= 1
      distTable(i)(d) += 1
      distTable(j)(d) += 1
      
      // update the distance matrix
      dist(i)(j) = d
      dist(j)(i) = d

      // update the minimum distances
      if (oldDist == minDists(i)) {
        var n = minDists(i)
        while (n < distTable(i).length && distTable(i)(n) == 0)
          n += 1
        if (n == distTable(i).length)
          minDists(i) = Integer.MAX_VALUE
        else
          minDists(i) = n
      }

      if (d < minDists(i))
        minDists(i) = d

      if (oldDist == minDists(j)) {
        var n = minDists(j)
        while (n < distTable(j).length && distTable(j)(n) == 0)
          n += 1
        if (n == distTable(j).length)
          minDists(j) = Integer.MAX_VALUE
        else
          minDists(j) = n
      }

      if (d < minDists(j))
        minDists(j) = d
    }
  }

  def dominates(that: RandomBarcodeSet): Boolean = {
    this.fitness._1 > that.fitness._1 || //bug fix 300921 : originally it was "this.fitness._1 > that.fitness._2 || ..."
      (this.fitness._1 == that.fitness._1 && this.fitness._2 > that.fitness._2)
  }

  def dominates2(that: RandomBarcodeSet): Boolean = {
    var firstCondition = true; // solution A is not worse in all objectives than solution B
    for (fitnessIdx <- 0 to fitnessList.length) {
      firstCondition = firstCondition && (this.fitnessList.lift(fitnessIdx) >= that.fitnessList.lift(fitnessIdx))
    }

    var secondCondition = false; // solution A is better in at least one objective than solution B
    for (fitnessIdx <- 0 to fitnessList.length) {
      secondCondition = secondCondition || (this.fitnessList.lift(fitnessIdx) > that.fitnessList.lift(fitnessIdx))
    }

    firstCondition && secondCondition

  }

  /**
   * Updates the fitness of the individual
   */
  def updateFitness(): Unit = {
    val minDistCounts = getMinDistCounts
    fitness = (minDists.min,
      minDistCounts.sum) //
    fitnessList = List(minDists.min, minDistCounts.sum)
  }

  /**
   * Applies a mutation to the individual
   */
  def mutate(): Unit = {

    for (i <- 0 until Random.nextInt(parent.mutateCount) + 1) {
      // mutate <mutateCount> barcodes one after the other
      
      // identify the barcodes that have the maximum number of minimal distances
      // to other barcodes, and choose one randomly
      val minDistCounts = getMinDistCounts
      var queue = minDists.zip(minDistCounts).zipWithIndex
      val min = queue.minBy(_._1)._1
      queue = queue.filter(x => x._1 == min)
      val el = queue(Random.nextInt(queue.length))

      val idx = el._2

      // replace this barcode by a new barcode
      val newBarcode = Array.fill(barcodes(0).length)('_')
      var GCPerc = 0.0
      do {
        // assign first position randomly
        newBarcode(0) = BarcodeSetOptimizer.nucleotides(Random.nextInt(4))
        for (i <- 1 until barcodes(0).length) {
          // iterate over nucleotide positions

          // randomly choose between intelligent choice and random choice 
          if (Random.nextBoolean) {
            // intelligent choice of next nucleotide:
            // use the nucleotide that occurs most infrequently
            // among the most similar barcodes
            var max = 0
            val maxIndices = new ArrayBuffer[Int]
            
            for (j <- 0 until barcodes.length) {
              // iterate over barcodes to identify the most similar barcodes
              if (j != idx) {                
                
                // count the number of equal nucleotides
                // between the new barcode and the current barcode
                // for the first i-1 positions
                var equal = 0
                for (k <- 0 until i) {
                  if (barcodes(j)(k) == newBarcode(k))
                    equal += 1
                }

                if (equal > max) {
                  // new maximum => reset index list
                  max = equal
                  maxIndices.clear
                }

                if (equal >= max) {
                  // this barcode is most similar and must be added to the maximum list
                  maxIndices += j
                }
              }

              // determine the nucleotide distribution at position i
              // among the most similar barcodes
              val counts = Array.fill(4)(0)
              for (b <- maxIndices) {
                val nuc = barcodes(b)(i) match {
                  case 'A' => 0
                  case 'C' => 1
                  case 'G' => 2
                  case 'T' => 3
                  case _ => -1
                }
                counts(nuc) += 1
              }
              // assign the least frequent nucleotide
              newBarcode(i) = BarcodeSetOptimizer.nucleotides(counts.zipWithIndex.minBy(_._1)._2)
            }
          }
          else
             // choose the nucleotide at random
             newBarcode(i) = BarcodeSetOptimizer.nucleotides(Random.nextInt(4))
        }
        // determine G/C percentage of the new barcode
        GCPerc = newBarcode.map(x => if (x == 'G' || x == 'C') 1 else 0).sum * 1.0 / newBarcode.length
        //reject barcodes not obeying to the required minimum/maximum percentages
      } while (GCPerc < parent.minGC || GCPerc > parent.maxGC)
      
      // replace old barcode by new barcode 
      barcodes(idx) = newBarcode

      // update distances to all other barcodes
      for (i <- 0 until barcodes.length) {
        if (i != idx) {
          val newDist = BarcodeDistanceCalculator.dist(newBarcode, barcodes(i))

          updateDist(idx, i, newDist)
        }
      }
    }
  }

  /**
   * Recombines this individual and another individual by randomly exchanging barcodes
   * @param that	The second individual
   * @return A tuple containing the two offspring
   */
  def recombine(that: RandomBarcodeSet): (RandomBarcodeSet, RandomBarcodeSet) = {
    
    // determine the indices of the barcodes that should be exchanged
    val shuffleIndices = Random.shuffle(0 to barcodes.length - 1).take(Random.nextInt(math.max(1,math.round(barcodes.length * 0.1).toInt)))

    // create copies of the parents
    val child1 = new RandomBarcodeSet(this)
    val child2 = new RandomBarcodeSet(that)

    // exchange barcodes
    for (i <- shuffleIndices) {
      child1.barcodes(i) = that.barcodes(i)
      child2.barcodes(i) = this.barcodes(i)
    }
    
    // update the distances of the exchanged barcodes to the remaining barcodes
    for (i <- shuffleIndices) {
      for (j <- 0 until barcodes.length) {
        val d1 = BarcodeDistanceCalculator.dist(child1.barcodes(i), child1.barcodes(j))
        val d2 = BarcodeDistanceCalculator.dist(child2.barcodes(i), child2.barcodes(j))
        child1.updateDist(i, j, d1)
        child2.updateDist(i, j, d2)
      }
    }
    (child1, child2)
  }

  def getBarcodes: Array[String] = {
    barcodes.map(_.mkString)
  }

  def getFitnessGrouping: Product = {
    fitness
  }

  def getFitnessValue(index: Int): Double = {
    if (index == 0)
      fitness._1
    else if (index == 1) {
      fitness._2
    } else {
      fitness._2
    }
  }

  def getMinDist: Int = {
    fitness._1
  }

  def getNumBarcodes: Int = {
    barcodes.length
  }

  override def toString(): String =
    {
      if (barcodes.isEmpty)
        "(empty barcode set)"
      else if (barcodes.length == 1)
        "1 barcode"
      else
        barcodes.length + " barcodes with minimum distance " + fitness._1;
    }

}

object BarcodeSetOptimizer {
  /**
   * An array containing the possible nucleotides
   */
  val nucleotides: Array[Char] = Array('A', 'C', 'G', 'T')
}

/**
 * A class that optimizes randomly generated sets of barcodes according to their
 * minimum distances
 * @param barcodePattern	A string specifying the fixed and non-fixed nucleotide positions
 * @param numBarcodes	The number of barcodes in the result set
 * @param minGC	The minimum fraction of G/C nucleotides in each barcode
 * @param maxGC The maximum fraction of G/C nucleotides in each barcode
 * @param startWithHammingCodes	Specifies whether the algorithm is initialized with 
 * Hamming codes (true) or with randomly generated codes (false)
 */
class BarcodeSetOptimizer(val barcodePattern: String, val numBarcodes: Int, 
                          val minGC : Double = 0.4, val maxGC : Double = 0.6, 
                          val startWithHammingCodes : Boolean = true) {

  //determine the number of non-fixed nucleotides
  val effectiveBarcodeLength: Int = barcodePattern.count(_ == '_')

  // determine the number of mutations
  val mutateCount: Int = math.min(100, math.max(1, math.round(numBarcodes * 0.0025).toInt))

  /**
   * Performs a tournament selection among a subset of individuals
   * @param individuals The individuals to sample from
   * @param size	The tournament size, i.e. the number of randomly chosen candidates
   * @return The best individual among the chosen candidates
   */
  private def tournament(individuals: Seq[RandomBarcodeSet], size: Int = 3): RandomBarcodeSet = {
    val subset = Array.tabulate(size)(i => individuals(Random.nextInt(individuals.size)))
    subset.maxBy(_.fitness)
  }

  /**
   * Finalizes the internal solutions of the algorithm by inserting the fixed nucleotides
   * at the specified position (the algorithm omits these positions)
   * @param solutions	The internal solutions/individuals of the algorithm
   * @return An array of barcode sets into which the fixed positions have been inserted
   */
  private def finalizeSolutions(solutions : Seq[RandomBarcodeSet]) : Array[BarcodeSet] = {
    solutions.map(sol => {
      new SimpleBarcodeSet(sol.barcodes.map(barcode => {
        var i = 0
        barcodePattern.map(c => {
          if (c != '_')
            // fixed position => use the specified nucleotide 
            c
          else {
            // non-fixed position => use the next nucleotide in the generated barcode
            i += 1
            barcode(i-1)
          }            
        })
      }))
    }).toArray
  }
  
  /**
   * Selects barcode sets with maximum pairwise distances between the members
   * according to a Genetic Algorithm
   * @param numIndividuals	The population size for the GA
   * @param numOffspring		The number of offspring in each generation of the GA
   * @param numRuns					The number of independent GA runs
   * @param numGenerations	The number of generations for which the GA is run
   * @param earlyStoppingMinDist	The minimum distance at which the algorithm terminates
	 * (or Integer.MAX_INT to disable early stopping)
   * @param progress	An optional listener that receives the progress of the barcode optimization
   */
  def optimizeBarcodeSets(numIndividuals: Int = 100, numOffspring: Int = 200,
    numRuns: Int = 1, numGenerations: Int = 1000, earlyStoppingMinDist: Int = Integer.MAX_VALUE,
                          numStreams: Int = 4, progress: BarcodeProgressListener = null): BarcodeSetCollection =
    {

      // store the best solutions across all runs in a separate object
      val allSolutions = new ArrayBuffer[RandomBarcodeSet]()

      for (run <- 1 to numRuns) {
        if (progress != null)
          // update progress
          if (!progress.progress(((run - 1) * numGenerations) * 1.0 / (numGenerations * numRuns) * 100,
            "Initializing..."))
            return null
        val init = new BarcodeGenerator(Array.fill(effectiveBarcodeLength)('_').mkString(""), minGC, maxGC, null)

        // initialize population by generating random barcode sets or sets of Hamming codes
        var individuals = new ArrayBuffer[RandomBarcodeSet]() ++ Array.tabulate(numIndividuals)((i: Int) => {
          val barcodes = init.generateBarcodes(numBarcodes, 1000, startWithHammingCodes).map(_.toArray)
          new RandomBarcodeSet(this, barcodes, true)
        })

        var gen = 0

        do {
          // iterate over generations
          gen += 1
          if (progress != null)
            // update progress each generation
            if (!progress.progress(((run - 1) * numGenerations + gen) * 1.0 / (numGenerations * numRuns) * 100,
              "Run " + run + "/" + numRuns + " Iteration " + gen + "/" + numGenerations + " Dist " + individuals(0).getMinDist +
                " DistCount " + individuals(0).fitness._2))
              return null
          // generate offspring in parallel
          val offspring = ParSeq.range(1, numOffspring / numStreams).flatMap(i => {

            // determine parents by tournament selection among three random individuals
            val parent1 = tournament(individuals)
            val parent2 = tournament(individuals)

            // generate two offspring either by cross-over of the parents
            // or by copying the parents
            val (offspring1, offspring2) =
              if (Random.nextBoolean())
                parent1 recombine parent2
              else
                (new RandomBarcodeSet(parent1), new RandomBarcodeSet(parent2))

            // mutate offspring
            offspring1.mutate()
            offspring2.mutate()

            // calculate fitness of offspring, and add them to the population
            offspring1.updateFitness()
            offspring2.updateFitness()
            Array(offspring1, offspring2)
          })

          // merge parents and offspring, and keep the best <numIndividuals> candidates
          individuals ++= offspring.toIndexedSeq
          individuals = individuals.sorted(Ordering.by((_: RandomBarcodeSet).fitness).reverse).take(numIndividuals)
        }
        // terminate if maximum number of generations has been reached or
        // if the minimum distance matches the early stopping criterion
        while (gen <= numGenerations && individuals(0).getMinDist < earlyStoppingMinDist)
        // add best individuals of the run to the result list
        allSolutions ++= individuals

      }
      
      // extract the best individual, and post-process it by inserting 
      // the fixed nucleotides into the barcodes      
      new BarcodeSetCollection(finalizeSolutions(allSolutions.take(1)))
    }

}