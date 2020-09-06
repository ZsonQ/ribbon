package com.netflix.loadbalancer;

/**
 * strategy for {@link com.netflix.loadbalancer.DynamicServerListLoadBalancer} to use for different ways
 * of doing dynamic server list updates.
 *
 * @author David Liu
 *
 * 接口使用不同的方法来做动态更新服务器列表
 * 被DynamicServerListLoadBalancer用于动态的更新服务列表
 *
 *      PollingServerListUpdater
 *          默认的实现策略。此对象会启动一个定时线程池，定时执行更新策略
 *      EurekaNotificationServerListUpdater
 *          当收到缓存刷新的通知，会更新服务列表
 *
 */
public interface ServerListUpdater {

    /**
     * an interface for the updateAction that actually executes a server list update
     */
    public interface UpdateAction {
        void doUpdate();
    }


    /**
     * start the serverList updater with the given update action
     * This call should be idempotent.
     *
     * @param updateAction
     */
    void start(UpdateAction updateAction);

    /**
     * stop the serverList updater. This call should be idempotent
     */
    void stop();

    /**
     * @return the last update timestamp as a {@link java.util.Date} string
     */
    String getLastUpdate();

    /**
     * @return the number of ms that has elapsed since last update
     */
    long getDurationSinceLastUpdateMs();

    /**
     * @return the number of update cycles missed, if valid
     */
    int getNumberMissedCycles();

    /**
     * @return the number of threads used, if vaid
     */
    int getCoreThreads();
}
