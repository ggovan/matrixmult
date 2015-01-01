package matrixmult;

import java.io.IOException;

import cern.colt.map.tdouble.AbstractLongDoubleMap;
import cern.colt.map.tdouble.OpenLongDoubleHashMap;
import cern.colt.matrix.io.MatrixInfo;
import cern.colt.matrix.io.MatrixSize;
import cern.colt.matrix.io.MatrixVectorReader;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.*;

public class SparseMatrix2D extends SparseDoubleMatrix2D {

    private static final long serialVersionUID = 1L;
    
	public SparseMatrix2D(int rows, int columns) {
        super(rows, columns, rows * (columns / 1000), 0.2, 0.5);
    }

    public DoubleMatrix2D zMult(DoubleMatrix2D B, DoubleMatrix2D C, final double alpha, double beta,
            final boolean transposeA, boolean transposeB) {
        if (!(this.isNoView)) {
            return super.zMult(B, C, alpha, beta, transposeA, transposeB);
        }
        if (transposeB)
            B = B.viewDice();
        int rowsA = rows;
        int columnsA = columns;
        if (transposeA) {
            rowsA = columns;
            columnsA = rows;
        }
        int p = B.columns();
        boolean ignore = (C == null);
        if (ignore)
            C = new DenseDoubleMatrix2D(rowsA, p);

        if (B.rows() != columnsA)
            throw new IllegalArgumentException("Matrix2D inner dimensions must agree:" + toStringShort() + ", "
                    + (transposeB ? B.viewDice() : B).toStringShort());
        if (C.rows() != rowsA || C.columns() != p)
            throw new IllegalArgumentException("Incompatibel result matrix: " + toStringShort() + ", "
                    + (transposeB ? B.viewDice() : B).toStringShort() + ", " + C.toStringShort());
        if (this == C || B == C)
            throw new IllegalArgumentException("Matrices must not be identical");

        if (!ignore)
            C.assign(cern.jet.math.tdouble.DoubleFunctions.mult(beta));

        // cache views
        final DoubleMatrix1D[] Brows = new DoubleMatrix1D[columnsA];
        for (int i = columnsA; --i >= 0;)
            Brows[i] = B.viewRow(i);
        final DoubleMatrix1D[] Crows = new DoubleMatrix1D[rowsA];
        for (int i = rowsA; --i >= 0;)
            Crows[i] = C.viewRow(i);

        final cern.jet.math.tdouble.DoublePlusMultSecond fun = cern.jet.math.tdouble.DoublePlusMultSecond.plusMult(0);

        this.elements.forEachPair(new cern.colt.function.tdouble.LongDoubleProcedure() {
            public boolean apply(long key, double value) {
                int i = (int) (key / columns);
                int j = (int) (key % columns);
                fun.multiplicator = value * alpha;
                if (!transposeA)
                    Crows[i].assign(Brows[j], fun);
                else
                    Crows[j].assign(Brows[i], fun);
                return true;
            }
        });

        return C;
    }

}
