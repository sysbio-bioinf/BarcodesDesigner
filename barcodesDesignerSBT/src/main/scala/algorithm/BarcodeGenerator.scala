package main.code
package algorithm

import scala.collection.mutable
import scala.util.Random

object BarcodeGenerator {

  /**
   * Assignment of indices to nucleotides
   */
  val nucleotides: Array[Char] = Array('A', 'C', 'G', 'T')
}
/**
 * Class that generates a set of random barcode strings with the specified parameters
 * @author Christoph Muessel, Marietta Hamberger
 * @param barcodePattern	The pattern specifying which positions can be set randomly and which are fixed
 * @param minGC	The minimum fraction of G/C nucleotides in each barcode
 * @param maxGC	The maximum fraction of G/C nucleotides in each barcode
 * @param progress	An optional listener that receives the progress of the barcode generation
 */
class BarcodeGenerator(var barcodePattern: String, minGC: Double, maxGC: Double, progress: BarcodeProgressListener = null) {

  if (progress != null)
    progress.progress(0, "Initializing...")

  // calculate the number of positions that can be generated randomly
  val effectiveBarcodeLength: Int = (for (i <- 0 until barcodePattern.length
                                          if barcodePattern(i) == '_') yield i).length

  //val hammingLength = effectiveBarcodeLength - math.floor(math.log(effectiveBarcodeLength) / math.log(2)).toInt - 1

  // determine the number of G/C nucleotides in the pattern
  val GCcount: Int = barcodePattern.map(x => if (x == 'G' || x == 'C') 1 else 0).sum

  // calculate the probability of G/C nucleotides based on the mean of minGC and maxGC and the 
  // already existing G/C nucleotides in the pattern
  val GCprob: Double = (barcodePattern.length * ((minGC + maxGC) / 2) - GCcount) / effectiveBarcodeLength

  /**
   * Generates a set of barcodes with the supplied parameters.
   * @param numBarcodes	The number of barcodes to generate
   * @param maxTries The maximum number of tries to generate a new barcode
   * 									before the generator gives up throwing an Exception
   * @return A string array of generated barcodes
   * @throws Exception if the barcodes could not be generated
   */
  def generateBarcodes(numBarcodes: Int, maxTries: Int = 1000, hamming: Boolean = false): Array[String] = {
    val codes = new mutable.HashSet[String]
    var tries = 0
    if ((hamming &&  numBarcodes > math.pow(4, effectiveBarcodeLength - math.floor(
      math.log(effectiveBarcodeLength)/math.log(2)) - 1)) || numBarcodes > math.pow(4, effectiveBarcodeLength))

      // the requested number of barcodes is higher than the number of possible barcodes 
      throw new Exception("It is impossible to generate the requested number of barcodes with the specified length!")
    while (codes.size < numBarcodes) {
      val barcode = if (hamming) generateHamming() else generateRandom()

      // determine the fraction of G/C nucleotides in the generated barcode
      val GCPerc = barcode.map(x => if (x == 'G' || x == 'C') 1 else 0).sum * 1.0 / barcodePattern.length
      if (GCPerc >= minGC && GCPerc <= maxGC && !codes.contains(barcode)) {
        // barcode is in the valid G/C range and not yet in the result set
        // => add it
        codes.add(barcode)
        tries = 0
        if (progress != null)
          progress.progress(codes.size * 1.0 / numBarcodes, "Generating random barcodes...")
      } else {
        // barcode is already in the set or has an invalid percentage of G/C nucleotides
        // => retry
        tries += 1
        if (tries > maxTries)
          throw new Exception("Unable to generate the requested number of barcodes!")
      }
    }
    if (progress != null)
      progress.progress(1.0, "Barcode generation finished!")

    // return the set as an array of strings
    codes.toArray
  }

  /**
  * Generates a random barcode string based on the barcode pattern and the GC probability
  * @return the generated barcode string
  */
  def generateRandom(): String = {
    String.valueOf(barcodePattern.map(x => {
      if (x != '_')
        // fixed position => return specified nucleotide
        x
      else {
        // random position
        val rand = Random.nextDouble()
        // first determine if G/C or A/T is generated,
        // then determine the exact nucleotide
        if (rand < GCprob) {
          if (Random.nextBoolean())
            'G'
          else
            'C'
        } else {
          if (Random.nextBoolean())
            'A'
          else
            'T'
        }
      }
    }))
  }

  /**
  * Generates a random barcode based on a Hamming code, the barcode pattern and the GC probability
  * @return the generated barcode string
  */
  def generateHamming(): String = {

    var j = 1
    // generate a skeleton for all non-fixed positions, setting
    // free positions randomly and parity positions to 0
    val barcodeSkeleton = (for (e <- barcodePattern if e == '_')
        yield
        {           
           val ret = if ((j & j - 1) != 0) {            
            // random position
            val rand = Random.nextDouble()
            // first determine if G/C or A/T is generated,
            // then determine the exact nucleotide
            if (rand < GCprob) {
              if (Random.nextBoolean())
                0
              else
                1
            } else {
              if (Random.nextBoolean())
                2
              else
                3
            }
          } else
            0
           j += 1
           ret
        }).toArray
    
    // now, calculate the parity nucleotides
    for (j <- 1 to barcodeSkeleton.length)
    {
      barcodeSkeleton(j - 1) = 
        if ((j & j-1) == 0) {
          if (j == barcodeSkeleton.length)
          // special treatment: If last bit is a power of two, this is an overall parity
            barcodeSkeleton.sum % 4
          else
          // parity over all barcodes in whose position the j-th bit is set
            (for (k <- 1 to barcodeSkeleton.length if k != j && (k & j) > 0) yield barcodeSkeleton(k - 1)).sum % 4
        } 
        else
          barcodeSkeleton(j - 1)
    }
    
    // construct the final barcode by inserting the fixed positions
    // and by converting the numbers to nucleotides
    j = 0
    String.valueOf(Array.tabulate(barcodePattern.length)(i => {
      val ret = if (barcodePattern(i) != '_')
        barcodePattern(i)
      else
        BarcodeGenerator.nucleotides(barcodeSkeleton(j))
        
      if (barcodePattern(i) == '_')
        j += 1
        
      ret
    }))
    
  }

}