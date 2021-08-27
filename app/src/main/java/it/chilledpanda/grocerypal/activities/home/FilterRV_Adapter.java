package it.chilledpanda.grocerypal.activities.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.fragments.fridge_frag.FridgeFragment;

public class FilterRV_Adapter extends RecyclerView.Adapter<FilterRV_Adapter.ViewHolder> {
    private ArrayList<String> members = new ArrayList<>();
    private ArrayList<String> filter_ids;
    private Context c;
    private DatabaseReference fridge;
    private FridgeFragment frag;
    private DatabaseReference users;
    private LayoutInflater mInflater;

    //Costruttore
    public FilterRV_Adapter(Context context,FridgeFragment frag, LayoutInflater inf, DatabaseReference fridge, DatabaseReference users) {
        this.c = context;
        this.fridge = fridge;
        this.users = users;
        this.mInflater = inf;
        this.filter_ids = new ArrayList<>();
        this.frag = frag;

        //User Mode Listener
        fridge.child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String new_user = snapshot.getValue(String.class);
                members.add(new_user);
                FilterRV_Adapter.this.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String torem_user = snapshot.getValue(String.class);
                members.remove(torem_user);
                FilterRV_Adapter.this.notifyDataSetChanged();
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
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.user_filter_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String user_id = members.get(position);


        users.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                holder.name.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Connessione Firebase Interrotta", Toast.LENGTH_SHORT).show();
            }
        });

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!filter_ids.contains(user_id)){
                    filter_ids.add(user_id);
                    holder.card.setCardBackgroundColor(c.getResources().getColor(R.color.giallo_ocra));
                }
                else{
                    filter_ids.remove(user_id);
                    holder.card.setCardBackgroundColor(c.getResources().getColor(R.color.grigio_scuro));
                }
                frag.filtro = filter_ids;
                frag.adapter.filter(filter_ids);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        ViewHolder(View itemView) {
            super(itemView);
            name =(TextView) itemView.findViewById(R.id.user_name);
            card =(CardView) itemView.findViewById(R.id.cardview_user_name);
        }

    }
}