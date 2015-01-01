package matrixmult;

import java.io.*;
import java.util.*;

/**
 * A dense matrix of integers.
 */
public class DenseMatrix implements Matrix{

	final int[] data;
	final int rows;
	final int columns;

	public DenseMatrix(int rows, int columns){
		data = new int[rows*columns];
		this.rows = rows;
		this.columns = columns;
	}

	public DenseMatrix(DenseMatrix[] matrices,int rows,int columns){
		data = new int[rows*columns];
		int index=0;
		for(int i=0;i<matrices.length;i++){
			System.arraycopy(matrices[i].data,0,data,index,matrices[i].data.length);
			index+=matrices[i].data.length;
		}
		this.rows = rows;
		this.columns = columns;

	}

	public void set(int row, int column, int value){
		data[row*columns+column]=value;
	}

	public void print(PrintWriter out)throws IOException{
		for(int i=0;i<rows;i++){
			for(int j=0;j<columns;j++){
				out.print("\t" + data[i*columns+j]);
			}
			out.println();
		}
		out.flush();
	}

	private int get(int row, int col){
		return data[row*columns+col];
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof DenseMatrix){
			if(other==this)
				return true;

			DenseMatrix o = (DenseMatrix) other;
			for(int i=0; i<rows; i++)
				for(int j=0; j<columns; j++)
					if(get(i,j)!=o.get(i,j))
						return false;
			return true;
		}
		else
			return false;
	}

}
