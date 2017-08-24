package com.github.star45.springboot;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.logging.Logger;

/**
 * Created by xulikang on 2017/5/17.
 */
public class DiamondHolderListener implements ApplicationListener {
    private static Thread holdThread;
    private static Boolean running = Boolean.FALSE;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationPreparedEvent) {
            if (running == Boolean.FALSE)
                running = Boolean.TRUE;
            if (holdThread == null) {
                holdThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.err.println(Thread.currentThread().getName());
                        while (running && !Thread.currentThread().isInterrupted()) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }, "Diamond-Holder");
                holdThread.setDaemon(false);
                holdThread.start();
            }
        }
        if (event instanceof ContextClosedEvent) {
            running = Boolean.FALSE;
            if (null != holdThread) {
                holdThread.interrupt();
                holdThread = null;
            }
        }
    }
}
