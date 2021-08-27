package it.chilledpanda.grocerypal.b_receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.activities.LoginActivity;
import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Fridge;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class AlarmsReceiver extends BroadcastReceiver {
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;

        //Base
        String ref = intent.getStringExtra("ref").replace("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app/", "");
        DatabaseReference database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        String[] path = ref.split("/");
        DatabaseReference fridge_ref = database.child(path[0]).child(path[1]);

        //UUID del prodotto
        int uuid = intent.getIntExtra("uuid", 0);

        //Cerco il prodotto e ottengo i dati
        fridge_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Fridge f = snapshot.getValue(Fridge.class);
                boolean found = false;
                for (Prodotto prod : f.prodotti) {
                    for (Articolo art : prod.articoli) {
                        if (art.uuid == uuid) {
                            create_show_notification(snapshot.getKey(), prod.name, f.f_name, art);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void create_show_notification(String channel_id, String nome_prod, String nome_frigo, Articolo art) {

        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent not_intent = new Intent(c, LoginActivity.class);

        not_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(c, 0, not_intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c, channel_id)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Articolo in Scadenza")
                .setContentIntent(intent)
                .setStyle(new NotificationCompat.InboxStyle().setSummaryText(nome_prod +" scadrà tra poco"))
                .setGroup(nome_frigo)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Aggrega più notifiche, aggregate per frigo
        Notification summaryNotification = new NotificationCompat.Builder(c, channel_id)
                .setContentTitle(nome_frigo)
                .setSmallIcon(R.drawable.notification_icon)
                .setStyle(new NotificationCompat.InboxStyle().setSummaryText(nome_frigo))
                .setGroup(nome_frigo)
                .setContentIntent(intent)
                .setShowWhen(false)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(channel_id.hashCode(), summaryNotification);
        notificationManager.notify(art.uuid, builder.build());
    }
}
