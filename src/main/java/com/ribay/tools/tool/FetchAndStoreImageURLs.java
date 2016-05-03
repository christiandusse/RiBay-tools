package com.ribay.tools.tool;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

/**
 * Created by CD on 03.05.2016.
 */
@Component
public class FetchAndStoreImageURLs implements CommandMarker {

    @CliCommand(value = "fetchAndStoreImageURLs", help = "Fetches image urls for all movies and stores them as an additional property")
    public void start() {

    }

}
