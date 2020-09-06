package com.netflix.niws.loadbalancer;

import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.client.config.IClientConfigKey.Keys;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.DummyPing;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.PollingServerListUpdater;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 *
 * Ribbon主要组件：
 *      IRule，上面讲过
 *      IPing，接口定义了我们如何“ping”服务器检查是否活着
 *      ServerList，定义了获取服务器的列表接口，存储服务列表
 *      ServerListFilter，接口允许过滤配置或动态获得的候选列表服务器
 *      ServerListUpdater，接口使用不同的方法来做动态更新服务器列表
 *      IClientConfig，定义了各种api所使用的客户端配置，用来初始化ribbon客户端和负载均衡器，默认实现是DefaultClientConfigImpl
 *      ILoadBalancer，接口定义了各种软负载，动态更新一组服务列表及根据指定算法从现有服务器列表中选择一个服务
 *
 *  ribbon的默认配置类
 *      IClientConfig	DefaultClientConfigImpl
 *      IRule	ZoneAvoidanceRule
 *      IPing	DummyPing
 *      ServerList	ConfigurationBasedServerList
 *      ServerListFilter	ZonePreferenceServerListFilter
 *      ILoadBalancer	ZoneAwareLoadBalancer
 *      ServerListUpdater	PollingServerListUpdater
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( {DiscoveryManager.class, DiscoveryClient.class} )
@PowerMockIgnore({"javax.management.*", "com.sun.jersey.*", "com.sun.*", "org.apache.*", "weblogic.*", "com.netflix.config.*", "com.sun.jndi.dns.*",
    "javax.naming.*", "com.netflix.logging.*", "javax.ws.*", "com.google.*"})
public class LBBuilderTest {
    
    static Server expected = new Server("www.example.com", 8001);
    
    static class NiwsClientConfig extends DefaultClientConfigImpl {
        public NiwsClientConfig() {
            super();
        }

        @Override
        public String getNameSpace() {
            return "niws.client";
        }
    }
    
    @Before
    public void setupMock(){
        List<InstanceInfo> instances = LoadBalancerTestUtils.getDummyInstanceInfo("dummy", expected.getHost(), "127.0.0.1", expected.getPort());
        PowerMock.mockStatic(DiscoveryManager.class);
        PowerMock.mockStatic(DiscoveryClient.class);

        DiscoveryClient mockedDiscoveryClient = LoadBalancerTestUtils.mockDiscoveryClient();
        DiscoveryManager mockedDiscoveryManager = createMock(DiscoveryManager.class);

        expect(DiscoveryManager.getInstance()).andReturn(mockedDiscoveryManager).anyTimes();
        expect(mockedDiscoveryManager.getDiscoveryClient()).andReturn(mockedDiscoveryClient).anyTimes();

        expect(mockedDiscoveryClient.getInstancesByVipAddress("dummy:7001", false, null)).andReturn(instances).anyTimes();

        replay(DiscoveryManager.class);
        replay(DiscoveryClient.class);
        replay(mockedDiscoveryManager);
        replay(mockedDiscoveryClient);
    }

    @Test
    public void testBuildWithDiscoveryEnabledNIWSServerList() {
        IRule rule = new AvailabilityFilteringRule();
        ServerList<DiscoveryEnabledServer> list = new DiscoveryEnabledNIWSServerList("dummy:7001");
        ServerListFilter<DiscoveryEnabledServer> filter = new ZoneAffinityServerListFilter<>();
        ZoneAwareLoadBalancer<DiscoveryEnabledServer> lb = LoadBalancerBuilder.<DiscoveryEnabledServer>newBuilder()
                .withDynamicServerList(list)
                .withRule(rule)
                .withServerListFilter(filter)
                .buildDynamicServerListLoadBalancer();
        assertNotNull(lb);
        assertEquals(Lists.newArrayList(expected), lb.getAllServers());
        assertSame(filter, lb.getFilter());
        assertSame(list, lb.getServerListImpl());
        Server server = lb.chooseServer();
        // make sure load balancer does not recreate the server instance
        assertTrue(server instanceof DiscoveryEnabledServer);
    }

    @Test
    public void testBuildWithDiscoveryEnabledNIWSServerListAndUpdater() {
        IRule rule = new AvailabilityFilteringRule();
        ServerList<DiscoveryEnabledServer> list = new DiscoveryEnabledNIWSServerList("dummy:7001");
        ServerListFilter<DiscoveryEnabledServer> filter = new ZoneAffinityServerListFilter<>();
        ServerListUpdater updater = new PollingServerListUpdater();
        ZoneAwareLoadBalancer<DiscoveryEnabledServer> lb = LoadBalancerBuilder.<DiscoveryEnabledServer>newBuilder()
                .withDynamicServerList(list)
                .withRule(rule)
                .withServerListFilter(filter)
                .withServerListUpdater(updater)
                .buildDynamicServerListLoadBalancerWithUpdater();
        assertNotNull(lb);
        assertEquals(Lists.newArrayList(expected), lb.getAllServers());
        assertSame(filter, lb.getFilter());
        assertSame(list, lb.getServerListImpl());
        assertSame(updater, lb.getServerListUpdater());
        Server server = lb.chooseServer();
        // make sure load balancer does not recreate the server instance
        assertTrue(server instanceof DiscoveryEnabledServer);
    }

    @Test
    public void testBuildWithArchaiusProperties() {
        Configuration config = ConfigurationManager.getConfigInstance();
        config.setProperty("client1.niws.client." + Keys.DeploymentContextBasedVipAddresses, "dummy:7001");
        config.setProperty("client1.niws.client." + Keys.InitializeNFLoadBalancer, "true");
        config.setProperty("client1.niws.client." + Keys.NFLoadBalancerClassName, DynamicServerListLoadBalancer.class.getName());
        config.setProperty("client1.niws.client." + Keys.NFLoadBalancerRuleClassName, RoundRobinRule.class.getName());
        config.setProperty("client1.niws.client." + Keys.NIWSServerListClassName, DiscoveryEnabledNIWSServerList.class.getName());
        config.setProperty("client1.niws.client." + Keys.NIWSServerListFilterClassName, ZoneAffinityServerListFilter.class.getName());
        config.setProperty("client1.niws.client." + Keys.ServerListUpdaterClassName, PollingServerListUpdater.class.getName());
        IClientConfig clientConfig = IClientConfig.Builder.newBuilder(NiwsClientConfig.class, "client1").build();
        ILoadBalancer lb = LoadBalancerBuilder.newBuilder().withClientConfig(clientConfig).buildLoadBalancerFromConfigWithReflection();
        assertNotNull(lb);
        assertEquals(DynamicServerListLoadBalancer.class.getName(), lb.getClass().getName());
        DynamicServerListLoadBalancer<Server> dynamicLB = (DynamicServerListLoadBalancer<Server>) lb;
        assertTrue(dynamicLB.getServerListUpdater() instanceof PollingServerListUpdater);
        assertTrue(dynamicLB.getFilter() instanceof ZoneAffinityServerListFilter);
        assertTrue(dynamicLB.getRule() instanceof RoundRobinRule);
        assertTrue(dynamicLB.getPing() instanceof DummyPing);
        assertEquals(Lists.newArrayList(expected), lb.getAllServers());
    }

    @Test
    public void testBuildStaticServerListLoadBalancer() {
        List<Server> list = Lists.newArrayList(expected, expected);
        IRule rule = new AvailabilityFilteringRule();
        IClientConfig clientConfig = IClientConfig.Builder.newBuilder()
                .withDefaultValues()
                .withMaxAutoRetriesNextServer(3).build();

        assertEquals(3, clientConfig.get(Keys.MaxAutoRetriesNextServer).intValue());
        BaseLoadBalancer lb = LoadBalancerBuilder.newBuilder()
                .withRule(rule)
                .buildFixedServerListLoadBalancer(list);
        assertEquals(list, lb.getAllServers());
        assertSame(rule, lb.getRule());
    }
}
