package it.chilledpanda.grocerypal.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.activities.home.HomeActivity;
import it.chilledpanda.grocerypal.activities.splashjoin.SplashJoin;
import it.chilledpanda.grocerypal.structures.User;

import static android.graphics.drawable.GradientDrawable.*;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    //N.B FirebaseAuth is a singleton
    private FirebaseAuth mAuth;

    private Timer tim = null;

    @Override
    //Inizializzo l'activity a partire da uno stato vuoto
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        //Configuro il background in base al tema corrente
        TypedValue prim_col = new TypedValue();
        TypedValue sec_col = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, prim_col, true);
        getTheme().resolveAttribute(R.attr.colorSecondary, sec_col, true);

        GradientDrawable gd = new GradientDrawable(
                Orientation.TOP_BOTTOM,
                new int[]{prim_col.data,sec_col.data});

        findViewById(R.id.login_layout).setBackground(gd);

        //Configuro il login al click del button
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                g_login.launch(mGoogleSignInClient.getSignInIntent());
            }
        });

    }

    @Override
    //Sto per rendere visibile l'activity
    protected void onStart() {
        super.onStart();

        //Prepara l'entry point singleton che comunicherà con Firebase SDK e incapsulerà le info dell'utente
        mAuth = FirebaseAuth.getInstance();

        //Se l'utente è già loggato,passa all'activity home dopo 2 secondi (come se fosse una splashscreen)
        FirebaseUser user = mAuth.getCurrentUser();

        tim = new Timer();

        if (user != null) {
            //nascondi button
            findViewById(R.id.login_button).setVisibility(View.GONE);

            tim.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    switch_home();
                }
            }, 2000);
        }

        //Prepara le cose necessarie al login
        else {
            //Preparo le impostazioni
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.IdToken)) //IdToken preso dalla console di Firebase
                    .requestEmail()
                    .build();

            //Ottengo il client per comunicare con l'OAuth API di Google
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            //Se è rimasta un'istanza vecchia di login, resetta
            mGoogleSignInClient.signOut();
        }
    }



    public void switch_home(){
        //overridePendingTransition(R.anim., R.anim.slide_out_up );
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }


    //Crea un launcher<InputType> per eseguire un ActivityResultContract, simile a startActivityForResult però
    //con il vantaggio che ActivityResultContracts offre un API che gestisce gli intent request-response under-the-hood
    //Parametri <Intent,ActivityResult> , il launcher viene eseguito con <nome>.launch(intent)
    ActivityResultLauncher<Intent> g_login = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        //Login google eseguito correttamente, prelevo l'intent risultato ed eseguo il task asincrono
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class); //Permetto di rilasciare eccezioni
                            //Wrappa il token Google Sign-In ID per usarlo per loggarsi su Firebase
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);

                            //Identifichiamoci su firebase
                            mAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                //Associamo un listener per gestire gli errori
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        //Crea sul database lo spazio utente
                                        DatabaseReference user_data = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Users");
                                        user_data.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (!snapshot.hasChild(user.getUid())) {
                                                    user_data.child(user.getUid()).setValue(new User(user.getDisplayName(),user.getPhotoUrl()));
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                finish();
                                            }
                                        });

                                        if(getCallingActivity()==null) switch_home();

                                        //Se è stato chiamato da SplashJoin
                                        else if(getCallingActivity().getClassName().equals(SplashJoin.class.getName())){
                                            setResult(Activity.RESULT_OK);
                                            finish();
                                        }
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "Errore con Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            Toast.makeText(LoginActivity.this, "Errore con Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Errore",Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onPause() {
        super.onPause();
        if(tim != null) tim.cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tim != null) tim.cancel();
    }
}