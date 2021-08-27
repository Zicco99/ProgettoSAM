package it.chilledpanda.grocerypal.fragments.fridge_frag;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.activities.scan.ScanActivity;
import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Fridge;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class FridgeFragment extends Fragment {
    //Context dell'activity
    private Context c;

    //Ottengo l'id del frigo per accedere allo spazio dedicato nel database [passato da HomeActivity]
    private String f_id;

    //Riferimento al sottoalbero che corrisponde al frigo
    private DatabaseReference frigo_ref;

    //Gestore di richieste asincrone (Modello FIFO)
    private RequestQueue requestQueue;

    //Radice view del fragment
    private View root;

    //Struttura che si pone tra la recycler view e il data source
    public ProductRV_Adapter adapter;

    //Barcode del prodotto in aggiunta
    private String barcode;

    //Filtro di visualizzazione
    public ArrayList<String> filtro;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fridge_fragment, container, false);

        f_id = this.getArguments().getString("id_fridge");
        c = getActivity();

        //Configuro la requestQueue per possibili get/post requests
        requestQueue = Volley.newRequestQueue(c);

        //Definisco la DataReference ai prodotti del frigo
        frigo_ref = FirebaseDatabase.getInstance("https://grocerypal-4881f-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference().child("Fridges").child(f_id);

        //Configuro le azioni
        FloatingActionButton scan_add_butt = root.findViewById(R.id.scan_add);
        scan_add_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(c, ScanActivity.class);
                add_scan.launch(intent);
            }
        });

        //Configuro RecyclerView con l'ausilio di RV_Adapter
        RecyclerView mRW = root.findViewById(R.id.recycler_view);
        TextView f = root.findViewById(R.id.noprod_text);

        adapter = new ProductRV_Adapter(c,frigo_ref);


        //Creo un listener sulla lista dei prodotti che mi modificano il datasource della recyclerview a seguito di eventi
        frigo_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Fridge new_f = snapshot.getValue(Fridge.class);

                //Se il frigo è stato eliminato non mostrare nulla
                if(new_f==null) return;

                if(new_f.prodotti==null || new_f.prodotti.size()==0){
                    f.setVisibility(View.VISIBLE);
                    return;
                }
                else f.setVisibility(View.GONE);

                //Fase di cleanup, riordino ed elimino i null
                ArrayList<Prodotto> prod_ord = new ArrayList<Prodotto>();

                Boolean null_prod=false;
                Boolean null_art=false;

                int i=0;
                for(Prodotto p : new_f.prodotti){
                    //c'è la struttura del prodotto
                    if(p!=null){
                        //se il prodotto c'è , controllo i suoi articoli prima di aggiungerlo alla lista aggiornata
                        if(p.articoli==null){
                            //Su firebase quando elimino l'ultimo articolo viene resto null l'array -> elimino la struttura del prodotto
                            frigo_ref.child("prodotti").child(String.valueOf(i)).removeValue();
                            i++;
                        }
                        else {
                            //se p.articoli != null vuol dire che c'è almeno un elemento -> eseguo un clean dei null
                            ArrayList<Articolo> art_ord = new ArrayList<Articolo>();
                            for (Articolo art : p.articoli) {
                                if (art != null) {
                                    art_ord.add(art);
                                }
                                else null_art = true;
                            }

                            //Se c'è almeno un elemento != null -> ridefinisci p e aggiungilo alla lista aggiornata
                            if (art_ord.size() != 0) {
                                p.articoli = art_ord;
                                p.quantità = art_ord.size();
                                prod_ord.add(p);
                            } else {
                                frigo_ref.child("prodotti").child(String.valueOf(i)).removeValue();
                            }
                        }
                    }
                    else null_prod=true;
                }

                frigo_ref.child("prodotti").setValue(prod_ord);

                //Se la ricorsione non ha eseguito modifiche passa la nuova struttura alla recyclerview
                if(!null_prod && !null_art) {
                    adapter.setProdotti(prod_ord);
                    adapter.filter(filtro);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRW.setLayoutManager(new LinearLayoutManager(c));
        mRW.setAdapter(adapter);
        return root;
    }

    //Crea un launcher<InputType> per eseguire lo scan, simile a startActivityForResult però
    //con il vantaggio che ActivityResultContracts offre un API che gestisce gli intent request-response under-the-hood
    //Parametri <Intent,ActivityResult> , il launcher viene eseguito con <nome>.launch(intent)
    ActivityResultLauncher<Intent> add_scan = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Eseguo il parsing dell'intent perchè è particolare
                        IntentResult res = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                        barcode = res.getContents();

                        //Aggiorno le info su firebase, successivamente attraverso il listener sui products aggiornerò i dati locali
                        frigo_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Fridge f = snapshot.getValue(Fridge.class);
                                if(f.prodotti==null) f.prodotti = new ArrayList<Prodotto>(); //Se è il primo elemento ad essere aggiunto, crea l'array dei prodotti

                                if(!f.contains(barcode)){
                                    ask_client_data_confirm_firstprod(f);
                                }
                                else{
                                    ask_client_data_confirm_notfirstprod(f);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(c, "Firebase irraggiungibile , aggiunta prodotto fallita", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        });
                    }
                }
            });

    //Variabili globali per gestire l'update dell'immagine nel popup della scelta
    AlertDialog add_fridge_dialog;
    ImageView img_view;

    File imageFile;
    Uri image_uri;
    Boolean img_changed; //Boolean that tells if user changed product image

    String post_img; //BASE64 Image [scelta dall'utente]
    String post_name; //Nome prodotto [scelto dall'utente]
    String get_name; //Nome ricevuto dal server
    String get_img_url; //Url immagine, ricevuta dal server


    //Metodo chiamato se il prodotto aggiunto non è presente ancora nel frigo
    public void ask_client_data_confirm_firstprod(Fridge f){

        //Mostra solo se non c'è un altro dialog add_fridge
        if (add_fridge_dialog != null && add_fridge_dialog.isShowing()) return;

        //FASE 1: Eseguo una GET al server python dove chiedo info sul prodotto

        String url = "http://23.94.219.145:80/barcode/it/" + barcode;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //oggetti per gestire l'immagine
                get_img_url = null;
                get_name = null;

                //se il counter ottenuto dalla get > soglia allora il dato (img/name) è confermato -> rendi non modificabile
                int c_image = 0;
                int c_name = 0;
                int soglia_image = 10;
                int soglia_name = 10;

                img_changed = false;

                //Eseguo il parsing della risposta json
                try {
                    get_img_url = response.getString("img_prod");
                    get_name = response.getString("prodotto");
                    c_image = response.getInt("counter_accepted_img");
                    c_name = response.getInt("counter_accepted_name");

                } catch (JSONException e) {
                    Toast.makeText(c, "Errore nei dati del server", Toast.LENGTH_SHORT).show();
                    return;
                }


                //FASE 2: Chiedo all'utente di confermare i dati attraverso un Dialog
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                //Configuro le proprietà
                alertDialogBuilder.setTitle("Add Product");
                alertDialogBuilder.setIcon(R.drawable.add_product_dark);
                alertDialogBuilder.setCancelable(false);

                //XML -> View
                View popup = LayoutInflater.from(getActivity()).inflate(R.layout.popup_add_product, null);

                //Configuro la view
                ImageButton img_butt = popup.findViewById(R.id.add_prod_img_action);
                img_view = popup.findViewById(R.id.add_prod_img);

                //Setta l'immagine del dialog
                if (!get_img_url.equals("null")) {
                    Picasso.get().load(get_img_url).transform(new ProductRV_Adapter.CircleTransform()).into(img_view);
                    //Non ancora confermata da <soglia_image> persone -> rendi modificabile
                    if (c_image < soglia_image) img_butt.setImageResource(R.drawable.replace);
                    else img_butt.setVisibility(View.GONE);
                } else {
                    //Immagine non presente ancora
                    Picasso.get().load(R.drawable.missing_photo).transform(new ProductRV_Adapter.CircleTransform()).into(img_view);
                    img_butt.setImageResource(R.drawable.add);
                }

                //Setta un listener per una possibile modifica dell'immagine [img_button]
                img_butt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Ottengo la directory che ho impostato nel manifest e creo il file
                        File imagePath = new File(getContext().getFilesDir(), "images");

                        //Se la directory does not exists , create it
                        if(!imagePath.exists()){
                            imagePath.mkdirs();
                        }
                        imageFile = new File(imagePath.getPath(), barcode + ".jpg");
                        Toast.makeText(c, imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                        //Configuro la non persistenza alla chiusura del processo
                        imageFile.deleteOnExit();

                        //Lancio la fotocamera passandogli la Uri dove salvare la foto
                        image_uri = FileProvider.getUriForFile(getActivity(),getActivity().getPackageName(),imageFile);
                        cameraLauncher.launch(image_uri);
                    }
                });


                //Mostra il barcode nel popup
                EditText ed_bar = popup.findViewById(R.id.barcode_editext);
                ed_bar.setText(barcode);

                //Suggerisco all'utente il nome del prodotto dalle info che ottengo dal server
                TextInputLayout t = popup.findViewById(R.id.name_layout);
                EditText ed_name = popup.findViewById(R.id.name_editext);

                if (c_name < soglia_name) {
                    //Non ancora confermata da soglia_image persone, rendi modificabile
                    t.setCounterEnabled(true);
                    t.setCounterMaxLength(30);
                    t.setPlaceholderText(get_name);
                } else {
                    //else rendi non modificabile
                    ed_name.setClickable(false);
                    ed_name.setFocusable(false);
                    ed_name.setText(get_name);
                }

                //Non permetto le date vecchie nel datapicker
                DatePicker expire_picker = popup.findViewById(R.id.expire_dp);
                expire_picker.setMinDate(System.currentTimeMillis());

                //Metto il focus per mostrare il consiglio
                ed_name.requestFocus();

                //FASE 3: Ottengo i dati dal dialog e aggiorno i dati sul server attraverso una POST

                //Setto il positive e negative button
                alertDialogBuilder.setPositiveButton("Aggiungi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Prendo i dati dal dialog
                        if (img_changed) {
                            //Converto l'immagine in una stringa base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            post_img = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                            img_changed = false;
                        } else post_img = "not_changed";

                        //Setto not_changed se non ho modificato nome e foto -> verrà usato dal server Python per incrementare i contatori
                        EditText prod_name = popup.findViewById(R.id.name_editext);
                        if (prod_name.getText().toString().equals("")) {
                            post_name = "not_changed";
                        } else post_name = prod_name.getText().toString();

                        //Estraggo la data dal expire_picker
                        // Backup the system's timezone
                        DatePicker expire_picker = popup.findViewById(R.id.expire_dp);
                        Calendar cal = Calendar.getInstance();

                        cal.set(expire_picker.getYear(),expire_picker.getMonth(),expire_picker.getDayOfMonth(),8,30);
                        Date date = cal.getTime();

                        //Carico sul server python l'aggiornamento del prodotto
                        String url_post = "http://23.94.219.145:80/barcode/it/" + barcode;
                        StringRequest request_post = new StringRequest(Request.Method.POST, url_post, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Il server mi risponde con i dati aggiornati <nome_prodotto,url_foto>
                                String[] tokens = response.split("@");

                                //Prima aggiunta,creo la struttura e aggiungo
                                ArrayList<Articolo> articoli = new ArrayList<Articolo>();
                                articoli.add(new Articolo(date));

                                //Creo il prodotto e lo aggiungo
                                f.prodotti.add(new Prodotto(tokens[0],barcode,1,tokens[1],articoli));

                                frigo_ref.setValue(f);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getActivity(), "Upload fallito ,server irraggiungibile", Toast.LENGTH_LONG).show();
                            }
                        })
                                //Eseguo l'override dei 2 metodi per la post
                        {
                            //Modifico il BodyContentType in modo tale che sia la POST sia gestita da python
                            @Override
                            public String getBodyContentType() {
                                return "application/x-www-form-urlencoded; charset=UTF-8";
                            }

                            //Inserisco i parametri da inserire nel body
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("image64", post_img);
                                params.put("name", post_name);
                                return params;
                            }
                        };
                        requestQueue.add(request_post);
                    }
                });
                alertDialogBuilder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialogBuilder.setView(popup);
                AlertDialog add_fridge_dialog = alertDialogBuilder.create();
                add_fridge_dialog.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Richiesta info del prodotto fallita ,server irraggiungibile", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });
        requestQueue.add(request);
    }


    //Metodo chiamato se il prodotto aggiunto è presente
    public void ask_client_data_confirm_notfirstprod(Fridge f){

        //Mostra solo se non c'è un altro dialog add_fridge
        if (add_fridge_dialog != null && add_fridge_dialog.isShowing()) return;

        //CONFIGURO IL DIALOG
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        //Configuro le proprietà
        alertDialogBuilder.setTitle("Add Product");
        alertDialogBuilder.setIcon(R.drawable.add_product_dark);
        alertDialogBuilder.setCancelable(false);

        //XML -> View
        View popup = LayoutInflater.from(getActivity()).inflate(R.layout.popup_add_product, null);

        //Configuro la view [nascondo tutto tranne il datapicker]
        popup.findViewById(R.id.prod_img_layout).setVisibility(View.GONE);
        popup.findViewById(R.id.barcode_layout).setVisibility(View.GONE);
        popup.findViewById(R.id.name_layout).setVisibility(View.GONE);

        //Non permetto le date vecchie nel datapicker
        DatePicker expire_picker = popup.findViewById(R.id.expire_dp);
        expire_picker.setMinDate(System.currentTimeMillis());

        //Setto il positive e negative button
        alertDialogBuilder.setPositiveButton("Aggiungi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Estraggo la data dal expire_picker
                DatePicker expire_picker = popup.findViewById(R.id.expire_dp);
                int day = expire_picker.getDayOfMonth();
                int month = expire_picker.getMonth();
                int year = expire_picker.getYear();

                f.prodotti.get(f.Indexof(barcode)).articoli.add(new Articolo(new Date(year, month, day)));
                f.prodotti.get(f.Indexof(barcode)).quantità++;

                frigo_ref.setValue(f);
            }
        });

        alertDialogBuilder.setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialogBuilder.setView(popup);
        AlertDialog add_fridge_dialog = alertDialogBuilder.create();
        add_fridge_dialog.show();
    }

    //Gestisce l'avvio della camera ,salva la foto temporaneamente la foto alla uri scelta
    ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(!result){
                        Toast.makeText(getActivity(),"Foto non scattata", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Picasso.get().load(image_uri).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).transform(new ProductRV_Adapter.CircleTransform()).into(img_view);
                        img_changed = true;

                        //Avverto l'adapter che per caricare l'immagine di questo prodotto non dovrà usare la cache locale di Picasso
                        if(!adapter.p_img_modified.contains(barcode)){
                            adapter.p_img_modified.add(barcode);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
}
