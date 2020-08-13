package com.cindi;

public class CindiClientFactory {
    private String url;

    private CindiClientFactory(String url)
    {
        this.url = url;
    }

    public static CindiClientFactory newInstance(String url) {
        return new CindiClientFactory(url);
    }

    public static CindiClientFactory newInstance() {
        return new CindiClientFactory(null);
    }

}
