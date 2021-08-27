package it.chilledpanda.grocerypal.fragments.users_frag;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import it.chilledpanda.grocerypal.R;

public class UsersFragment extends Fragment {
    //Context dell'activity
    private Context c;
    private AlertDialog add_user_dialog;

    //Radice view del fragment
    View root;

    //Struttura che si pone tra la recycler view e il data source
    UserRV_Adapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        c = getActivity();
        root = inflater.inflate(R.layout.users_fragment, container, false);
        String f_id = this.getArguments().getString("id_fridge");

        DatabaseReference database = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        database.child("Fridges").child(f_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String f_creator = snapshot.child("f_creator").getValue(String.class);
                FloatingActionButton scan_add_butt = root.findViewById(R.id.scan_add);
                scan_add_butt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (add_user_dialog != null && add_user_dialog.isShowing()) return;

                        // Create a AlertDialog Builder.
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
                        // Set title, icon, can not cancel properties.
                        alertDialogBuilder.setTitle("Invita un coinquilino");
                        alertDialogBuilder.setIcon(R.drawable.user_add);
                        alertDialogBuilder.setCancelable(true);

                        View dialog = LayoutInflater.from(c).inflate(R.layout.popup_add_user, null);
                        String link = "http://grocery.app/" + f_id + "/" + f_creator;

                        try {
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.encodeBitmap(link, BarcodeFormat.QR_CODE, 400, 400);
                            ImageView imageViewQrCode = (ImageView) dialog.findViewById(R.id.qr_code);
                            imageViewQrCode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            Toast.makeText(c, "Errore nella creazione del link di invito", Toast.LENGTH_SHORT).show();
                        }

                        Button get_link = (Button) dialog.findViewById(R.id.get_button);
                        get_link.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Get invite Link", link);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(c, "Link copiato nella clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        });


                        alertDialogBuilder.setView(dialog);
                        add_user_dialog = alertDialogBuilder.create();
                        add_user_dialog.show();
                    }
                });

                //Configuro RecyclerView con l'ausilio di RV_Adapter
                RecyclerView mRW = root.findViewById(R.id.user_rv);
                adapter = new UserRV_Adapter(c, getLayoutInflater(), database.child("Fridges").child(f_id),database.child("Users"));
                mRW.setLayoutManager(new GridLayoutManager(c, 3));
                mRW.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(c, "Errore Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        return root;

    }
}