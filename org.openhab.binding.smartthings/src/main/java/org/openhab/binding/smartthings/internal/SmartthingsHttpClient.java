/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * HTTP client used to send messages to smartthings hub
 *
 * @author Bob Raker
 *
 */
public class SmartthingsHttpClient {

    private final Logger log = LoggerFactory.getLogger(SmartthingsHttpClient.class);

    private HttpClient httpClient;
    private Gson gson;

    private String smartthingsIp;
    private int smartthingsPort;

    public SmartthingsHttpClient(String smartthingsIp, int smartthingsPort) throws Exception {
        // Save the Smartthing IP and port
        this.smartthingsIp = smartthingsIp;
        this.smartthingsPort = smartthingsPort;

        // Setup the jetty client for use in sending data to the Smartthings hub
        httpClient = new HttpClient();
        httpClient.start();

        // Get a Gson instance
        gson = new Gson();
    }

    public Map<String, Object> sendDeviceCommand(String path, String data)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = httpClient.newRequest(smartthingsIp, smartthingsPort).timeout(3, TimeUnit.SECONDS)
                .path(path).method(HttpMethod.POST).content(new StringContentProvider(data), "application/json").send();

        Map<String, Object> result = null;

        int status = response.getStatus();
        if (status == 200) {
            String responseStr = response.getContentAsString();
            if (response != null && responseStr.length() > 0) {
                result = new HashMap<String, Object>();
                result = gson.fromJson(responseStr, result.getClass());
            }
        } else {
            log.info("Sent message \"{}\" with path \"{}\" to the Smartthings hub, recieved HTTP status {}", data, path,
                    status);
        }

        return result;
    }

    public void stopHttpClient() {
        try {
            httpClient.stop();
            log.info("HTTP Client stopped");
        } catch (Exception e) {
            log.error("HTTP client failed to stop because: {}", e.getMessage());
        }
    }
}
