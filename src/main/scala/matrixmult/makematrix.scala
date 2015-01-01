import java.util.Random
import java.io.PrintWriter

object makematrix {
	def main(args:Array[String]) = {
		val size = args(0).toInt
		val p = args(1).toDouble
		val fn = args(2)

		val r = new Random()
		val out = new PrintWriter(fn)
		out.println(size + " "+size)
		var i = 0
		while(i<size){
			var j = 0
			while(j<size){
				if(p>r.nextDouble()){
					out print(r.nextInt(100)+" ")
				}
				else{
					out print("0 ")
				}
				j+=1
			}
			out.println()
			i+=1
		}
		out.flush()
		out.close()
	}
}
