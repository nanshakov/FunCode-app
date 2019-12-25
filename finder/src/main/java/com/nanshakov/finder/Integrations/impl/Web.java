package com.nanshakov.finder.Integrations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.finder.Dto.GoogleResults;
import com.nanshakov.finder.Dto.Post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;

import lombok.SneakyThrows;

@Service
public class Web extends BaseIntegrationImpl {

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void start() {

    }

    @Override
    public Post getNext() {
        return null;
    }

    @SneakyThrows
    @PostConstruct
    void postConstruct() {
        String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
        String search = "stackoverflow";
        String charset = "UTF-8";

        URL url = new URL(google + URLEncoder.encode(search, charset));
        Reader reader = new InputStreamReader(url.openStream(), charset);
        GoogleResults results = mapper.readValue(reader, GoogleResults.class);

        // Show title and URL of 1st result.
        System.out.println(results.getResponseData().getResults().get(0).getTitle());
        System.out.println(results.getResponseData().getResults().get(0).getUrl());
    }
}
