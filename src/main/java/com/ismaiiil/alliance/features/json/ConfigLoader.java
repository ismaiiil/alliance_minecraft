package com.ismaiiil.alliance.features.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;


public class ConfigLoader {

    private static final JsonParser parser = new JsonParser();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public static <T> T loadConfig(Class<T> clazz, File file) {
        try {
            if (!file.createNewFile()) { //File does not exist, save to file
                return gson.fromJson(new String(Files.readAllBytes(file.toPath())), clazz);
            }else{
                T config = clazz.newInstance();
                ConfigLoader.saveConfig(config, file);
                return config;
            }
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void saveConfig(Object config, File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = gson.toJson(parser.parse(gson.toJson(config)));
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
