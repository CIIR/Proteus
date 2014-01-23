package code.lib

object Timer {
  private var start: Long = 0L
  private var end: Long = 0L
  def go = {
    start = System.currentTimeMillis
  }
  def stop = {
    end = System.currentTimeMillis
    print(">   " + (end - start) / 1000.0 + " s")
  }
}