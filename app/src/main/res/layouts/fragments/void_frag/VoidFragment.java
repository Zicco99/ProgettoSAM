package it.chilledpanda.grocerypal.fragments.void_frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.chilledpanda.grocerypal.R;

public class VoidFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fridge_fragment, container, false);
        ((TextView) root.findViewById(R.id.noprod_text)).setText("Seleziona un frigo");
        root.findViewById(R.id.recycler_view).setVisibility(View.GONE);
        root.findViewById(R.id.scan_add).setVisibility(View.GONE);
        return root;
    }
}
