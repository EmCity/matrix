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

import java.util.List;
import org.json.simple.*;
import org.jppf.client.*;
import org.jppf.node.policy.*;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.MemoryMapDataProvider;
import org.jppf.utils.*;
import org.slf4j.*;

/**
 * Runner class for the square matrix multiplication demo.
 * @author Laurent Cohen
 */
public class MatrixRunner
{
  /**
   * Logger for this class.
   */
  static Logger log = LoggerFactory.getLogger(MatrixRunner.class);
  /**
   * JPPF client used to submit execution requests.
   */
  private JPPFClient jppfClient = null;
  /**
   * Keeps track of the current iteration number.
   */
  private int iterationsCount = 0;

  /**
   * Entry point for this class, performs a matrix multiplication a number of times.,<br>
   * The number of times is specified as a configuration property named &quot;matrix.iterations&quot;.<br>
   * The size of the matrices is specified as a configuration property named &quot;matrix.size&quot;.<br>
   * @param args - not used.
   */
  public String[] go(String[] input)
  {
    try
    {
      //if ((args != null) && (args.length > 0)) jppfClient = new JPPFClient(args[0]);
      jppfClient = new JPPFClient();
	  JSONArray array;
	  JSONArray array2;
	  //String input1 = "[{\"0\":44, \"1\":22}, {\"0\":1, \"1\":3}]";
	  //String input2 = "[{\"0\":10, \"1\":44}, {\"0\":2, \"1\":6}]";
	  if (input[1] != null & input[2] != null)
	  {
	 
		output("Heeello");
		//here is the JSON with the matrix values
		Object obj=JSONValue.parse(input[1]);
		array=(JSONArray)obj;
		Object obj2=JSONValue.parse(input[2]);
		array2=(JSONArray)obj2;
		  
		  //Get the single elements of the JSON and thus be able to find out number of rows and columns
		  //
		  //
		  double[][] values1; //values of first matrix
		  double[][] values2; //values of second matrix
		  int rows1 = array.size();
		  int rows2 = array2.size();
		  output("rows1: " + rows1 + "rows2: " + rows2);
		  JSONObject j1 = (JSONObject)array.get(0);
		  JSONObject j2 = (JSONObject)array2.get(0);
		  output("j1: " + j1 + "j2: " + j2);
		  int cols1 = j1.size();	  
		  int cols2 = j2.size();
		  output("cols1: " + cols1 + "cols2: " + cols2);
		  values1 = new double[rows1][cols1];
		  values2 = new double[rows2][cols2];
		  for(int i = 0; i < rows1; i++)
		  {
			for(int j = 0; j < cols1; j++)
			{
				JSONObject thisRow = (JSONObject)array.get(i);
				output("row: " + thisRow);
				String js = String.valueOf(j);
				String bla = thisRow.get(js).toString();
				//output(bla);
				double valueOf = Double.parseDouble(bla);
				//output(String.valueOf(valueOf));
				//long thisValuef = (long)thisRow.get(js);
				//double thisValue = thisValuef.doubleValue();
				//output("value: " + thisValue);
				values1[i][j] = valueOf;
				
			}
		  }
		  for(int i = 0; i < rows2; i++)
		  {
			for(int j = 0; j < cols2; j++)
			{
				JSONObject thisRow = (JSONObject)array.get(i);
				output("row: " + thisRow);
				String js = String.valueOf(j);
				String bla = thisRow.get(js).toString();
				//output(bla);
				double valueOf = Double.parseDouble(bla);
				values2[i][j] = valueOf;
			}
		  }
		  
		  TypedProperties props = JPPFConfiguration.getProperties();
		  //how big is the matrix
		  int size = props.getInt("matrix.size", 300);
		  //how many times to calculate
		  int iterations = props.getInt("matrix.iterations", 10);
		  //number of rows of matrix a per task
		  int nbRows = props.getInt("task.nbRows", 1);
		  output("Running Matrix demo with matrix size = "+size+"*"+size+" for "+iterations+" iterations");
		  Matrix c = perform(values1, values2, iterations, nbRows);
		  String[] result = new String[2];
		  output("Array created");
		  result[0] = input[0];
		  result[1] = c.printConsole();
		  if (jppfClient != null) jppfClient.close();
		  return result;
		  }
		  else
		  {
			output("No input");
		    String[] result = new String[2];
			result[0] = input[0];
			result[1] = "Nothing";
			if (jppfClient != null) jppfClient.close();
			return result;
		  }
    }
    catch(Exception e)
    {
      e.printStackTrace();
	  output("catch");
	  if (jppfClient != null) jppfClient.close();
	  return null;
    }
  }

  /**
   * Perform the multiplication of 2 matrices with the specified size, for a specified number of times.
   * @param size - the size of the matrices.
   * @param iterations - the number of times the multiplication will be performed.
   * @param nbRows - number of rows of matrix a per task.
   * @throws Exception if an error is raised during the execution.
   */
  private Matrix perform(double[][] values1, double[][] values2, final int iterations, final int nbRows) throws Exception
  {
    try
    {
      // initialize the 2 matrices to multiply
      Matrix a = new Matrix(values1.length, values1[0].length, values1);
      //a.assignRandomValues();
      Matrix b = new Matrix(values2.length, values2[0].length, values2);
      //b.assignRandomValues();*)
      performSequentialMultiplication(a, b);
      long totalIterationTime = 0L;

      // determine whether an execution policy should be used
      ExecutionPolicy policy = null;
      String s = JPPFConfiguration.getProperties().getString("jppf.execution.policy");
      if (s != null)
      {
        PolicyParser.validatePolicy(s);
        policy = PolicyParser.parsePolicy(s);
      }
      // perform "iteration" times
	  Matrix c = null;
      for (int iter=0; iter<iterations; iter++)
      {
        c = performParallelMultiplication(a, b, nbRows, policy);
        //totalIterationTime += elapsed;
        //output("Iteration #" + (iter+1) + " performed in " + StringUtils.toStringDuration(elapsed));
      }
      //output("Average iteration time: " + StringUtils.toStringDuration(totalIterationTime / iterations));
      /*
			if (JPPFConfiguration.getProperties().getBoolean("jppf.management.enabled"))
			{
				JPPFStats stats = jppfClient.requestStatistics();
				output("End statistics :\n" + stats.toString());
			}
       */
	   return c;
    }
    catch(Exception e)
    {
      throw e;
    }
  }

  /**
   * Perform the sequential multiplication of 2 squares matrices of equal sizes.
   * @param a - the left-hand matrix.
   * @param b - the right-hand matrix.
   * @param nbRows - number of rows of matrix a per task.
   * @param policy - the execution policy to apply to the submitted job, may be null.
   * @return the elapsed time for the computation.
   * @throws Exception if an error is raised during the execution.
   */
  private Matrix performParallelMultiplication(final Matrix a, final Matrix b, final int nbRows, final ExecutionPolicy policy) throws Exception
  {
    long start = System.currentTimeMillis();
    //int size = a.getSize();
    // create a task for each row in matrix a
    JPPFJob job = new JPPFJob();
    job.setName("matrix sample " + (iterationsCount++));
    int remaining = a.getRows();
    for (int i=0; i<a.getRows(); i+= nbRows)
    {
      double[][] rows = null;
      if (remaining >= nbRows)
      {
        rows = new double[nbRows][];
        remaining -= nbRows;
      }
      else rows = new double[remaining][];
      for (int j=0; j<rows.length; j++) rows[j] = a.getRow(i + j);
      job.addTask(new ExtMatrixTask(rows));
    }
    // create a data provider to share matrix b among all tasks
    job.setDataProvider(new MemoryMapDataProvider());
    job.getDataProvider().setValue(MatrixTask.DATA_KEY, b);
    job.getSLA().setExecutionPolicy(policy);
    // submit the tasks for execution
    List<JPPFTask> results = jppfClient.submit(job);
    // initialize the resulting matrix
	int rRows = a.getRows();
	int rCols = b.getCols();
    Matrix c = new Matrix(rRows, rCols, new double[rRows][rCols]);
    // Get the matrix values from the tasks results
    int rowIdx = 0;
    for (JPPFTask matrixTask : results) {
      if (matrixTask.getException() != null) throw matrixTask.getException();
      double[][] rows = (double[][]) matrixTask.getResult();
      for (int j = 0; j < rows.length; j++) {
        for (int k = 0; k < rCols; k++) c.setValueAt(rowIdx + j, k, rows[j][k]);
      }
      rowIdx += rows.length;
    }
	output("Parallel");
	return c;
    //return System.currentTimeMillis() - start;
  }

  /**
   * Perform the sequential multiplication of 2 squares matrices of equal sizes.
   * @param a - the left-hand matrix.
   * @param b - the right-hand matrix.
   */
  private void performSequentialMultiplication(final Matrix a, final Matrix b)
  {
    long start = System.currentTimeMillis();
    Matrix c = a.multiply(b);
	output("Sequential");
	c.printConsole();
    long elapsed = System.currentTimeMillis() - start;
    output("Sequential computation performed in "+StringUtils.toStringDuration(elapsed));
  }

  /**
   * Print a message to the console and/or log file.
   * @param message - the message to print.
   */
  private void output(final String message)
  {
    System.out.println(message);
    log.info(message);
  }
}
