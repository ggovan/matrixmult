package sparsematrix 

import java.io._

object Run {

	def loadF(fname:String):SparseMatrix = {
		val in = new BufferedReader(new FileReader(fname))
    val line = in readLine() split("\\s+") filter(_ !="") map(_.toInt)
		new SparseMatrix(line(0),line(1),in)
	}

	def main (args:Array[String]) = {
    val logger = new PrintWriter(args(2)+".log")
    val times = if(args.length>4) args(4).toInt else 1
    logger println(System.currentTimeMillis + "\tStarting")
		val A = loadF(args(0))
		val B = loadF(args(1))
    A.LOGGER = logger
    B.LOGGER = logger
    var i = 0
    while(i<times){
  		val start = System.currentTimeMillis
  		val C =	if(args.length<4)
  					A multiply B 
  				else 
  					A multiply( B, Integer.parseInt(args(3)))
      val end = System.currentTimeMillis
      val timeTaken = end-start
  		println(s"Time Taken : $timeTaken ms")
      logger.println(System.currentTimeMillis + s"\tMultiplication took $timeTaken")
      i+=1
    }
// 		val out = new PrintWriter(args(2))
// 		C.print(out)
//		out.flush
    logger println(System.currentTimeMillis + "\tFinished")
    logger.flush
    logger.close
	}

}
