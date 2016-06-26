package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.datatypes.*;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.ribay.tools.db.MyRiakClient;
import com.ribay.tools.util.RibayConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * Created by CD on 25.06.2016.
 */
@Component
public class ChangeDynamicData implements CommandMarker {

    @Autowired
    private MyRiakClient client;

    @Autowired
    private JobUtil util;

    // resetDynamicData --bucket articles --start 170000 --end 1000000

    @CliCommand(value = "resetDynamicData", help = "Resets the dynamic data of articles")
    public void start1(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the article (source) bucket containing the article ids") final String bucket, //
                       @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                       @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.forAllKeys(bucket, idxStart, idxEnd, new JobUtil.OperationForKey() {
            @Override
            public void perform(String key) {
                resetDynamicData(key);
            }
        });
    }

    // fillInitialDynamicData --bucket articles --start 0 --end 1000000

    @CliCommand(value = "fillInitialDynamicData", help = "Fill the initial dynamic data of articles")
    public void start2(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the article (source) bucket containing the article ids") final String bucket, //
                       @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                       @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.forAllKeys(bucket, idxStart, idxEnd, new JobUtil.OperationForKey() {
            @Override
            public void perform(String key) {
                fillInitialDynamicData(key);
            }
        });
    }

    private void resetDynamicData(String key) {
        try {
            final Location location1 = new Location(new Namespace("ribay_crdt", "article_dynamic"), key);
            DeleteValue operation1 = new DeleteValue.Builder(location1).build();
            RiakFuture<?, ?> future1 = client.executeAsync(operation1);

            final Location location2 = new Location(new Namespace("ribay_crdt", "article_search"), key);
            FetchMap fetch2 = new FetchMap.Builder(location2).build();
            FetchMap.Response fetch2Resp = client.execute(fetch2);
            if (fetch2Resp.hasContext()) {
                MapUpdate mapUpdate = new MapUpdate() //
                        .removeRegister("price") //
                        .removeCounter("stock") //
                        .removeCounter("countRatings") //
                        .removeCounter("sumRatings"); //
                UpdateMap operation2 = new UpdateMap.Builder(location2, mapUpdate).withContext(fetch2Resp.getContext()).build();
                RiakFuture<?, ?> future2 = client.executeAsync(operation2);

                // future1.await();
                // future2.await();
            } else {
                // future1.await();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillInitialDynamicData(String key) {
        try {
            final int initialPrice = 1995; // 19.95€
            final int initialStock = 20;
            final int initialCountRatings = 0;
            final int initialSumRatings = 0;

            final Location location1 = new Location(new Namespace("ribay_crdt", "article_dynamic"), key);
            MapUpdate mapUpdate1 = new MapUpdate() //
                    .update("price", new RegisterUpdate(BigInteger.valueOf(initialPrice).toByteArray())) // integer -> byte array
                    .update("stock", new CounterUpdate(initialStock)) //
                    .update("countRatings", new CounterUpdate(initialCountRatings)) // both 0 st start
                    .update("sumRatings", new CounterUpdate(initialSumRatings)); // both 0 st start
            UpdateMap operation1 = new UpdateMap.Builder(location1, mapUpdate1).build();
            RiakFuture<?, ?> future1 = client.executeAsync(operation1);

            final Location location2 = new Location(new Namespace("ribay_crdt", "article_search"), key);
            MapUpdate mapUpdate2 = new MapUpdate() //
                    .update("price", new RegisterUpdate(String.format(RibayConstants.NUMBERFORMAT_PRICE_SEARCH, initialPrice))) // solr needs string so that we can filter
                    .update("stock", new CounterUpdate(initialStock)) //
                    .update("countRatings", new CounterUpdate(initialCountRatings)) // both 0 st start
                    .update("sumRatings", new CounterUpdate(initialSumRatings)); // both 0 st start
            UpdateMap operation2 = new UpdateMap.Builder(location2, mapUpdate2).build();
            RiakFuture<?, ?> future2 = client.executeAsync(operation2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
