package matrixmult

import org.scalatest._

class MatrixMultSpec extends FlatSpec with Matchers {

  "An identity matrix mult" should "be equal to the identity matrix" in {
    val size = 1000
    val matA = new SparseMatrix(size,size)
    identity(matA,size)
    val matB = new SparseMatrix(size,size)
    identity(matB,size)

    val identityMatrix = new DenseMatrix(size,size)
    identity(identityMatrix,size)

    matA.multiply(matB) should be (identityMatrix)
  }

  "A matrix mult" should "be correct" in {
    val matA = new SparseMatrix(2,3){
      set(0,0,1)
      set(0,1,2)
      set(0,2,3)
      set(1,0,4)
      set(1,1,5)
      set(1,2,6)
    }
    val matB = new SparseMatrix(3,2){
      set(0,0,7)
      set(0,1,8)
      set(1,0,9)
      set(1,1,10)
      set(2,0,11)
      set(2,1,12)
    }

    val expected = new DenseMatrix(2,2){
      set(0,0,58)
      set(0,1,64)
      set(1,0,139)
      set(1,1,154)
    }

    matA.multiply(matB) should be (expected)
  }


  def indentiy(m: Matrix, size: Int){
    for(i <- 0 until size)
      m.set(i,i,1)
  }

}
