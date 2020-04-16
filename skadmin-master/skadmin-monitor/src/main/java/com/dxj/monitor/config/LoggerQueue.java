package com.dxj.monitor.config;

import com.dxj.monitor.domain.entity.LogMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 创建一个阻塞队列，作为日志系统输出的日志的一个临时载体
 *
 * @author https://cloud.tencent.com/developer/article/1096792
 * @date 2019-04-24
 */
class LoggerQueue {

    /**
     * 队列大小
     */
    private static final int QUEUE_MAX_SIZE = 10000;

    private static LoggerQueue alarmMessageQueue = new LoggerQueue();
    /**
     * 阻塞队列
     */
    private BlockingQueue blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private LoggerQueue() {
    }

    static LoggerQueue getInstance() {
        return alarmMessageQueue;
    }

    /**
     * 消息入队
     *
     * @param log
     * @return
     */
    void push(LogMessage log) {

        //队列满了就抛出异常，不阻塞
        blockingQueue.add(log);
    }

    /**
     * 消息出队
     *
     * @return
     */
    LogMessage poll() {
        LogMessage result = null;
        try {
            result = (LogMessage) this.blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
