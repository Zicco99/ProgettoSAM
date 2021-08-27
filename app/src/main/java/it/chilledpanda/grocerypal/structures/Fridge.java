package it.chilledpanda.grocerypal.structures;

import java.io.Serializable;
import java.util.ArrayList;


public class Fridge implements Serializable{
    public String f_name;
    public String f_desc;
    public String f_creator;
    public ArrayList<String> members;
    public ArrayList<String> member_requests;
    public ArrayList<Prodotto> prodotti;

    public Fridge(){ }

    public Fridge(String f_name, String f_desc, String f_creator) {
        this.f_name = f_name;
        this.f_desc = f_desc;
        this.f_creator = f_creator;
        this.members = new ArrayList<>();
        this.members.add(f_creator);
    }

    public Boolean contains(String barcode){
        for(Prodotto p : prodotti){
            if(p.barcode.equals(barcode)) return true;
        }
        return false;
    }

    public int Indexof(String barcode){
        for(Prodotto p : prodotti){
            if(p.barcode.equals(barcode)) return prodotti.indexOf(p);
        }
        return -1;
    }
}
