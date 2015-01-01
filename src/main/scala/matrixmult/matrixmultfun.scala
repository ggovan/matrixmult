package sparsematrix

import java.io._
import sparsematrix._
import scala.collection.immutable._
import scala.collection.parallel.ForkJoinTaskSupport

class BufferedReaderIterator(val br:BufferedReader) extends Iterator[String]{
  private var n = br.readLine()
  def hasNext():Boolean = n!=null
  def next():String = {
    val old = n
    n = br.readLine
    //println(old)
    old
  }
}


object SparseFunMatrix {
  def apply(in:BufferedReader):Matrix = {
    val bri = new BufferedReaderIterator(in)
    val lines:List[List[Int]] = (for(line <- bri) yield line.trim.split("\\s").toList.map(_.toInt)).toList
    val rows:List[List[Column]] = lines.map(_.zipWithIndex.filter(_._1!=0).map{case(v,i)=>(i,v)})

    val matrix:Matrix = rows.zipWithIndex.map{case(v,i)=>(i,v)}.toList

    matrix
  }
  def apply(fname:String):Matrix = {
    val br = new BufferedReader(new FileReader(fname))
    br.readLine()
    val m = apply(br)
    br.close()
    m
  }


  def main(args:Array[String]){
    val A = apply(args(0))
    val B = apply(args(1))
    val times = if(args.length>4) args(4).toInt
    else 1
    println(times)
    var i = 0
    while(i!=times){
      val threads = if(args.length>3) args(3).toInt else 1
      val start = System.currentTimeMillis
      val C = multiply(A,B,threads)
      println(threads + " " + C.length)
      val end = System.currentTimeMillis
      val timeTaken = end-start
      println(s"Time Taken : $timeTaken ms")
      i+=1
    }
    //   output(C,args(2))
  }

  def output(mat:Matrix,fname:String) {
    val out = new PrintWriter(fname)
    val size = mat.length

    def outRow(row:Row) {
      val sb = new StringBuffer
      def o(i:Int,cs:List[Column]):Unit = cs match {
        case Nil => {
          sb.append(0)
          if(i!=size-1){
            sb.append("\t")
            o(i+1,Nil)
          }
        }
        case (c,v)::t => {
          if(i==c) sb.append(v)
            else sb.append(0)
          if(i!=size-1){
            sb.append("\t")
            if(i==c) o(i+1,t)
              else o(i+1,(c,v)::t)
          }
        }
      }

      o(0,row._2)
      out.println(sb.toString)
    }

    out.println(size + " " + size)

    mat.foreach(outRow)

    out.close()
  }

  def transpose(mat:Matrix):Matrix = {
    val flattened:List[(Int,Int,Value)] = mat.flatMap{case(r,row)=>row.map{case(c,value)=>(c,r,value)}}
    val grouped:List[(Int,List[(Int,Int,Value)])] = flattened.groupBy(_._1).toList
    val newMat = grouped.map{case(r,rcvs)=>(r,rcvs.map{case(_,c,v)=>(c,v)}.sortBy(_._1))}.sortBy(_._1)

    newMat
  }

  def multiply(mat:Matrix,other:Matrix,threads:Int = 1):Matrix = {
    val A = mat
    val start = System.currentTimeMillis
    val B = transpose(other)
    val end = System.currentTimeMillis
    println("Transpose took :" + (end-start))

    def sumRowsMult(a:List[Column],b:List[Column]):Value = (a,b) match {
      case (Nil,_) => 0
      case (_,Nil) => 0
      case ((c1,v1)::t1,(c2,v2)::t2) => 
        if(c1==c2) v1*v2 + sumRowsMult(t1,t2)
        else if(c1 < c2) sumRowsMult(t1,b)
        else sumRowsMult(a,t2)
    }

    def vecMult(row:Row,mat:Matrix):Row = (row._1,mat.map{case(c,col)=>(c,sumRowsMult(row._2,col))})

    val result = threads match{
      case 1 => A.map{row=>vecMult(row,B)}
      case 0 => A.par.map{row=>vecMult(row,B)}.toList
      case n => {
        val par = A.par
        par.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(threads))
        par.map{row=>vecMult(row,B)}.toList
      }
    }

    result
  }

}

