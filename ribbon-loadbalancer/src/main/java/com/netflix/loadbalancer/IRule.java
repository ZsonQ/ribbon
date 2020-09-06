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

/**
 * Interface that defines a "Rule" for a LoadBalancer. A Rule can be thought of
 * as a Strategy for loadbalacing. Well known loadbalancing strategies include
 * Round Robin, Response Time based etc.
 * 
 * @author stonse
 *
 *
 * 负载均衡策略 顶级接口
 *
 * 子类实现类
 *      RoundRobinRule，轮训策略，默认策略
 *      RandomRule，随机，使用Random对象从服务列表中随机选择一个服务
 *      RetryRule，轮询 + 重试
 *      WeightedResponseTimeRule，优先选择响应时间快，此策略会根据平均响应时间计算所有服务的权重，响应时间越快，服务权重越重、被选中的概率越高。此类有个DynamicServerWeightTask的定时任务，默认情况下每隔30秒会计算一次各个服务实例的权重。刚启动时，如果统计信息不足，则使用RoundRobinRule策略，等统计信息足够，会切换回来
 *      AvailabilityFilteringRule，可用性过滤，会先过滤掉以下服务：由于多次访问故障而断路器处于打开的服务、并发的连接数量超过阈值，然后对剩余的服务列表按照RoundRobinRule策略进行访问
 *      BestAvailableRule，优先选择并发请求最小的，刚启动时吗，如果统计信息不足，则使用RoundRobinRule策略，等统计信息足够，才会切换回来
 *      ZoneAvoidanceRule，可以实现避免可能访问失效的区域(zone)
 *
 */
public interface IRule{
    /*
     * choose one alive server from lb.allServers or
     * lb.upServers according to key
     * 
     * @return choosen Server object. NULL is returned if none
     *  server is available 
     */

    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}
