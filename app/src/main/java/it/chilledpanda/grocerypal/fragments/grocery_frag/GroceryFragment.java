package it.chilledpanda.grocerypal.fragments.grocery_frag;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.chilledpanda.grocerypal.R;

public class GroceryFragment extends Fragment {
    //Context dell'activity
    private Context c;

    //Ottengo l'id del frigo per accedere allo spazio dedicato nel database [passato da HomeActivity]
    private String f_id;


    //Radice view del fragment
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        c = getActivity();
        root = inflater.inflate(R.layout.grocery_fragment, container, false);
        f_id = this.getArguments().getString("id_fridge");

        DatabaseReference database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        DatabaseReference user = database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        return root;
    }

}
