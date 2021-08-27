package it.chilledpanda.grocerypal.fragments.fridge_frag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

import it.chilledpanda.grocerypal.R;
import it.chilledpanda.grocerypal.structures.Articolo;
import it.chilledpanda.grocerypal.structures.Prodotto;

public class ProductRV_Adapter extends RecyclerView.Adapter<ProductRV_Adapter.ViewHolder> {
    public ArrayList<Prodotto> prodotti;
    public ArrayList<Prodotto> tutti_prodotti;
    public ArrayList<String> p_img_modified = new ArrayList<>();
    public LayoutInflater mInflater;
    public DatabaseReference frigo_ref;
    private Context c;


    //Costruttore
    public ProductRV_Adapter(Context context, DatabaseReference frigo_ref) {
        this.c=context;
        this.mInflater = LayoutInflater.from(c);
        this.prodotti = new ArrayList<Prodotto>();
        this.frigo_ref = frigo_ref;
    }

    //Gonfia il layout xml quando necessario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.product_row, parent, false);
        return new ViewHolder(view);
    }

    //Collega dati a view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Prodotto p = prodotti.get(position);

        //Gestisco il caso in cui l'immagine di uno dei prodotti è stata modificata , in quel caso non viene usata la cache locale di Picasso
        if(p.img_url!="null") {
            if (p_img_modified.contains(p.barcode)) {
                Picasso.get().load(p.img_url).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.myImageView);
                p_img_modified.remove(p.barcode);
            } else
                Picasso.get().load(p.img_url).transform(new CircleTransform()).into(holder.myImageView);
        }

        holder.myTextView.setText(p.name);
        holder.counter.setText(String.valueOf(p.quantità));

        ItemRV_Adapter item_adapter = new ItemRV_Adapter(c,p.name,frigo_ref.child("prodotti"));

        //Collego al recycler l'adapter
        holder.myRecycler.setLayoutManager(new LinearLayoutManager(c));
        holder.myRecycler.setAdapter(item_adapter);

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.expanded =! holder.expanded;
                if (holder.expanded) {
                    holder.constraintSet.clear(R.id.cardview_icon, ConstraintSet.BOTTOM);
                    holder.constraintSet.clear(R.id.counter_bubble, ConstraintSet.BOTTOM);
                    holder.constraintSet.applyTo(holder.constraintLayout);
                    holder.myRecycler.setVisibility(View.VISIBLE);
                } else {
                    holder.constraintSet.connect(R.id.cardview_icon, ConstraintSet.BOTTOM, R.id.constraintlayout, ConstraintSet.BOTTOM, 10);
                    holder.constraintSet.connect(R.id.counter_bubble, ConstraintSet.BOTTOM, R.id.constraintlayout, ConstraintSet.BOTTOM, 35);
                    holder.constraintSet.applyTo(holder.constraintLayout);
                    holder.myRecycler.setVisibility(View.GONE);
                }
            }

        });

        item_adapter.setArticoli(p.articoli);
        item_adapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.prodotti.size();
    }

    public void setProdotti(ArrayList<Prodotto> prodotti){
        this.prodotti = prodotti;
        this.tutti_prodotti = prodotti;
    }

    public void filter(ArrayList<String> filtro) {

        if(filtro==null) return;

        ArrayList<Prodotto> filtered = new ArrayList<Prodotto>();
        for(Prodotto p : this.tutti_prodotti) {
            Prodotto new_p = new Prodotto();
            ArrayList<Articolo> items = new ArrayList<Articolo>();
            for (Articolo art : p.articoli) {
                if (filtro.contains(art.owner_id)){
                    items.add(art);
                }
            }
            if(items.size()!=0) {
                new_p.img_url = p.img_url;
                new_p.barcode = p.barcode;
                new_p.name = p.name;
                new_p.quantità = items.size();
                new_p.articoli = items;
                filtered.add(new_p);
            }
        }
        prodotti = filtered;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        boolean expanded;
        TextView myTextView;
        ImageView myImageView;
        CardView myCardView;
        TextView counter;
        ConstraintLayout constraintLayout;
        ConstraintSet constraintSet;
        RecyclerView myRecycler;

        ViewHolder(View itemView) {
            super(itemView);
            myRecycler = itemView.findViewById(R.id.expire_rv);
            myTextView = itemView.findViewById(R.id.prod_name);
            myImageView = itemView.findViewById(R.id.prod_row_icon);
            myCardView = itemView.findViewById(R.id.card);
            counter = itemView.findViewById(R.id.counter);

            constraintLayout = itemView.findViewById(R.id.constraintlayout);
            constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            expanded=false;
            myRecycler.setVisibility(View.GONE);
        }

    }



    //Trasforma l'immagine, arrotondandola , usato da Picasso quando carica un'immagine
    public static class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }
}