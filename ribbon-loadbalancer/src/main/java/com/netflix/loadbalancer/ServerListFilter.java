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
 * This interface allows for filtering the configured or dynamically obtained
 * List of candidate servers with desirable characteristics.
 * 
 * @author stonse
 * 
 * @param <T>
 *
 * 接口允许过滤配置或动态获得的候选列表服务器
 * ServerListFilter是DynamicServerListLoadBalancer用于过滤从ServerList实现返回的服务器的组件。
 *
 *      ZoneAffinityServerListFilter
 *          过滤掉所有的不和客户端在相同zone的服务，如果和客户端相同的zone不存在，才不过滤不同zone有服务。启用此配置使用：
 *          kxtx-oms.ribbon.EnableZoneAffinity=true
 *      ZonePreferenceServerListFilter
 *          ZoneAffinityServerListFilter的子类，但是比较的zone是发布环境里面的zone。过滤掉所有和客户端环境里的配置的zone的不同的服务，如果和客户端相同的zone不存在，才不进行过滤。
 *      ServerListSubsetFilter
 *          ZoneAffinityServerListFilter的子类，确保客户端仅看到由ServerList实现返回的整个服务器的固定子集。 它还可以定期用新服务器替代可用性差的子集中的服务器。要启用此过滤器：
 *          # 选择ServerList获取模式
 *          kxtx-oms.ribbon.NIWSServerListClassName=com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList
 *          # the server must register itself with Eureka server with VipAddress "myservice"
 *          kxtx-oms.ribbon.DeploymentContextBasedVipAddresses=myservice
 *          kxtx-oms.ribbon.NIWSServerListFilterClassName=com.netflix.loadbalancer.ServerListSubsetFilter
 *          # only show client 5 servers. default is 20.
 *          kxtx-oms.ribbon.ServerListSubsetFilter.size=5
 */
public interface ServerListFilter<T extends Server> {

    public List<T> getFilteredListOfServers(List<T> servers);

}
