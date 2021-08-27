package it.chilledpanda.grocerypal.activities.home;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.b_receiver.AlarmsReceiver;
import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Fridge;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class FridgesSP_Adapter extends ArrayAdapter<String>{

    private LinkedHashMap<String,String> fridges = new LinkedHashMap<>();
    private LayoutInflater inflater;

    public FridgesSP_Adapter(Context context,DatabaseReference all_data,LayoutInflater inflater) {
        super(context, R.layout.fridge_spinner_row,new ArrayList<String>());
        this.inflater = inflater;

        //Ottengo il singleton che contiene le info dell'utente
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        //Installo il listener sull'array che contiene gli id associati
        all_data.child("Fridges").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Un frigo è stato aggiunto,controllo che l'utente sia un membro -> se è membro aggiungo o/w ignoro
                Fridge f = snapshot.getValue(Fridge.class);
                for(String mem : f.members){
                    if(mem.equals(uid)){
                        String new_fridge_id = snapshot.getKey();
                        String new_fridge_name = f.f_name;

                        fridges.put(new_fridge_name,new_fridge_id);
                        FridgesSP_Adapter.this.add(new_fridge_name);

                        //Creo il notification channel del frigo
                        NotificationChannel channel = new NotificationChannel(
                                snapshot.getKey(),
                                f.f_name,
                                NotificationManager.IMPORTANCE_DEFAULT
                        );
                        channel.setDescription("Gestisce le notifiche del frigo : " + f.f_name);

                        NotificationManager manager = context.getSystemService(NotificationManager.class);
                        manager.createNotificationChannel(channel);
                        
                        // break;
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //Un frigo è stato rimosso,controllo che l'utente sia un membro -> se è membro rimuovo o/w ignoro
                Fridge f = snapshot.getValue(Fridge.class);
                for(String mem : f.members){
                    if(mem.equals(uid)){
                        String torem_fridge_name = f.f_name;
                        fridges.remove(torem_fridge_name);
                        FridgesSP_Adapter.this.remove(torem_fridge_name);

                        //Rimuovo le sveglie dei prodotti , se ne ha
                        if(f.prodotti!=null) {
                            Intent notifica_scadenza = new Intent(context, AlarmsReceiver.class);
                            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                            for (Prodotto p : f.prodotti) {
                                for (Articolo art : p.articoli) {
                                    if (PendingIntent.getBroadcast(context.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_NO_CREATE) != null) { //!=null-> esiste
                                        //Elimino la sveglia , identificata attraverso l'id univoco dell'articolo
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), art.uuid, notifica_scadenza, PendingIntent.FLAG_UPDATE_CURRENT);
                                        am.cancel(pendingIntent);
                                        pendingIntent.cancel();
                                    }
                                }
                            }
                        }

                        //Elimino il canale delle notifiche
                        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.deleteNotificationChannel(snapshot.getKey());

                        break;
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        View v = inflater.inflate(R.layout.fridge_spinner_row, parent, false);
        TextView t = v.findViewById(R.id.fridge_name_spinner);

        t.setText(this.getFridgeName(position));
        t.setGravity(Gravity.CENTER);
        t.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_medium));
        t.setTextSize(14);

        return v;
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = inflater.inflate(R.layout.fridge_spinner_row, parent, false);
        TextView t = v.findViewById(R.id.fridge_name_spinner);

        t.setText(this.getFridgeName(position));
        t.setGravity(Gravity.CENTER);
        t.setTypeface(ResourcesCompat.getFont(getContext(), R.font.roboto_medium));
        t.setTextSize(14);

        return v;
    }

    public String getFridgeID(int position){
        return (String) fridges.values().toArray()[position];
    }

    public String getFridgeName(int position){
        return this.getItem(position);
    }
}
