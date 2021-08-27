package it.chilledpanda.grocerypal.activities.splashjoin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.activities.LoginActivity;
import it.chilledpanda.grocerypal.structures.Fridge;

public class SplashJoin extends AppCompatActivity {

    @Override
    //Inizializzo l'activity a partire da uno stato vuoto
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        findViewById(R.id.login_button).setVisibility(View.GONE);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            login_first.launch(new Intent(getApplicationContext(),LoginActivity.class));
        }
        else{
            send_join_request();
        }
    }

    void send_join_request(){

        String user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = getIntent();
        //ricavo i due parametri <fridge_id,fridge_creator>
        String[] data = intent.getData().toString().replaceFirst("http://grocery.app/", "").split("/");
        String f_id = data[0];
        String f_creator = data[1];

        //f_creator viene usata come chiave , quindi controllo
        DatabaseReference database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        DatabaseReference fridges = database.child("Fridges");

        //Chiedo al server l'uid del creatore del frigo
        fridges.child(f_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child("f_creator").getValue(String.class).equals(f_creator))
                    //Invito non valido
                    Toast.makeText(SplashJoin.this, "Invito non valido", Toast.LENGTH_SHORT).show();
                else{
                    //Invito valido
                    //Aggiungo l'id utente nella lista delle member request se non c'è già
                    Fridge f = snapshot.getValue(Fridge.class);
                    if(!f.members.contains(user_uid)){
                        if(f.member_requests==null){
                            f.member_requests=new ArrayList<>();
                        }
                        if(!f.member_requests.contains(user_uid)){
                            f.member_requests.add(user_uid);
                            fridges.child(f_id).setValue(f);
                            Toast.makeText(SplashJoin.this, "Richiesta inviata", Toast.LENGTH_SHORT).show();
                        }
                        else Toast.makeText(SplashJoin.this, "Richiesta già inviata", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(SplashJoin.this, "Utente già presente", Toast.LENGTH_SHORT).show();

                    //Apro l'activity in un altro Task
                    Intent dialogIntent = new Intent(SplashJoin.this, LoginActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);

                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Crea un launcher<InputType> per eseguire un ActivityResultContract, simile a startActivityForResult però
    //con il vantaggio che ActivityResultContracts offre un API che gestisce gli intent request-response under-the-hood
    //Parametri <Intent,ActivityResult> , il launcher viene eseguito con <nome>.launch(intent)
    ActivityResultLauncher<Intent> login_first = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        send_join_request();
                    }
                }
            });
}

