package it.chilledpanda.grocerypal.structures;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Articolo implements Serializable{
    public String owner;
    public String owner_id;
    public Date data_scadenza;
    public int uuid;
    public Boolean alarm;

    public Articolo() {
    }

    public Articolo(Date data_scadenza) {
        this.owner = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        this.owner_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.data_scadenza = data_scadenza;
        //UUID per generare un integer univoco [Per la gestione delle notifiche]
        this.uuid = Math.abs(UUID.randomUUID().toString().hashCode());
        this.alarm=true;
    }
}
