package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.datatypes.*;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.util.BinaryValue;
import com.ribay.tools.data.Article;
import com.ribay.tools.db.MyRiakClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Created by CD on 17.05.2016.
 */
@Component
public class FillSearchBucket extends UpdateValue.Update<Article> implements CommandMarker {

    @Autowired
    private MyRiakClient client;

    @Autowired
    private JobUtil util;

    @CliCommand(value = "fillSearch", help = "Fills search article bucket from article bucket")
    public void start(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the bucket") final String bucket, //
                      @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                      @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.updateOnAll(bucket, idxStart, idxEnd, this, false);
    }

    @Override
    public Article apply(Article original) {

        try {
            Location location = new Location(new Namespace("ribay_crdt", "article_search"), original.getId());

            MapUpdate mu = new MapUpdate();
            if (original.getId() != null) {
                mu.update("id", new RegisterUpdate(original.getId()));
            }
            if (original.getTitle() != null) {
                mu.update("title", new RegisterUpdate(original.getTitle()));
            }
            if (original.getYear() != null) {
                mu.update("year", new RegisterUpdate(original.getYear()));
            }
            if (original.getActors() != null) {
                mu.update("actors", createFor(original.getActors()));
            }
            if (original.getActors() != null) {
                mu.update("genre", createFor(original.getGenre()));
            }
            mu.update("price", new RegisterUpdate("1349")); // TODO price
            if (original.getImageId() != null) {
                mu.update("image", new RegisterUpdate(original.getImageId()));
            }
            mu.update("votes", new CounterUpdate(original.getVotes()));
            mu.update("sumRatings", new CounterUpdate(Math.round(original.getVotes() * original.getRating())));
            mu.update("isMovie", new FlagUpdate(original.isMovie()));

            UpdateMap update = new UpdateMap.Builder(location, mu).build();
            client.execute(update);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setModified(false);
        return original;
    }

    private SetUpdate createFor(Collection<String> col) {
        SetUpdate result = new SetUpdate();
        for (String elem : col) {
            result.add(elem);
        }
        return result;
    }

}
