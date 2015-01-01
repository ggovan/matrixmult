package matrixmult;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * A sparse matrix implementation.
 *
 * An array of rows are stored.
 * Each of these rows contain an ArrayList of <em>&lt;column_index,value&gt;</em> pairs.
 */
public class SparseMatrix implements Matrix{

	private final int rows,columns;
	private final IDRow[] data;
	private static final ID EMPTY = new ID(-1,0);
	public static PrintWriter LOGGER = new PrintWriter(System.out);

	public SparseMatrix(final int _rows, final int _columns){
		rows = _rows;
		columns = _columns;
		data = new IDRow[rows];
	}

	public int getNumRows(){
		return rows;
	}
	public int getNumColumns(){
		return columns;
	}

//	public int get(int row, int col){
//		if(data[row]==null)
//			return 0;
//		ID id = data[row].get(col);
//		if(id==null)
//			return 0;
//		return id.d;
//	}

	private IDRow getRow(int row){
		return data[row];

	}
	public void set(int row,int col, int value){
		if(row>=rows||col>=columns){
			throw new IndexOutOfBoundsException("Index out of bounds: Value " + value +" set at (" + row + "," + col + ") in matrix size of ("+rows+","+columns+")");
		}
		if(data[row]==null){
			IDRow r = new IDRow();
			data[row] = r;
			ID id =new ID(col,value); 
			r.add(id);
		}
		else{
			ID id =new ID(col,value); 
			data[row].add(id);
		}
	}

	/**
	 * Transpose a matrix.
	 * @param A Matrix to transpose.
	 * @return The transposed matrix.
	 */
	public static SparseMatrix transpose(SparseMatrix A){
		SparseMatrix B = new SparseMatrix(A.getNumColumns(),A.getNumRows());
		for(int i=0;i<A.rows;i++){
			IDRow row = A.data[i];
			if(row==null)continue;
			for(ID id : row){
				B.set(id.i,i,id.d);
			}
		}
		return B;
	}

	/**
	 * Multiply this matrix with another.
	 *
	 * @see SparseMatrix#multiply(SparseMatrix, SparseMatrix)
	 */
	public DenseMatrix multiply(SparseMatrix other){
		return multiply(this,other);
	}

	/**
	 * Multiply two matrices and return their product.
	 * First determines if the two matrices are fit for multiplication.
	 * Then transposes the second one so that it is stored in column major form.
	 * The multiplication is then performed and the results is returned in a new matrix.
	 * @param A The first matrix.
	 * @param B The second matrix.
	 * @return The resultant matrix.
	 */
	public static DenseMatrix multiply(SparseMatrix A, SparseMatrix B){
		LOGGER.println(System.currentTimeMillis() + "\tStarting Sequential Multiplication Method");
		int rows = A.getNumRows();
		int cols = B.getNumColumns();
		if(rows!=cols)
			throw new Error("HissyFit, matrices not the right sizes!");
		DenseMatrix C = new DenseMatrix(rows,cols);
		LOGGER.println(System.currentTimeMillis() + "\tTransposing");
		B = transpose(B);
		LOGGER.println(System.currentTimeMillis() + "\tFinished Transposing");
		LOGGER.println(System.currentTimeMillis() + "\tMultiplying");
		for(int i = 0; i<rows;i++){
			if(A.data[i]==null)continue;
			for(int j = 0;j<cols;j++){
				if(B.data[j]==null)continue;
				Iterator<ID> ait = A.data[i].iterator();
				Iterator<ID> bit = B.data[j].iterator();
				int sum = 0;
				boolean finished = !ait.hasNext()||!bit.hasNext();
				if(finished)
					continue;
				ID aV = ait.next();
				ID bV = bit.next();
				while(!finished){
					if(aV.i==bV.i){
						sum+=aV.d*bV.d;
						if(!ait.hasNext()||!bit.hasNext())
							finished = true;
						else{
							aV = ait.next();
							bV = bit.next();
						}
					}
					else if(aV.i<bV.i){
						if(ait.hasNext())
							aV = ait.next();
						else
							finished = true;
					}
					else if(bit.hasNext())
						bV=bit.next();
					else
						finished=true;
				}
				if(sum!=0){
					C.set(i,j,sum);
				}
			}
		}
		LOGGER.println(System.currentTimeMillis() + "\tFinished Multiplying");
		return C;
	}
	
	public static DenseMatrix multiplyParallel(SparseMatrix A, SparseMatrix B,int threads){
		LOGGER.println(System.currentTimeMillis() + "\tStarting Parallel Multiplication Method");
		int rows = A.getNumRows();
		int cols = B.getNumColumns();
		if(rows!=cols)
			throw new Error("HissyFit, matrices not the right sizes!");
		LOGGER.println(System.currentTimeMillis() + "\tTransposing");
		B = transpose(B);
		LOGGER.println(System.currentTimeMillis() + "\tFinished Transposing");
		LOGGER.println(System.currentTimeMillis() + "\tMultiplying");
		DenseMatrix C = new DenseMatrix(rows,cols);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(new MultiplyTask(0,rows,cols,A,B,C));
		LOGGER.println(System.currentTimeMillis() + "\tFinished Multiplying");
		return C;
	}

	private static class MultiplyTask extends RecursiveAction{
		final int startRow;
		final int rows;
		final int cols;
		DenseMatrix C;
		final SparseMatrix A,B;
		Thread t;
		final static int threshold = 1;

		MultiplyTask(int start, int rows, int columns, SparseMatrix A, SparseMatrix B, DenseMatrix C){
			startRow = start;
			this.rows=rows;
			this.cols = columns;
			this.A=A;
			this.B=B;
			this.C=C;
		}

		public void compute(){
			if(rows<=threshold){
				work();
			}
			else{
				int split = rows/2;
				invokeAll(new MultiplyTask(startRow,split,cols,A,B,C),
						new MultiplyTask(startRow+split,rows-split,cols,A,B,C));
			}
		}

		public void work(){
			int i=0;
			for(int runs = startRow; runs<rows+startRow;runs++){
				if(A.data[runs]==null)continue;
				for(int j = 0;j<cols;j++){
					if(B.data[j]==null)continue;
					Iterator<ID> ait = A.data[runs].iterator();
					Iterator<ID> bit = B.data[j].iterator();
					int sum = 0;
					boolean finished = !ait.hasNext()||!bit.hasNext();
					if(finished)
						continue;
					ID aV = ait.next();
					ID bV = bit.next();
					while(!finished){
						if(aV.i==bV.i){
							sum+=aV.d*bV.d;
							if(!ait.hasNext()||!bit.hasNext())
								finished = true;
							else{
								aV = ait.next();
								bV = bit.next();
							}
						}
						else if(aV.i<bV.i){
							if(ait.hasNext())
								aV = ait.next();
							else
								finished = true;
						}
						else if(bit.hasNext())
							bV=bit.next();
						else
							finished=true;
					}
					if(sum!=0){
						C.set(runs,j,sum);
					}
				}
			}
			i++;
		}
	}

	/**
	 * Print the matrix out on the specified PrintWriter.
	 * @param out The writer to be written on.
	 */
	public void print(PrintWriter out) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append(rows + " " + columns + " ");
		int entries = 0;
		for(IDRow row : data){
			entries+=row.size();
		}
		sb.append(entries);
		out.println(sb);
		
		for(IDRow row : data){
			sb = new StringBuilder();
			int index = 0;
			if(row!=null){
				for(ID id : row){
					if(id.i!=index){
						for(;index<id.i;index++)
						sb.append("\t0");
					}
					sb.append("\t"+id.d);
					index++;
				}
			}
			for(;index<columns;index++)
				sb.append("\t0");
			out.println(sb);
		}
		out.flush();
	}

	/**
	 * Stores the contents of a row.
	 * Note that contents must be added in order of index.
	 * Or the collection sorted after adding something out of order.
	 */
	 private static final class IDRow extends ArrayList<ID> {}

	/**
	 * A pairing of two numbers to represent the column index and value of a cell.
	 * The row number is not stored here and should be found else where.
	 * The <em>i</em> value is considered primary for equality and sorting purposes.
	 */
	private static final class ID implements Comparable<ID>{
		final int i;
		final int d;
		ID(final int _i,final int _d){
			i=_i;
			d=_d;
		}
		@Override
		public int hashCode(){
			return i;
		}
		@Override
		public int compareTo(ID o){
			return Integer.compare(i,o.i);
		}
		@Override
		public boolean equals(Object o){
			if(o instanceof ID)
				return i==((ID)o).i;
			return false;
		}
	}



}
