package matrixmult;

import java.util.Random;
import java.io.*;

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;

public class MatrixMult{

	public static void main(String[] args)throws IOException{
		if(args.length==0){
			System.out.println(
					"Java program for performing Sparse Matrix Multiplication"+
					"Usage: matrixmult.MatrixMult fileA fileB outputC [n_threads]\n"+
					"Where files are of the form\n"+
					"<n_rows> <n_columns> <n_entries>\n"+
					"followed on each line by the rows, with values space separated."
					);
			return;
		}
		testMineParallel(args);
	}

	/**
	 * Multiplies two sparse matrices using the (parallel?) COLT library from CERN.
	 *
	 * @param args arguments of the form &lt;input1&gt; &lt;input2&gt;
	 */
	public static void test(String[] args)throws IOException{
		System.out.println("SEQ");
		SparseDoubleMatrix2D mat = loadFileColt(args[0]);//new SparseDoubleMatrix2D(size,size);
		SparseDoubleMatrix2D mat2 =loadFileColt(args[1]);// new SparseDoubleMatrix2D(size,size);
		long start = System.currentTimeMillis();
		DoubleMatrix2D res = mat.zMult(mat2,null,1,1,false,false);
		long end = System.currentTimeMillis()-start;
		System.out.println("Time taken : " + end);
	}

	/**
	 * Multiplies two sparse matrices using the SparseMatrix class.
	 * Output is either written to a file (if a third argument is given) or sent to the screen.
	 *
	 * @param args arguments of the form &lt;input1&gt; &lt;input2&gt; &lt;outputFile&gt; [&lt;n_threads&gt;]
	 * @see matrixmult.SparseMatrix
	 */
	public static void testMineParallel(String[] args)throws IOException{
		try(PrintWriter logger = new PrintWriter(args[2]+".log")){
			logger.println(System.currentTimeMillis() + "\tStarting");

			SparseMatrix mat =  loadFileMM(args[0]);//new SparseMatrix(size,size);
			SparseMatrix mat2 =  loadFileMM(args[1]);//new SparseMatrix(size,size);
			SparseMatrix.LOGGER=logger;
			long start = System.currentTimeMillis();
			Matrix res;
			if(args.length==3){
				res = SparseMatrix.multiply(mat,mat2);
			}
			else{
				int threads = Integer.parseInt(args[3]);
				res = SparseMatrix.multiplyParallel(mat,mat2,threads);
			}
			long end = System.currentTimeMillis();
		
			logger.println(end + "\tMultiplying took " + (end-start));
			System.out.println("Time taken : " +(end-start));
			logger.println(System.currentTimeMillis() + "\tWriting output");
			res.print(new PrintWriter(args[2]));
			logger.println(System.currentTimeMillis() + "\tFinished");
		}
	}
	
	/**
	 * Loads a SparseMatrix from the given file according to the standard.
	 *
	 * @param fname The name of the file to load.
	 * @return The loaded matrix.
	 * @see matrixmult.SparseMatrix
	 */
	public static SparseMatrix loadFileMM(String fname)throws IOException{
		try(BufferedReader in = new BufferedReader(new FileReader(fname))){

			// Header should have three values, #rows, #columns, #entries
			String[] props = in.readLine().split(" ");
			int rows = Integer.parseInt(props[0]);
			int cols = Integer.parseInt(props[1]);
			int entries = Integer.parseInt(props[2]);

			SparseMatrix mat = new SparseMatrix(rows,cols);
			String line;
			int row =0;
			while((line=in.readLine())!=null){
				String[] nums = line.split("\\s+");
				int cnum = 0;
				for(int col=0;col<nums.length;col++){
					// White space and empty strings can sneak in. Ignore them.
					if(nums[col].equals("")||nums[col].equals(" "))
						continue;
					if(nums[col].equals("0")){
						cnum+=1;
						continue;
					}
					mat.set(row,cnum,Integer.parseInt(nums[col]));
					cnum+=1;
				}
				row++;
			}
			return mat;
		}
	}

	/**
	 * Loads a COLT sparse matrix from the given file according to the standard.
	 * @param fname The name of the file to load.
	 * @return The loaded matrix.
	 */
	public static SparseDoubleMatrix2D loadFileColt(String fname)throws IOException{
		try(BufferedReader in = new BufferedReader(new FileReader(fname))){
			
			// Header
			String[] props = in.readLine().split(" ");
			int rows = Integer.parseInt(props[0]);
			int cols = Integer.parseInt(props[1]);
			int entries = Integer.parseInt(props[2]);

			SparseDoubleMatrix2D mat = new SparseDoubleMatrix2D(rows,cols);
			String line;
			int row =0;
			while((line=in.readLine())!=null){
				String[] nums = line.split(" ");
				for(int col=0;col<nums.length;col++){
					if(!nums[col].equals("0")){
						mat.setQuick(row,col,Double.parseDouble(nums[col]));
					}
				}
				row++;
			}
			return mat;
		}
	}

}
