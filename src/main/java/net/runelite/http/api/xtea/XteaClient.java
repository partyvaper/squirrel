// package com.osrsbox.squirrel;
package net.runelite.http.api.xtea;

// This should override this one:
// package net.runelite.http.api.xtea;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.List;

import net.runelite.http.api.RuneLiteAPI;
import okhttp3.OkHttpClient;

public class XteaClient
{
    private final OkHttpClient client;

    public XteaClient(OkHttpClient client) {
        this.client = client;
    }

    public void submit()
    {
        System.out.println("[MODIFIED XteaClient]: submit");
    }

    public List<XteaKey> get() throws IOException
    {
        System.out.println("[MODIFIED XteaClient]: get");

        InputStream in = getClass().getResourceAsStream("/xteas.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        // FileReader data = new FileReader("xteas.json");
        // BufferedReader bufferedReader = new BufferedReader(data);

        try {
            return RuneLiteAPI.GSON.fromJson(bufferedReader, new TypeToken<List<XteaKey>>() { }.getType());
        } catch (JsonParseException ex) {
            throw new IOException(ex);
        }
    }

    public XteaKey get(int region) throws IOException
    {
        System.out.println("[MODIFIED XteaClient]: get " + region);

        return (XteaKey) get();
    }
}
