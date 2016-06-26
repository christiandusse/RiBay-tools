package com.ribay.tools.tool;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.Converter;
import com.ribay.tools.data.Article;
import com.ribay.tools.data.Release;
import com.ribay.tools.data.Tweet;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by CD on 20.05.2016.
 */
@Component
public class ListGenre implements CommandMarker {

    @CliCommand(value = "listGenre", help = "Lists all distinct genre")
    public Set<String> start() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                JsonNode node = jp.getCodec().readTree(jp);
                JsonNode nested = node.findValue("$date");
                return (nested == null) ? null : new Date(nested.longValue());
            }
        });
        mapper.registerModule(module);
        mapper.setConfig(mapper.getDeserializationConfig() //
                .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY) //
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT) //
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT) //
        );

        Set<String> result = new HashSet<>();

        InputStream articleStream = getClass().getClassLoader().getResourceAsStream("movies.json");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(articleStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                MoviesJSONEntry jsonEntry = mapper.readValue(line, MoviesJSONEntry.class);

                List<String> genre = jsonEntry.getGenre();
                result.addAll(genre);
            }
        }

        return result;
    }

    public static class MoviesJSONEntry {
        private String _id;
        private List<String> actors;
        private List<String> genre;
        private boolean movie;
        private String plot;
        private float rating;
        private List<Release> releases;
        private String runtime;
        private String title;
        private int votes;
        private String year;
        private String comment;
        private List<Tweet> tweets;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public List<String> getActors() {
            return actors;
        }

        public void setActors(List<String> actors) {
            this.actors = actors;
        }

        public List<String> getGenre() {
            return genre;
        }

        public void setGenre(List<String> genre) {
            this.genre = genre;
        }

        public boolean isMovie() {
            return movie;
        }

        public void setMovie(boolean movie) {
            this.movie = movie;
        }

        public String getPlot() {
            return plot;
        }

        public void setPlot(String plot) {
            this.plot = plot;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public List<Release> getReleases() {
            return releases;
        }

        public void setReleases(List<Release> releases) {
            this.releases = releases;
        }

        public String getRuntime() {
            return runtime;
        }

        public void setRuntime(String runtime) {
            this.runtime = runtime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public List<Tweet> getTweets() {
            return tweets;
        }

        public void setTweets(List<Tweet> tweets) {
            this.tweets = tweets;
        }
    }

}
