package sparsematrix

import java.io._
import java.util._
import scala.collection.mutable._
import scala.collection.immutable._
import scala.concurrent.forkjoin._
import scala.collection.parallel.immutable.ParRange
import scala.collection.parallel._

class DenseMatrix (_rows:Int, _columns:Int){
	val rows = _rows
	val cols = _columns
	val data = new Array[Int](rows*cols)

	def this(_rows:Int,_cols:Int,_in:BufferedReader) = {
		this(_rows,_cols)
		var i = 0
		while(i<rows){
			val line = _in.readLine
			val nums = line.split(" ").map(_.toInt)
			var j = 0
			while(j<cols){
				update(i,j,nums(j))
				j+=1
			}
			i+=1
		}
	}

	def update(_row : Int, _column : Int, _value : Int) = {
		data(_row*cols + _column) = _value
	}

	def apply(_row:Int,_column:Int):Int = {
		data(_row*cols + _column)
	}

	def print(out : PrintWriter) = {
		out.println(rows + " " + cols)
		var r = 0
		while(r<rows){
			val sb = new StringBuffer
			var c = 0
			while(c<cols){
//Why do these two lines give greatly different times?				
				sb.append(apply(r,c) + "\t")
//				sb.append("\t" + apply(r,c))
				c+=1
			}
			out.println(sb)
			r+=1
		}
	}
}

class SparseMatrix (val rows:Int, val cols:Int){
	val data = new Array[(Int,ArrayBuffer[(Int,Int)])](rows)
  var LOGGER = new PrintWriter(System.out)

	for(i <- 0 until rows)
		data(i)=(i,new ArrayBuffer[(Int,Int)])

	def this(_row:Int,_col:Int,_in:BufferedReader) = {
		this(_row,_col)
		var i=0
		while(i<rows){
			val (rowi,row) = data(i)
			val line = _in.readLine
      val nums :Array[Int] = line.split("\\s+").filter(_!="").map(_.toInt)
			var c = 0
			while(c<cols){
				if(nums(c)!=0)
					row+=((c,nums(c)))
				c+=1
			}
			i+=1
		}
	}

	def transpose : SparseMatrix = {
		val out = new SparseMatrix(cols,rows)
		var i = 0
		while(i<rows){
			val (_,row) = data(i)
			var j = 0
			while(j<row.length){
				val (c,v) = row(j)
				out(c,i) = v
				j+=1
			}
			i+=1
		}
		out
	}

	def update(_row:Int,_col:Int,_val:Int)= {
		data(_row)._2+=((_col,_val))
	}

	def apply(_row:Int,_col:Int):Int = {
		var i = 0
		while(i < data(_row)._2.length){
			val (c,v) = data(_row)._2(i)
			if(_col==c)
				return v
			i+=1
		}
		return 0
	}

	def print(out : PrintWriter) = {
		out.println(rows + " " + cols)
		var i = 0
		while(i<rows){
			val (_,row) = data(i)
			val sb = new StringBuffer
			var next = 0
			var j = 0
			while(j<row.length){
				val (c,v) = row(j)
				while(c>next){
					sb.append("0\t")
					sb.append(v + "\t")
					next+=1
				}
				j+=1
			}
			j = next
			while(j < cols){
				sb.append("0 ")
				j+=1
			}
			out.println(sb)
			i+=1
		}
	}

	def multiply(other:SparseMatrix,threads:Int=1):DenseMatrix = {
    LOGGER.println(System.currentTimeMillis + "\tStarting Parallel Multiplication with threads set to " + threads)
		val A = this
		if(A.rows!=other.cols)
			return null
    LOGGER.println(System.currentTimeMillis + "\tStarting Transpose")
		val B = other.transpose
    LOGGER.println(System.currentTimeMillis + "\tFinished Transpose")
		val C = new DenseMatrix(A.rows,B.rows)
		val range = threads match{
      case 0 => (0 until A.rows).par
      case 1 => (0 until A.rows)
      case n => {
        val r = (0 until A.rows).par
        r.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(threads))
        r
      }
    }

		for(i <- range){
			val (_,arow) = A.data(i)
			var j = 0
			while(j<B.rows){
				val (_,brow) = B.data(j)
				var sum = 0
				var aj = 0
				var bj = 0
				while(aj<arow.length&&bj<brow.length){
					if(arow(aj)._1==brow(bj)._1){
						sum += arow(aj)._2*brow(bj)._2
						aj+=1
						bj+=1
					}
					else if(arow(aj)._1<brow(bj)._1)
						aj+=1
					else
						bj+=1
				}
				C.data(i*C.cols+j)=sum
				j+=1
			}
		}
		return C
	}
}

