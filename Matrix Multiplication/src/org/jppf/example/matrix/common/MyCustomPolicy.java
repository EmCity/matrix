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

package org.jppf.example.matrix.common;

import org.jppf.node.policy.*;
import org.jppf.utils.*;
import org.slf4j.*;
import java.util.List;
import org.jppf.client.*;
import org.jppf.example.matrix.common.MyCustomPolicy;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.MemoryMapDataProvider;

/**
 * A custom execution policy that checks a node has a specified minimum
 * amount of memory for each processing thread in the node.
 * @author Laurent Cohen
 */
public class MyCustomPolicy extends CustomPolicy
{
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(MyCustomPolicy.class);
  /**
   * Minimum available size per node processing thread, in bytes.
   */
  private long minimumSizePerThread = 0L;

  /**
   * Initialize this policy with the specified parameter.
   * @param minimumSizePerThreadStr the minimum available heap size per node processing thread.
   */
  public MyCustomPolicy()
  {
    super("default");
    this.minimumSizePerThread = Long.valueOf("2");
  }
  public MyCustomPolicy(final String minimumSizePerThreadStr)
  {
    super(minimumSizePerThreadStr);
    this.minimumSizePerThread = Long.valueOf(minimumSizePerThreadStr);
  }
  public MyCustomPolicy(final String[] args)
  {
    super(args[0]);
    this.minimumSizePerThread = 1;
	// Do something more here
  }


  /**
   * Determines whether this policy accepts the specified node.
   * @param info system information for the node on which the tasks will run if accepted.
   * @return true if the node is accepted, false otherwise.
   * @see org.jppf.node.policy.ExecutionPolicy#accepts(org.jppf.management.JPPFSystemInformation)
   */
  @Override
  public boolean accepts(final PropertiesCollection info)
  {
    // get the number of processing threads in the node
    long nbThreads = info.getProperties("jppf").getLong("processing.threads");
    // get the node's max heap size
	//output("nbThreads: " + nbThreads);
    long maxHeap = info.getProperties("runtime").getLong("maxMemory");
	//output("maxHeap: " + maxHeap);
    // we assume that 10 MB is taken by JPPF code and data
    maxHeap -= 10 * 1024 * 1024;
    // return true only if there is at least minimumSizePerThread of memory available for each thread
    //return maxHeap / nbThreads >= minimumSizePerThread;
	if(nbThreads<4) return false;
	return true;
  }
  
  private static void output(final String message)
  {
    System.out.println(message);
    log.info(message);
  }
	public static ExecutionPolicy initializeCustomPolicy(String name){
		output("Policy ARG = " + name);
		if(name.equals("Custom")){
			output("Custom policy used");
			String s;
			int n = JPPFConfiguration.getProperties().getInt("jppf.execution.policy.custom.numbargs");		
			if(n != 0) {
				String[] args = new String[n];
				for (int i=0; i<n; i++)
				{
					s = "jppf.execution.policy.custom.arg" + i;					
					args[i] = JPPFConfiguration.getProperties().getString(s);
					output(s + " = " + args[i]);
				}
				return new MyCustomPolicy(args);
			}
			else
				return new MyCustomPolicy();
		}
		else if(name.equals("AtLeast")) {
			return new AtLeast("processing.threads",JPPFConfiguration.getProperties().getInt("jppf.execution.policy.atleast.n",1));
		}
		else if(name.equals("MoreThan")) {	
			output(name  + "is being executed: SUCCES!");
			return new MoreThan("processing.threads ",JPPFConfiguration.getProperties().getInt("jppf.execution.policy.morethan.n",0));
		}
		else output("Unknown or unimplemented policy, using default!");
			return new MoreThan("processing.threads", 0);
	}
}

