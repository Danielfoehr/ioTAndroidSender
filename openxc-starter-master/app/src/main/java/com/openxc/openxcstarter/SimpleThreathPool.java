package com.openxc.openxcstarter;

import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple threath pool to perform the http post requests to iotServicesServlet.
 * Performance and stability reasons
 */
public class SimpleThreathPool {

	private static int successCounter = 0;
	private static int failureCounter = 0;
	private static ThreadPoolExecutor executorPool;
	private static int deviceMessagesRecieved;
	private static MyMonitorThread monitor;

	public static int getSuccessCounter() {

		return successCounter;
	}

	public static void setSuccessCounter(int successCounter) {
		SimpleThreathPool.successCounter = successCounter;
	}

	public static int getFailureCounter() {

		return failureCounter;
	}

	public static void setFailureCounter(int failureCounter) {
		SimpleThreathPool.failureCounter = failureCounter;
	}

	public static int getDeviceMessagesRecieved() {
		return deviceMessagesRecieved;
	}

	/**
	 * Method to initialize the threath pool - only called once on startup of the activity
	 */
	public SimpleThreathPool() {


		//RejectedExecutionHandler implementation

		RejectedExecutionHandlerImpl rejectionHandler = new RejectedExecutionHandlerImpl();

		//Get the ThreadFactory implementation to use

		ThreadFactory threadFactory = Executors.defaultThreadFactory();

		//creating the ThreadPoolExecutor
		//initial pool size as 2, maximum pool size to 4, keep alive time : 1 second  and work queue size as 2 ArrayBlockingQueue<Runnable>(2)))
		//Keep alive time: when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
		 executorPool = new ThreadPoolExecutor(4, 4, 50, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);

		//start the monitoring thread -> updates every 3 seconds

		 monitor = new MyMonitorThread(executorPool, 3);

		Thread monitorThread = new Thread(monitor);

		monitorThread.start();
	}

	/**
	 * Method gets called from StarterActivity from listener object to hand over the data received from the car
	 * @param Speed
	 * @param fuelConsumed
	 * @param odometer
	 * @param acceleration
	 * @param gear
	 * @param longitude
	 * @param latitude
	 * @param tripId
	 * @param messagesRecieved
	 * @throws InterruptedException
	 */
	public static void startMultiThreathing(com.openxc.measurements.Measurement Speed, com.openxc.measurements.Measurement fuelConsumed, com.openxc.measurements.Measurement odometer, com.openxc.measurements.Measurement acceleration, com.openxc.measurements.Measurement gear, com.openxc.measurements.Measurement longitude,com.openxc.measurements.Measurement latitude, String tripId, int messagesRecieved)
		 throws InterruptedException{

				deviceMessagesRecieved = messagesRecieved;
		        //submit work to the thread pool
		
				executorPool.execute(new WorkerThread(Speed, fuelConsumed, odometer, acceleration, gear, longitude,latitude, tripId,Integer.toString(deviceMessagesRecieved)));

		
		    }

}

