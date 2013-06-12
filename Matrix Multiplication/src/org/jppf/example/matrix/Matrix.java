/*
 * JPPF.
 * Copyright (C) 2005-2013 JPPF Team.
 * http://www.jppf.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jppf.example.matrix;

import java.io.Serializable;
//import java.util.Random;

/**
 * This class represents a square matrix of arbitrary size.
 * @author Laurent Cohen
 */
public class Matrix implements Serializable
{
  /**
   * Explicit serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  /**
   * The range of values for random values.
   */
  //private static final double RANDOM_RANGE = 1e6d;
  /**
   * The size of this matrix. The matrix contains size*size values.
   */
  private int rows;
  private int cols;
  /**
   * The values in this matrix.
   */
  public double[][] values;

  /**
   * Initialize this matrix with a specified size.
   * @param newSize the size of this matrix.
   */
  public Matrix(final int rows, final int cols, double[][] values)
  {
    this.rows = rows;
	this.cols = cols;
    this.values = values;
  }

  /**
   * Initialize this matrix with random values.
   */
  /*public void assignRandomValues()
  {
    //Random rand = new Random(System.currentTimeMillis());
    for (int i=0; i<values.length; i++)
    {
      for (int j=0; j<values[i].length; j++)
        // values in ]-RANDOM_RANGE, +RANDOM_RANGE[
        //values[i][j] = RANDOM_RANGE * (2d * rand.nextDouble() - 1d);
    }
  }*/

  /**
   * Get the size of this matrix.
   * @return the size as an integer value.
   */
  public int getRows()
  {
    return rows;
  }
  
  public int getCols()
  {
	return cols;
  }
	
  public int getSize()
  {
	return (rows+cols)/2;
  }
  /**
   * Get the row of matrix values at the specified index. Provided as a convenience.
   * @param row the row index.
   * @return the values in the row as an array of <code>double</code> values, or null if the row index is
   * greater than the matrix size.
   */
  public double[] getRow(final int row)
  {
    return (row < rows) ? values[row] : null;
  }

  /**
   * Get a value at the specified coordinates.
   * @param row the row coordinate.
   * @param column the column coordinate.
   * @return the specified value as a double.
   */
  public double getValueAt(final int row, final int column)
  {
    return values[row][column];
  }

  /**
   * Set a value to the specified coordinates.
   * @param row the row coordinate.
   * @param column the column coordinate.
   * @param value the value to set.
   */
  public void setValueAt(final int row, final int column, final double value)
  {
    values[row][column] = value;
  }

  /**
   * Compute the result of multiplying this matrix by another: thisMatrix x otherMatrix.
   * @param matrix the matrix to multiply this one by.
   * @return a new matrix containing the result of the multiplication.
   */
  public Matrix multiply(final Matrix matrix)
  {
    if (matrix.getRows() != this.cols) return null;
    Matrix result = new Matrix(this.rows, matrix.getCols(), new double[this.rows][matrix.getCols()]);
    for (int i=0; i<rows; i++)
    {
      for (int j=0; j<cols; j++)
      {
        double value = 0d;
        for (int k=0; k< rows; k++) value += matrix.getValueAt(k, j) * values[i][k];
        result.setValueAt(j, i, value);
      }
    }
    return result;
  }

  /**
   * Multiply a row of this matrix by another matrix.
   * The result is a row in the resulting matrix multiplication.
   * @param n the index of the row in this matrix.
   * @param matrix the matrix to multiply by.
   * @return a new row represented as an array of <code>double</code> values.
   */
  public double[] multiplyRow(final int n, final Matrix matrix)
  {
    double[] result = new double[cols];
    for (int col=0; col<cols; col++)
    {
      double sum = 0d;
      for (int row=0; row<rows; row++)
      {
        sum += matrix.getValueAt(row, col) * getValueAt(n, row);
      }
      result[col] = sum;
    }
    return result;
  }
  
  public void printConsole()
  {
		JSONArray list = new JSONArray();
		for(int i = 0; i < rows; i++)
		{	
			JSONObject obj = new JSONObject();
			for(int j = 0; j < cols; j++)
			{
				obj.put(values[i][j]);
			}
			list.add(obj);
		}
  }
}
