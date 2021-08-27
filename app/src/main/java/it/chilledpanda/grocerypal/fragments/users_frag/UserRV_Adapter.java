package it.chilledpanda.grocerypal.fragments.users_frag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.fragments.fridge_frag.ProductRV_Adapter;
import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Fridge;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class UserRV_Adapter extends RecyclerView.Adapter<UserRV_Adapter.ViewHolder> {
    private LinkedHashMap<String,Boolean> members = new LinkedHashMap<>();
    private Context c;
    public DatabaseReference fridge;
    public DatabaseReference users;
    private LayoutInflater mInflater;
    private int admin_mode;
    public String f_creator;

    //Costruttore
    public UserRV_Adapter(Context context, LayoutInflater inf, DatabaseReference fridge, DatabaseReference users) {
        this.c = context;
        this.fridge = fridge;
        this.users=users;
        this.mInflater = inf;
        this.admin_mode=0;

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //User Mode Listener
        fridge.child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String new_user = snapshot.getValue(String.class);
                members.put(new_user,true);
                UserRV_Adapter.this.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String torem_user = snapshot.getValue(String.class);
                members.remove(torem_user);
                UserRV_Adapter.this.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });

        //Ottengo l'id dell'admin e setto la modalità (ADMIN MODE / USER MODE)
        fridge.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                f_creator = snapshot.child("f_creator").getValue(String.class);

                if(f_creator.equals(user_id)){
                    admin_mode=1;
                    //Admin Mode Listener, mi metto in ascolto su questo sotto albero per gestire le richieste di partecipazione
                    fridge.child("member_requests").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String new_user = snapshot.getValue(String.class);
                            members.put(new_user,false);
                            UserRV_Adapter.this.notifyDataSetChanged();
                        }
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            String torem_user = snapshot.getValue(String.class);
                            members.remove(torem_user);
                            UserRV_Adapter.this.notifyDataSetChanged();
                        }
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //Collega dati a view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String user_id = new ArrayList<String>(members.keySet()).get(position);
        Boolean accepted = members.get(user_id);

        //Ottieni da firebase Uri imagine e nome
        users.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatar_uri = snapshot.child("avatar_uri").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);
                Picasso.get().load(avatar_uri).transform(new ProductRV_Adapter.CircleTransform()).into(holder.avatar);
                holder.name.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });

        if(accepted) conf_as_accepted(user_id,holder);
        else conf_as_tobeaccepted(user_id,holder);
    }

    //Gonfia il layout xml quando necessario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.user_pannel, parent, false);
        return new ViewHolder(view);
    }

    //Fornisce il numero di elementi alla recyclerview
    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView avatar;
        CardView user_card;
        TextView role;
        TextView name;
        ImageButton add;
        ImageButton remove;
        ConstraintLayout user_panel_layout;

        ViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_avatar);
            user_card = itemView.findViewById(R.id.user_card);
            role = itemView.findViewById(R.id.role);
            name = itemView.findViewById(R.id.user_name);
            add = itemView.findViewById(R.id.add_butt);
            remove = itemView.findViewById(R.id.rmv_butt);
            user_panel_layout = itemView.findViewById(R.id.user_pannel_layout);
        }
    }

    //LOGICA

    void conf_as_accepted(String user_id, ViewHolder holder){
        //Setto il ruolo
        if (user_id.equals(f_creator)) holder.role.setText("Admin");
        else holder.role.setText("User");

        //Nascondo i button
        holder.add.setVisibility(View.GONE);
        holder.remove.setVisibility(View.GONE);

        //Adatto il constrainset, essendo cambiati gli elementi del layout
        ConstraintSet pending_cs = new ConstraintSet();
        pending_cs.clone(holder.user_panel_layout);
        pending_cs.connect(R.id.user_avatar, ConstraintSet.TOP,R.id.cardview_role,ConstraintSet.BOTTOM,0);
        holder.user_panel_layout.setConstraintSet(pending_cs);

        //Configura la view come cancellabile [a patto che l'admin mode sia on e la view non abbia l'userid = admin_id]
        if(!user_id.equals(f_creator) && admin_mode == 1) conf_as_removable(user_id,holder);
    }

    void conf_as_removable(String user_id, ViewHolder holder){
        //Configuro l'OnClickListener che permetterà l'eliminazione dell'utente
        holder.user_card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Rendi visibile ruolo e button_remove
                holder.role.setVisibility(View.VISIBLE);
                holder.remove.setVisibility(View.VISIBLE);

                //Adatto il constrainset, essendo cambiati gli elementi del layout
                ConstraintSet pending_cs = new ConstraintSet();
                pending_cs.clone(holder.user_panel_layout);
                pending_cs.connect(R.id.user_avatar, ConstraintSet.TOP,R.id.cardview_role,ConstraintSet.BOTTOM,0);
                holder.user_panel_layout.setConstraintSet(pending_cs);

                //Setto il listener sul remove button
                holder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fridge.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Fridge f = snapshot.getValue(Fridge.class);

                                //Rimuovo l'utente
                                f.members.remove(user_id);

                                if(f.prodotti!=null) {
                                    //Rimuovo i suoi prodotti
                                    for (Prodotto p : f.prodotti) {
                                        for (Articolo a : p.articoli) {
                                            if (a.owner_id.equals(user_id)) p.articoli.remove(a);
                                        }
                                    }
                                }
                                fridge.setValue(f);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(c, "Eliminazione cancellata , Firebase non raggiungibile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                return false;
            }
        });
    }

    private void conf_as_tobeaccepted(String user_id, ViewHolder holder) {

        //Nascondi il ruolo
        holder.role.setVisibility(View.GONE);

        //Cambio il layout , perchè l'utente deve essere confermato
        ConstraintSet pending_cs = new ConstraintSet();
        pending_cs.clone(holder.user_panel_layout);
        pending_cs.connect(R.id.user_avatar, ConstraintSet.TOP,R.id.add_butt,ConstraintSet.BOTTOM,0);
        holder.user_panel_layout.setConstraintSet(pending_cs);

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ottieni da firebase Uri imagine e nome
                fridge.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Fridge f = snapshot.getValue(Fridge.class);
                        f.member_requests.remove(user_id);
                        f.members.add(user_id);
                        fridge.setValue(f);
                        holder.role.setVisibility(View.VISIBLE);
                        holder.add.setVisibility(View.GONE);
                        holder.remove.setVisibility(View.GONE);


                        //Cambio il layout , perchè l'utente è stato confermato confermato
                        ConstraintSet pending_cs = new ConstraintSet();
                        pending_cs.clone(holder.user_panel_layout);
                        pending_cs.connect(R.id.user_avatar, ConstraintSet.TOP,R.id.add_butt,ConstraintSet.BOTTOM,0);
                        holder.user_panel_layout.setConstraintSet(pending_cs);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ottieni da firebase Uri imagine e nome
                fridge.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Fridge f = snapshot.getValue(Fridge.class);
                        f.member_requests.remove(user_id);
                        fridge.setValue(f);
                        holder.role.setVisibility(View.VISIBLE);
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

