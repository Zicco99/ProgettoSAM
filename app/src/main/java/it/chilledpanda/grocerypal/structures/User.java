package it.chilledpanda.grocerypal.structures;

import android.net.Uri;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String avatar_uri;

    public User(String name, Uri avatar_uri) {
        this.name = name;
        this.avatar_uri = avatar_uri.toString();
    }
}
