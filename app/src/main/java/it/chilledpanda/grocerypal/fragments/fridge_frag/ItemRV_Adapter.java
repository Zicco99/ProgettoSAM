package it.chilledpanda.grocerypal.fragments.fridge_frag;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.b_receiver.AlarmsReceiver;
import it.chilledpanda.grocerypal.structures.Articolo;

public class ItemRV_Adapter extends RecyclerView.Adapter<ItemRV_Adapter.ViewHolder>{
    private LayoutInflater mInflater;
    private Context c;
    private String name;
    private ArrayList<Articolo> articoli;
    private DatabaseReference prodotti_ref;
    private AlarmManager am;


    //Costruttore
    public ItemRV_Adapter(Context context,String name, DatabaseReference prodotti_ref) {
        this.c = context;
        this.name = name;
        this.mInflater = LayoutInflater.from(context);
        this.articoli = new ArrayList<Articolo>();
        this.prodotti_ref = prodotti_ref;
        this.am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    }

    //Gonfia il layout xml quando necessario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.expire_row, parent, false);
        return new ViewHolder(view);
    }

    //Collega dati a view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Articolo art = articoli.get(position);

        Date expire = art.data_scadenza;
        String date_expire = expire.getDate() + "/" + expire.getMonth();
        holder.expire_text.setText("Scadrà il : " + date_expire);
        holder.owner_text.setText(art.owner);

        if(!art.owner_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.removeButt.setVisibility(View.GONE);
            holder.notification.setVisibility(View.GONE);
        }
        else {
            Intent notifica_scadenza = new Intent(c, AlarmsReceiver.class);
            notifica_scadenza.putExtra("uuid",art.uuid);
            notifica_scadenza.putExtra("ref",prodotti_ref.toString());
            PendingIntent pendingIntent;

            if (art.alarm) {
                //alarm = on -> controllo se c'è la sveglia settata o/w l'aggiungo
                if (PendingIntent.getBroadcast(c.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_NO_CREATE) == null) { //==null-> non esiste

                    long when = art.data_scadenza.getTime();

                    //Creo il pendingIntent e lo aggiungo all'alarm manager associandolo all'id univoco dell'articolo
                    pendingIntent = PendingIntent.getBroadcast(c.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_UPDATE_CURRENT);
                    am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
                }
                //Setto l'immagine a sveglia settata
                holder.notification.setImageResource(R.drawable.remove_notification);

            } else {
                //alarm = off -> controllo se c'è la sveglia settata -> l'elimino
                if (PendingIntent.getBroadcast(c.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_NO_CREATE) != null) { //!=null-> esiste
                    //Elimino la sveglia , identificata attraverso l'id univoco dell'articolo
                    pendingIntent = PendingIntent.getBroadcast(c.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_UPDATE_CURRENT);
                    am.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
                //Setto l'immagine a sveglia settata
                holder.notification.setImageResource(R.drawable.add_notification);

            }

            //Configuro l'eliminazione
            holder.removeButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PendingIntent pendingIntent;
                    //Elimino la sveglia,se esiste
                    if (PendingIntent.getBroadcast(c, art.uuid, notifica_scadenza, PendingIntent.FLAG_NO_CREATE) != null) { //!=null-> esiste
                        pendingIntent = PendingIntent.getBroadcast(c.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_UPDATE_CURRENT);
                        am.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }

                    prodotti_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            DatabaseReference ref;
                            boolean found=false;
                            for (DataSnapshot d : snapshot.getChildren()) {
                                for (DataSnapshot a : d.child("articoli").getChildren()) {
                                    if (a.getValue(Articolo.class).uuid == art.uuid){
                                        prodotti_ref.child(d.getKey()).child("articoli").child(a.getKey()).removeValue();
                                        found=true;
                                        break;
                                    }
                                }
                                if(found) break;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            //Configuro il cambiamento della sveglia
            holder.notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prodotti_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean found=false;
                            for (DataSnapshot d : snapshot.getChildren()) {
                                for (DataSnapshot a : d.child("articoli").getChildren()) {
                                    Articolo new_art = a.getValue(Articolo.class);
                                    if (new_art.uuid == art.uuid){
                                        new_art.alarm =! new_art.alarm;
                                        prodotti_ref.child(d.getKey()).child("articoli").child(a.getKey()).setValue(new_art);
                                        found=true;
                                        break;
                                    }
                                }
                                if(found) break;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            });
        }
    }


    @Override
    public int getItemCount() {
        return articoli.size();
    }

    public void setArticoli(ArrayList<Articolo> articoli){
        this.articoli = articoli;
    }

    //Mantiene e ricicla le view e ricicla nel caso le view non più visibili
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView expire_text;
        TextView owner_text;
        ImageButton removeButt;
        ImageButton notification;
        ConstraintLayout costr;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            expire_text = itemView.findViewById(R.id.expire_text);
            owner_text = itemView.findViewById(R.id.owner_text);
            removeButt = itemView.findViewById(R.id.remove_butt);
            notification = itemView.findViewById(R.id.notification);
            costr = itemView.findViewById(R.id.expire_costr);
        }
    }

}
