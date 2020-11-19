package com.gacrnd.gcs.customnetwork.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public class ThreadManager {

    private static ThreadManager mInstance = new ThreadManager();
    private LinkedBlockingDeque<Runnable> mQueue = new LinkedBlockingDeque<>();
    private Executor mExecutor;

    public static ThreadManager getInstance() {
        return mInstance;
    }

    private ThreadManager() {
        mExecutor = new ThreadPoolExecutor(3, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), new CustomThreadFactory(), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                addTask(r);
            }
        });
        mExecutor.execute(mRunnable);
    }

    public void addTask(Runnable r) {
        if (r == null) {
            return;
        }
        mQueue.add(r);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable runnable = mQueue.take();
                    mExecutor.execute(runnable);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class CustomThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }
}
