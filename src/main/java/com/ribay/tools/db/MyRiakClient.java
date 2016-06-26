package com.ribay.tools.db;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.core.FutureOperation;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.RiakNode;
import io.netty.channel.ThreadPerChannelEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MyRiakClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyRiakClient.class);

    private RiakClient client;

    public MyRiakClient() {
    }

    @PostConstruct
    private void init() throws Exception {
        LOGGER.info("init");

        String[] ips = {"134.100.11.158", "134.100.11.159", "134.100.11.160"};

        List<RiakNode> nodes = new ArrayList<>();
        for (String ip : ips) {
            RiakNode node = new RiakNode.Builder().withRemoteAddress(ip).withRemotePort(8087)
                    //.withMinConnections(20)
                    //.withMaxConnections(100)
                    //.withBlockOnMaxConnections(true)
                    // .withConnectionTimeout(60000)
                    .build();
            nodes.add(node);
        }

        RiakCluster cluster = new RiakCluster.Builder(nodes).build();

        // The cluster must be started to work, otherwise you will see errors
        cluster.start();

        client = new RiakClient(cluster);
    }

    @PreDestroy
    private void destroy() throws Exception {
        client.shutdown();
    }

    public <T, S> T execute(RiakCommand<T, S> command)
            throws ExecutionException, InterruptedException {
        LOGGER.debug("execute: " + command);

        // chain of responsibility
        return client.execute(command);
    }

    public <V, S> RiakFuture<V, S> execute(FutureOperation<V, ?, S> operation) throws ExecutionException, InterruptedException {
        // is this the new api??
        LOGGER.debug("execute: " + operation);

        // chain of responsibility
        return client.getRiakCluster().execute(operation);
    }

    public <T, S> RiakFuture<T, S> executeAsync(RiakCommand<T, S> command) {
        LOGGER.debug("executeAsync: " + command);

        // chain of responsibility
        return client.executeAsync(command);
    }

    public RiakClient getRiakClient() {
        return client;
    }

}
