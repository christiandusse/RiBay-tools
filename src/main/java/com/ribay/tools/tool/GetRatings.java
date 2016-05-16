package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.ribay.tools.data.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by CD on 15.05.2016.
 */
@Component
public class GetRatings extends UpdateValue.Update<Article> implements CommandMarker {

    @Autowired
    private JobUtil util;

    private BufferedWriter writer;

    private Object lock = new Object();

    @PostConstruct
    private void init() throws Exception {
        writer = new BufferedWriter(new FileWriter("data/ratings", true));
    }

    @PreDestroy
    private void exit() throws Exception {
        writer.close();
    }

    @CliCommand(value = "getRatings", help = "Gets ratings and stores them into a text-file")
    public void start(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the bucket") final String bucket, //
                      @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                      @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.updateOnAll(bucket, idxStart, idxEnd, this, true);
    }

    @Override
    public Article apply(Article original) {
        try {
            double mediumRating = original.getRating();
            int nofVotes = original.getVotes();

            writeToFile(mediumRating, nofVotes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setModified(false); // does not modify object
        return original;
    }

    private void writeToFile(double mediumRating, int nofVotes) throws Exception {
        String line = String.format("%f,%d", mediumRating, nofVotes);

        synchronized (lock) {
            writer.write(line);
            writer.newLine();
        }
    }

}
