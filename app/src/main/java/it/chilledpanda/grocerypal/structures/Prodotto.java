package it.chilledpanda.grocerypal.structures;

import java.io.Serializable;
import java.util.ArrayList;

public class Prodotto implements Serializable {
    public String name;
    public String barcode;
    public int quantità;
    public String img_url;
    public ArrayList<Articolo> articoli;

    public Prodotto() {
    }

    public Prodotto(String name,String barcode, int quantità, String img_url, ArrayList<Articolo> articoli) {
        this.name = name;
        this.barcode = barcode;
        this.quantità = quantità;
        this.img_url = img_url;
        this.articoli = articoli;
    }
}
