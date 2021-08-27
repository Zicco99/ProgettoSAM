package it.chilledpanda.grocerypal.activities.home;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.activities.LoginActivity;
import it.chilledpanda.grocerypal.fragments.fridge_frag.FridgeFragment;
import it.chilledpanda.grocerypal.fragments.fridge_frag.ProductRV_Adapter;
import it.chilledpanda.grocerypal.fragments.grocery_frag.GroceryFragment;
import it.chilledpanda.grocerypal.fragments.users_frag.UsersFragment;
import it.chilledpanda.grocerypal.fragments.void_frag.VoidFragment;
import it.chilledpanda.grocerypal.structures.Fridge;

public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    //3 riferimenti diversi per 3 parti differenti del database
    private DatabaseReference user_data;
    private DatabaseReference fridge_data;
    private DatabaseReference all_data;

    //Un riferimento al dialog per l'aggiunta di un frigo (reso globale per mantere lo stato e controllare se sia già visualizzato)
    private AlertDialog add_fridge_dialog;

    //Contiene le informazioni dell'utente
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //Mantiene il frigo selezionato
    FridgesSP_Adapter fridges_adapter;
    String curr_f_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ottengo il database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app");

        all_data = database.getReference();

        user_data = database.getReference().child("Users");

        fridge_data = database.getReference().child("Fridges");

        //Setto la toolbar e rimuovo il titolo
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout mDrawer = findViewById(R.id.drawer_layout);
                //Setto la navbar laterale
                Picasso.get().load(user.getPhotoUrl()).transform(new ProductRV_Adapter.CircleTransform()).into((ImageView) mDrawer.findViewById(R.id.nav_avatar));
                ((TextView) mDrawer.findViewById(R.id.nav_name)).setText(user.getDisplayName());
                if(curr_f_id==null) ((TextView) mDrawer.findViewById(R.id.nav_info)).setText("Nessun frigo selezionato");
                else {
                    fridge_data.child(curr_f_id).child("f_name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ((TextView) mDrawer.findViewById(R.id.nav_info)).setText("Frigo selezionato : " + snapshot.getValue(String.class));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                mDrawer.openDrawer(Gravity.LEFT);
            }
        });

        //Setto il nav_view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setto lo spinner
        Spinner spin = (Spinner) findViewById(R.id.fridge_spin);
        spin.setOnItemSelectedListener(this);

        fridges_adapter = new FridgesSP_Adapter(this,all_data,getLayoutInflater());
        fridges_adapter.setNotifyOnChange(true);
        spin.setAdapter(fridges_adapter);

    }

    //Gonfia il layout del menu nella toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main_dark, menu);
        return true;
    }

    //Definisco le azioni
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_fridge: {
                //Faccio in modo che ci sia solo un dialog per volta
                if (add_fridge_dialog != null && add_fridge_dialog.isShowing()) return false;

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                // Set title, icon, can not cancel properties.
                alertDialogBuilder.setTitle("Add Fridge");
                alertDialogBuilder.setIcon(R.drawable.add_fridge_dark);
                alertDialogBuilder.setCancelable(false);

                View gg = LayoutInflater.from(HomeActivity.this).inflate(R.layout.popup_add_fridge, null);
                alertDialogBuilder.setView(gg);

                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String f_name = ((EditText) gg.findViewById(R.id.fridge_name)).getText().toString();
                        String f_desc = ((EditText) gg.findViewById(R.id.fridge_desc)).getText().toString();
                        String f_creator = user.getUid();

                        //check if there already a fridge with that name
                        for (int i = 0; i < fridges_adapter.getCount(); i++) {
                            if (f_name.equals(fridges_adapter.getFridgeName(i))) {
                                Toast.makeText(getBaseContext(), "Esiste già un frigo con questo nome", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        //Creo l'oggetto fridge e lo inserisco nel database
                        DatabaseReference new_id = fridge_data.push();
                        new_id.setValue(new Fridge(f_name, f_desc, f_creator));

                    }
                });
                alertDialogBuilder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                add_fridge_dialog = alertDialogBuilder.create();
                add_fridge_dialog.show();
                break;
            }

            case R.id.remove_fridge:{

                if (curr_f_id == null)
                    Toast.makeText(HomeActivity.this, "Seleziona un frigo prima!", Toast.LENGTH_LONG).show();

                //Faccio in modo che ci sia solo un dialog per volta
                if (add_fridge_dialog != null && add_fridge_dialog.isShowing()) return false;

                all_data.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Fridge f = snapshot.child("Fridges").child(curr_f_id).getValue(Fridge.class);
                        if (f.f_creator.equals(user.getUid())) {
                            //Allora posso eliminare il frigo

                            //Rimuovo il frammento prima
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.nav_host_fragment, new VoidFragment()).commit();

                            //Elimino i dati del frigo su firebase -> listener in FridgeSP_Adapter mi aggiorna lo spinner
                            fridge_data.child(curr_f_id).removeValue();

                        } else {
                            Toast.makeText(HomeActivity.this, "Non sei l'amministratore , non puoi eliminare il frigo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                    }
                });

                break;
            }

            case R.id.filter: {
                filter_clicked=!filter_clicked;
                //disabilito i due buttons
                findViewById(R.id.filter).setClickable(false);

                View f = findViewById(R.id.nav_host_fragment);
                ValueAnimator anim;
                if(!filter_clicked) anim = ValueAnimator.ofInt(f.getMeasuredHeight(), f.getMeasuredHeight() + findViewById(R.id.filter_option).getMeasuredHeight());
                else anim = ValueAnimator.ofInt(f.getMeasuredHeight(), f.getMeasuredHeight() - findViewById(R.id.filter_option).getMeasuredHeight());

                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = f.getLayoutParams();
                        layoutParams.height = val;
                        f.setLayoutParams(layoutParams);
                    }
                });
                anim.setDuration(300);
                anim.start();
                f.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //a fine animazione riabilito il click per sort solo se è chiuso
                        findViewById(R.id.filter).setClickable(true);
                    }
                }, 300);

            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    Boolean filter_clicked=false;

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        curr_f_id = fridges_adapter.getFridgeID(position);

        Bundle bundle = new Bundle();
        bundle.putString("id_fridge",curr_f_id);

        FridgeFragment currFridge = new FridgeFragment();
        currFridge.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.nav_host_fragment,currFridge).commit();

        //Configuro il filtro
        RecyclerView rv_filter = (RecyclerView) findViewById(R.id.recycler_filter);
        rv_filter.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
        FilterRV_Adapter filter_adapter = new FilterRV_Adapter(this,currFridge,getLayoutInflater(),fridge_data.child(curr_f_id),user_data);
        rv_filter.setAdapter(filter_adapter);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.man_products: {
                if (curr_f_id == null) return true;

                Bundle bundle = new Bundle();
                bundle.putString("id_fridge", curr_f_id);

                FridgeFragment currFridge = new FridgeFragment();
                currFridge.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.nav_host_fragment, currFridge).commit();

                if(filter_clicked){
                    findViewById(R.id.filter).performClick();
                    findViewById(R.id.filter).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //a fine animazione riabilito il click per sort solo se è chiuso
                            findViewById(R.id.filter).setClickable(true);
                        }
                    }, 300);
                }
                else findViewById(R.id.filter).setClickable(true);

                //Configuro il filtro
                RecyclerView rv_filter = (RecyclerView) findViewById(R.id.recycler_filter);
                rv_filter.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
                FilterRV_Adapter filter_adapter = new FilterRV_Adapter(this,currFridge,getLayoutInflater(),fridge_data.child(curr_f_id),user_data);
                rv_filter.setAdapter(filter_adapter);

                break;
            }

            case R.id.man_members: {
                if (curr_f_id == null) return true;

                Bundle bundle = new Bundle();
                bundle.putString("id_fridge", curr_f_id);

                UsersFragment frag = new UsersFragment();
                frag.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, frag).commit();

                if(filter_clicked){
                    findViewById(R.id.filter).performClick();
                }
                findViewById(R.id.filter).setClickable(false);
                break;
            }

            case R.id.man_grocery: {
                if (curr_f_id == null) return true;

                Bundle bundle = new Bundle();
                bundle.putString("id_fridge", curr_f_id);

                GroceryFragment frag = new GroceryFragment();
                frag.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, frag).commit();

                if(filter_clicked){
                    findViewById(R.id.filter).performClick();
                }
                findViewById(R.id.filter).setClickable(false);

                break;
            }

            case R.id.logout:
                // Firebase sign out
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        DrawerLayout mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(Gravity.LEFT);
        return true;
    }
    
}
