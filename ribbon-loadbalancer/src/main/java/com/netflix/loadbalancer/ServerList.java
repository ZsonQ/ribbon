/*
*
* Copyright 2013 Netflix, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package com.netflix.loadbalancer;

import java.util.List;

/**
 * Interface that defines the methods sed to obtain the List of Servers
 * @author stonse
 *
 * @param <T>
 *
 * 定义了获取服务器的列表接口，存储服务列表
 * 服务列表分为静态和动态。如果是动态的，后台有个线程会定时刷新和过滤服务列表
 *
 * 服务列表分为静态和动态。如果是动态的，后台有个线程会定时刷新和过滤服务列表
 *      ConfigurationBasedServerList
 *          从配置文件中获取所有服务列表，比如：
 *          kxtx-oms.ribbon.listOfServers=www.microsoft.com:80,www.yahoo.com:80,www.google.com:80
 *      DiscoveryEnabledNIWSServerList
 *          从Eureka Client中获取服务列表。此值必须通过属性中的VipAddress来标识服务器集群。DynamicServerListLoadBalancer（之前也提过）会调用此对象动态获取服务列表。
 *
 */
public interface ServerList<T extends Server> {

    public List<T> getInitialListOfServers();
    
    /**
     * Return updated list of servers. This is called say every 30 secs
     * (configurable) by the Loadbalancer's Ping cycle
     *
     *
     * 更新服务列表 默认30s
     * 
     */
    public List<T> getUpdatedListOfServers();   

}
