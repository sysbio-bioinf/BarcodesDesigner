package main.code
package algorithm

/**
 * Listener interface receiving the status message of a process
 * and the percentage of completion
 * @author Christoph Muessel
 */
trait BarcodeProgressListener {
  
  /**
   * Callback handler called when a progress is made
   * @param percentage	The percentage of completion
   * @param message	The message detailing the progress
   * @return true if the progress should continue,
   * false if it is cancelled
   */
  def progress(percentage: Double, message: String): Boolean
}