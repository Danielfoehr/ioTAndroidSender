package com.openxc.openxcstarter;

import java.util.concurrent.RejectedExecutionHandler;

import java.util.concurrent.ThreadPoolExecutor;


public class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
 
	public static int rejectedCounter ;

    /**
     * Method gets called whenever a new request should be processed, but there are no available threaths in the pool
     * The post request gets  skipped
     * @param r
     * @param executor
     */
    @Override

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        //System.out.println(r.toString() + " is rejected because pool is full");
        rejectedCounter ++;

    }

 

}

