package com.ribay.tools.tool;

import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.ribay.tools.data.Article;
import com.ribay.tools.db.MyRiakClient;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by CD on 04.05.2016.
 */
@Component
public class DownloadAndStoreImages extends UpdateValue.Update<Article> implements CommandMarker {

    @Autowired
    private MyRiakClient client;

    @Autowired
    private JobUtil util;

    private String targetBucket = null;

    @CliCommand(value = "loadImages", help = "Downloads images from the specified urls and stores them into the db")
    public void start(@CliOption(key = {"bucket"}, mandatory = true, help = "The name of the bucket") final String bucket, //
                      @CliOption(key = {"start"}, mandatory = true, help = "The index where to start") final int idxStart, //
                      @CliOption(key = {"end"}, mandatory = true, help = "The index where to end") final int idxEnd, //
                      @CliOption(key = {"targetBucket"}, mandatory = true, help = "The name of the bucket to store the images to") final String targetBucket) throws Exception {

        this.targetBucket = targetBucket;

        util.updateOnAll(bucket, idxStart, idxEnd, this);
    }

    @Override
    public Article apply(Article article) {
        if (article.getImageId() != null) {
            if (article.getImageId().startsWith("http://")) {
                String imageURL = article.getImageId();
                try {
                    byte[] imageRaw = getImageRaw(imageURL);
                    String mimeType = parseMimeType(imageURL);

                    RiakObject toStore = new RiakObject();
                    toStore.setValue(BinaryValue.create(imageRaw));
                    toStore.setContentType(mimeType);

                    StoreValue command = new StoreValue.Builder(toStore).withNamespace(new Namespace(targetBucket)).build();
                    StoreValue.Response response = client.execute(command);

                    String imageId = response.getGeneratedKey().toString();
                    article.setImageId(imageId);
                    setModified(true);
                } catch (Exception e) {
                    System.err.println("Error while loading and storing image");
                    e.printStackTrace();
                    setModified(false);
                }
            } else {
                System.out.println("image already loaded into db");
                setModified(false);
            }
        } else {
            System.out.println("article has no image data");
            setModified(false);
        }
        return article;
    }

    private byte[] getImageRaw(String url) throws Exception {
        URL u = new URL(url);
        try (InputStream is = u.openStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    private String parseMimeType(String imageURL) {
        if (imageURL.toLowerCase().endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (imageURL.toLowerCase().endsWith(".jpg") || imageURL.toLowerCase().endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (imageURL.toLowerCase().endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

}
