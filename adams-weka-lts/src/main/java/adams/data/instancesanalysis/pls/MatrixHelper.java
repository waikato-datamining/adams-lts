/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * MatrixAlgorithmHelper.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.instancesanalysis.pls;

import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import com.github.waikatodatamining.matrix.core.MatrixFactory;
import weka.core.matrix.Matrix;

/**
 * Helper class for the matrix-algorithm library.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MatrixHelper
  extends weka.core.matrix.MatrixHelper {

  /**
   * Turns a Jama matrix into a Weka one.
   *
   * @param jama	the jama matrix to convert
   * @return		the converted matrix
   */
  public static weka.core.matrix.Matrix jamaToWeka(Jama.Matrix jama) {
    return new weka.core.matrix.Matrix(jama.getArray());
  }

  /**
   * Turns a Weka matrix into a Jama one.
   *
   * @param weka	the Weka matrix to convert
   * @return		the converted matrix
   */
  public static Jama.Matrix wekaToJama(weka.core.matrix.Matrix weka) {
    return new Jama.Matrix(weka.getArray());
  }

  /**
   * Turns a matrix-algorithm matrix into a Weka one.
   *
   * @param matrixalgo	the jama matrix to convert
   * @return		the converted matrix
   */
  public static weka.core.matrix.Matrix matrixAlgoToWeka(com.github.waikatodatamining.matrix.core.Matrix matrixalgo) {
    return new weka.core.matrix.Matrix(matrixalgo.toRawCopy2D());
  }

  /**
   * Turns a Weka matrix into a matrix-algorithm one.
   *
   * @param weka	the Weka matrix to convert
   * @return		the converted matrix
   */
  public static com.github.waikatodatamining.matrix.core.Matrix wekaToMatrixAlgo(weka.core.matrix.Matrix weka) {
    return MatrixFactory.fromRaw(weka.getArray());
  }

  /**
   * Turns the matrix into a spreadsheet.
   *
   * @param matrix	the matrix to convert
   * @param colPrefix	the prefix for the column names
   * @return		the generated spreadsheet
   */
  public static SpreadSheet matrixToSpreadSheet(Matrix matrix, String colPrefix) {
    SpreadSheet	result;
    Row row;
    int		i;
    int		n;

    result = null;

    if (matrix != null) {
      result = new DefaultSpreadSheet();

      // header
      row = result.getHeaderRow();
      for (i = 0; i < matrix.getColumnDimension(); i++)
	row.addCell("" + i).setContent(colPrefix + (i+1));

      // data
      for (n = 0; n < matrix.getRowDimension(); n++) {
	row = result.addRow();
	for (i = 0; i < matrix.getColumnDimension(); i++)
	  row.addCell("" + i).setContent(matrix.get(n, i));
      }
    }

    return result;
  }
}
