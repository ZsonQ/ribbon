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
 * Interface that defines how we "ping" a server to check if its alive
 * @author stonse
 *
 * 心跳检测的顶级接口
 *
 * 子类实现类
 *      NIWSDiscoveryPing ，不执行真正的ping。如果Discovery Client认为是在线，则程序认为本次心跳成功，服务活着
 *      PingUrl ，使用HttpClient调用服务的一个URL，如果调用成功，则认为本次心跳成功，表示此服务活着
 *      NoOpPing ，永远返回true，即认为服务永远活着
 *      DummyPing ，默认实现，默认返回true，即认为服务永远活着
 *
 *
 */
public interface IPing {
    
    /**
     * Checks whether the given <code>Server</code> is "alive" i.e. should be
     * considered a candidate while loadbalancing
     * 
     */
    public boolean isAlive(Server server);
}
