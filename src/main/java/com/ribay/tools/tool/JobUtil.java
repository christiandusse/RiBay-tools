package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.ribay.tools.db.MyRiakClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by CD on 04.05.2016.
 */
public class JobUtil {

    @Autowired
    private MyRiakClient client;

    public void updateOnAll(String bucket, int idxStart, int idxEnd, UpdateValue.Update<?> update) throws Exception {
        Namespace namespace = new Namespace(bucket);
        ListKeys lk = new ListKeys.Builder(namespace).build();
        ListKeys.Response lkResp = client.execute(lk);

        List<String> keys = new ArrayList<>();
        for (Location location : lkResp) {
            keys.add(location.getKeyAsString());
        }
        Collections.sort(keys);

        int idx = -1;
        try {
            for (idx = idxStart; (idx < keys.size()) && (idx < idxEnd); idx++) {
                System.out.println(idx);

                String key = keys.get(idx);
                Location location = new Location(namespace, key);

                UpdateValue command = new UpdateValue.Builder(location).withUpdate(update).build();
                client.execute(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ended with idx: " + idx);
        }
    }

}