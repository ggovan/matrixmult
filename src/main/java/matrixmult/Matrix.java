package matrixmult;

import java.io.PrintWriter;
import java.io.IOException;

/**
 * A matrix of integers.
 *
 * Allows for setting values within the matrix so that implementation agnostic code can read in and set matrices.
 * There is no ability to read the values in the matrix because it isn't needed.
 */
public interface Matrix{

	public void set(int row,int column,int value);

	/**
	 * Prints out the contents of this matrix in an efficient manner.
	 *
	 * @param out Writer to be written out to.
	 * @throws IOException 
	 */
	public void print(PrintWriter out)throws IOException;

}
