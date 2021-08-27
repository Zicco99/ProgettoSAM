package it.chilledpanda.grocerypal.b_receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Fridge;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class SetAlarmsOnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            DatabaseReference database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            String user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Riattivo le sveglie per gli articoli che sono di proprietà dell'user

            database.child("Fridges").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot fridge_ref : snapshot.getChildren()) {
                        Fridge f = fridge_ref.getValue(Fridge.class);
                        for (Prodotto prod : f.prodotti) {
                            for (Articolo art : prod.articoli) {
                                if (art.owner_id.equals(user_uid)) {
                                    Intent notifica_scadenza = new Intent(context, AlarmsReceiver.class);
                                    notifica_scadenza.putExtra("uuid", art.uuid);
                                    notifica_scadenza.putExtra("ref", database.child("Fridges").child(fridge_ref.getKey()).child("prodotti").toString());
                                    if (art.alarm) {
                                        long when = art.data_scadenza.getTime();
                                        //Creo il pendingIntent e lo aggiungo all'alarm manager associandolo all'id univoco dell'articolo
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), art.uuid, notifica_scadenza, 0);
                                        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error in Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
}
