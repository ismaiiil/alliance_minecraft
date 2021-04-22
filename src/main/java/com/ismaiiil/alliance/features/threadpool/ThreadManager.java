package com.ismaiiil.alliance.features.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ismaiiil.alliance.AlliancePlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {

    public static ThreadPoolExecutor pool;

    public static void init(int min, int max){
        pool = new ThreadPoolExecutor(min, max, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(max),
                new ThreadFactoryBuilder()
                        .setNameFormat("ALLIANCE POOL-%d")
                        .setDaemon(false)
                        .build());
    }

    public static void runMainThread(@NotNull Callable<?> method){
        AlliancePlugin.getInstance().getServer().getScheduler().callSyncMethod(AlliancePlugin.getInstance(),
                method);
    }

}
