package main.code
package algorithm

import java.io.FileWriter
import java.io.BufferedWriter

/**
 * Reader and writer for text files containing barcodes
 */
object BarcodeReader {

  /**
   * Reads barcodes from a text file with one barcode in each line
   * @param file The name of the text file
   * @return The barcodes as a string array
   */
  def readBarcodes(file: String) :Array[String] = {
    val source = scala.io.Source.fromFile(file)
    val barcodes = source.getLines().toArray.map(_.toUpperCase)
    val length = if (barcodes.length > 0) barcodes(0).length() else 0

    for (line <- barcodes) {
      if (line.length() != length)
        throw new Exception("All barcodes must have the same length!")
      
      if (!line.matches("[ACGT]*"))
        throw new Exception("File contains non-nucleotide characters!")      
    }
    source.close()
    barcodes
  }
  
  /**
   * Writes a set of barcodes to a file with one barcode in each line
   * @param file The name of the file
   * @param barcodes The barcodes to write
   */
  def writeBarcodes(file: String, barcodes : Array[String]): Unit = {
    val output  = new BufferedWriter(new FileWriter(file))
    for (barcode <- barcodes){      
      output.write(barcode)
      output.newLine()
    }
    output.close()
  }
  
}