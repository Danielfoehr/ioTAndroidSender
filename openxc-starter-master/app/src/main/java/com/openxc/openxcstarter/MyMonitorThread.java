package com.openxc.openxcstarter;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * Class to monitor the status of the threath pool and the application -> implements the runnable interface and therefore runs independently in a different threath.
 *
 */
public class MyMonitorThread implements Runnable

{

    private ThreadPoolExecutor executor;

 

    private int seconds;

 

    private boolean run=true;


    /**
     *
     * @param executor
     * @param delay    -> to define after which amount of time the status should be monitored again
     */
    public MyMonitorThread(ThreadPoolExecutor executor, int delay)

    {

        this.executor = executor;

        this.seconds=delay;

    }

 

    public void shutdown(){

        this.run=false;

    }


    /**
     * Method which writes the status into the Logcat as well as into the Android notification for the user
     */
    @Override

    public void run()

    {

        while(run){
                System.out.println(

                        String.format("[monitor] [Current Pool Size: %d/Core Pool Size(minimum threats always in pool): %d] Active: %d, Completed: %d, Task: %d, Rejected: %d, isShutdown: %s, isTerminated: %s, Successfull Requests: %d, Failed Requests: %d, Total device meesages recieved: %d",

                                this.executor.getPoolSize(),

                                this.executor.getCorePoolSize(),

                                this.executor.getActiveCount(),

                                this.executor.getCompletedTaskCount(),

                                this.executor.getTaskCount(),

                                RejectedExecutionHandlerImpl.rejectedCounter,

                                this.executor.isShutdown(),

                                this.executor.isTerminated(),

                                SimpleThreathPool.getSuccessCounter(),

                                SimpleThreathPool.getFailureCounter(),

                                SimpleThreathPool.getDeviceMessagesRecieved()


                        ));
            if( StarterActivity.getNotificationBuilder() != null && StarterActivity.getEnabled() == true) {
                StarterActivity.getNotificationBuilder().setContentText("Successfull: " + SimpleThreathPool.getSuccessCounter() + " Failed: " + SimpleThreathPool.getFailureCounter() + " Active: " + this.executor.getActiveCount());
                StarterActivity.getmNotificationManager().notify(StarterActivity.getUniqueIdentificationId(), StarterActivity.getNotificationBuilder().build());
            };
                try {

                    Thread.sleep(seconds*1000);

                } catch (InterruptedException e) {

                    e.printStackTrace();

                }

        }

 

    }

}
