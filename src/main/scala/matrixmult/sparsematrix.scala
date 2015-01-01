package sparsematrix

object sparsematrix {

type Value = Int
type Column = (Int,Value)
type Row = (Int,List[Column])
type Matrix = List[Row]

}
