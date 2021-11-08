package main.code
package algorithm

import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
 * Static methods of the PopulationManager class
 */
object PopulationManager {

  /**
   * Determines a domination list for the supplied list of barcode sets.
   * Here, each entry of the resulting map comprises a set of solutions that dominate
   * the corresponding solution.
   * @param solutions	The array of solutions to investigate
   * @return The domination list
   */
  def getDominationList[T <: Individual[T]](solutions: Array[T]): Map[T, Set[T]] = {
    (solutions zip solutions.map(sol1 =>
      solutions.filter(sol2 => {
        sol2.dominates(sol1)
      }).toSet)).toMap
  }

  /**
   * Identifies the next Pareto front from a domination list
   * @param domList	The domination list, as returned by getDominationList()
   * @return A tuple, where the first element comprises the non-dominated solutions,
   * and the second element comprises the domination list of the remaining solutions
   * (to be used to determine the next Pareto front)
   */
  def getNextParetoFront[T <: BarcodeSet](domList: Map[T, Set[T]]): (Set[T], Map[T, Set[T]]) = {
    val nonDominated = (for (i <- domList.keys if domList(i).isEmpty) yield i).toSet
    var remainingList = domList
    for (i <- nonDominated) remainingList -= i
    remainingList = remainingList.mapValues(_.diff(nonDominated))
    (nonDominated, remainingList)
  }

  /**
   * Wrapper function that returns the array of non-dominated solutions for an array of solutions
   * @param solutions	The array of solutions to investigate
   * @return An array comprising the subset of solutions that are non-dominated
   */
  def getNonDominated[T <: Individual[T]: ClassTag](solutions: Array[T]): Array[T] = {
    val (paretoFront, _) = getNextParetoFront(getDominationList(solutions))
    paretoFront.toArray
  }
}

/**
 * A class managing a population of barcode sets and their Pareto fronts
 * @param initialPopulation	The initial population of barcode sets
 * @param crowdingObjectives An array of tuples specifying the indices and the normalization divisors
 * of objectives that are used to calculate the crowding distance
 */
class PopulationManager[T <: MOOIndividual[T] : ClassTag](initialPopulation: Array[T],
  crowdingObjectives: Array[(Int, Double)]) {

  // internally manage the population in an array buffer
  val population: ArrayBuffer[T] = new ArrayBuffer[T]

  // internally store the Pareto fronts
  val paretoFronts: ArrayBuffer[Set[T]] = new ArrayBuffer[Set[T]]
  population ++= initialPopulation
  updateFronts()

  /**
   * Recalculate the Pareto fronts of the current population
   */
  def updateFronts(): Any = {
    val domList = PopulationManager.getDominationList(population.toArray)

    // get first Pareto front
    var front = PopulationManager.getNextParetoFront(domList)
    var frontIndex = 0
    var stop = false

    paretoFronts.clear()
    // add current front to results
    paretoFronts += front._1

    do {

      // calculate crowding distances for individuals on the current front
      updateCrowdingDists(front._1)

      // update the stored Pareto front index of the individuals
      for (set <- front._1) {
        set.paretoFrontIndex = frontIndex
      }

      if (front._2.nonEmpty) {
        // continue with the next front if any solutions are left
        front = PopulationManager.getNextParetoFront(front._2)
        paretoFronts += front._1
        frontIndex += 1
        // add current front to results
      } else
        stop = true
    } while (!stop)
  }

  /**
   * Updates the crowding distances for the solutions on one Pareto front
   * @param front	The set of solutions on the Pareto front
   */
  private def updateCrowdingDists(front: Set[T]): Unit = {
    if (front.size == 1)
      front.head.crowdingDistance = Double.PositiveInfinity
    else {
      for (ind <- front)
        ind.crowdingDistance = 0.0
      // iterate over objectives
      for ((obj, norm) <- crowdingObjectives) {
        // sort the solutions by the current objective
        val objVals: Array[(T, Double)] = (front zip front.map(_.getFitnessValue(obj))).toArray.sortBy(_._2)

        for (i <- objVals.indices) {
          // determine crowding distance based on "neighbours" for the current objective
          if (i == 0 || i == objVals.length - 1)
            objVals(i)._1.crowdingDistance = Double.PositiveInfinity
          else {
            objVals(i)._1.crowdingDistance = objVals(i)._1.crowdingDistance +
              objVals(i + 1)._2 / norm - objVals(i - 1)._2 / norm
          }

        }
      }
    }
  }

  /**
   * Removes fitness duplicates from the population
   * @param mainOnly	If set to true, only the main Pareto objectives
   * (objective 1 and objective 3) are considered, while
   * objective 2 is ignored
   */
  def removeFitnessDuplicates(mainOnly: Boolean = false): Unit = {
    // group the population by fitness value
    val duplicates = population.groupBy(x => x.getFitnessGrouping)
    population.clear
    for ((_, value) <- duplicates) {
      // take one individual per unique fitness value, discard the others        
      population += value(Random.nextInt(value.size))
    }
  }

  /**
   * Sorts solutions by their Pareto fronts and crowding distances,
   * and extracts the top solutions. The remaining solutions are discarded.
   * @param cutoff	The number of solutions to keep in the population
   * @param eliminateDuplicates	Specifies whether fitness duplicates are removed
   * 				prior to non-dominated sorting
   */
  def nonDominatedSorting(cutoff: Int, eliminateDuplicates: Boolean = true): Unit = {
    var remaining: Int = cutoff
    var front: Int = 0
    if (eliminateDuplicates) {
      removeFitnessDuplicates()
      // ensure that the population size is not too small by re-adding duplicates if necessary
      while (population.size < cutoff) {
        val ind = population(Random.nextInt(population.size))
        population += ind.copy().asInstanceOf[T]
      }

    }

    updateFronts()
    population.clear

    while (remaining > 0) {
      if (paretoFronts(front).size > remaining) {
        // more individuals on the front than needed => take the solutions with the highest crowding distances

        val sortedFront = paretoFronts(front).toArray.sorted(Ordering.by((_: T).crowdingDistance).reverse)

        // take exactly the best *remaining* (integer value) solutions into population
        population ++= sortedFront.take(remaining)

        // remove remaining solutions on the current front and recalculate crowding distances
        paretoFronts(front) = paretoFronts(front) -- sortedFront.drop(remaining)

        updateCrowdingDists(paretoFronts(front))

        remaining = 0
      } else {
        // add the complete front to the result set
        population ++= paretoFronts(front)
        remaining -= paretoFronts(front).size
      }

      // proceed to next front
      front += 1
    }
    if (paretoFronts.size > front)
      // remove further Pareto fronts
      paretoFronts.trimEnd(paretoFronts.size - front)

  }

  /**
   * Choose a random individual/solution from the population
   * @return The chosen solution
   */
  def chooseRandom(): T = {
    population(Random.nextInt(population.size))
  }

  /**
   * Extract the non-dominated solutions from the current population
   * @return An array of non-dominated solutions
   */
  def getNonDominated(): Array[T] = {
    PopulationManager.getNonDominated(population.toArray)
  }

}