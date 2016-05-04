package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ribay.tools.data.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;

/**
 * Created by CD on 03.05.2016.
 */
@Component
public class FetchAndStoreImageURLs extends UpdateValue.Update<Article> implements CommandMarker {

    @Autowired
    private JobUtil util;

    @CliCommand(value = "fetchAndStoreImageURLs", help = "Fetches image urls for all movies and stores them as an additional property")
    public void start(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the bucket") final String bucket, //
                      @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                      @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd) throws Exception {

        util.updateOnAll(bucket, idxStart, idxEnd, this);
    }

    @Override
    public Article apply(Article article) {
        if ((article.getImageId() == null) || article.getImageId().startsWith("{")) { // only fill if not set already
            String imageURL = getImageURL(article);
            if (imageURL != null && !imageURL.equals("N/A")) { // only if found image from omdb
                // store imageUrl as imageId first so that we can later copy the image to the db
                article.setImageId(imageURL);
                setModified(true);
            } else {
                System.out.println("did not find image from omdb");
                setModified(false);
            }
        } else {
            System.out.println("imageId or imageUrl already set");
            setModified(false);
        }
        return article;
    }

    private String getImageURL(Article article) {
        try {
            String title = article.getTitle();
            String titleEncoded = URLEncoder.encode(title, "UTF-8");
            RestTemplate restTemplate = new RestTemplate();
            String value = restTemplate.getForObject("http://www.omdbapi.com/?t=" + titleEncoded + "&y=&plot=short&r=json", String.class);

            OMDBResult result = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(value, OMDBResult.class);
            return result.getPoster();
        } catch (Exception e) {
            return null;
        }
    }

    public static class OMDBResult {
        @JsonProperty("Poster")
        private String poster;

        public OMDBResult() {
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }
    }

}
