package com.kt.restful.scheduler;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClientThread extends Thread {
    
	private static Logger logger = LogManager.getLogger(ClientThread.class);
	
    private boolean done = false;
    private Timer timer;
    private TimerTask task;
    
    public ClientThread (TimerTask task) {
        this.task = task;
    }
    
    public void quit() {
        this.done = true;
        this.interrupt();
    }
    
    public boolean finishing() {
        return (done || Thread.interrupted());
    }

    @Override
    public void run() {
        super.run();
        
        logger.debug("run");
        
        timer = new Timer();
        
        Calendar now = Calendar.getInstance();
//        timer.scheduleAtFixedRate(task, now.getTime(), 1000);
        timer.scheduleAtFixedRate(task, now.getTime(), 1000);
        while (!finishing()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        timer.cancel();
    }
}

