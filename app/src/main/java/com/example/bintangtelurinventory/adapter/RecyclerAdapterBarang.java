package com.example.bintangtelurinventory.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.modeldata.Barang;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RecyclerAdapterBarang extends RecyclerView.Adapter<RecyclerAdapterBarang.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<Barang> Barangs = new ArrayList<>();
    private OnItemClickListener listener;
    AtomicReference<Integer> totalHarga = new AtomicReference<>(0);



    //constructor class ini agar konstructor bisa menangkap data saat pembentukan object adapter nanti
    public RecyclerAdapterBarang() {
    }


    //setter data
    public void setBarangs(List<Barang> Barangs) {
        this.Barangs = Barangs;
        notifyDataSetChanged(); //method ini akan trigger jika ada data berubah di database
    }



    //getter data
    public Barang getBarangs(int position){
        return Barangs.get(position); //kembalikan data note berdasarkan index yang dikitim
    }



    //buat kerangka/prototype item2nya sesuai dengan desain card yang sudah kita buat di folder res - layout - card_design_for_recycler_item.xml
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_namabrg;
        private TextView tv_idbrg;
        public ViewHolder(@NonNull View itemView) { //constructor
            super(itemView);

            //panggil item item di layout card_design_for_recycler_item.xml kita
            tv_namabrg = itemView.findViewById(R.id.tv_namabrg);
            tv_idbrg = itemView.findViewById(R.id.tv_idbrg);

            //biar kalo fungsi onclicklistener(yang kita buat sendiri di bawah) adapter ini dipanggil akan mengembalikan data berupa data note yang diklik saat ini
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null&&getAdapterPosition()!=RecyclerView.NO_POSITION){
                        listener.onItemClick(Barangs.get(getAdapterPosition()));
                    }
                }
            });

        }
    }



    //ini fungsi untuk memanggil layout / bentuk desain ui card_design_for_recycler_item.xmlnya
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_design_for_recycler_item_barang, parent, false);

        return new ViewHolder(view);
    }



    //fungsi ini untuk menempelkan data yang dikirim ke class ini saat nanti membentuk object adapter ke card_design_for_recycler_item.xml yang sudah kita panggil fungsi oncreateviewholder
    //selain menempelkan data disini kita bisa tempelkan juga fungsi event click
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {
        Barang curr = Barangs.get(position);

        holder.tv_namabrg.setText(curr.getNamabarang());
        holder.tv_idbrg.setText(curr.getIdbarang());
    }



    //ini fungsi untuk mendapatkan jumlah data yang akan ditampilkan
    @Override
    public int getItemCount() {
        return Barangs.size();
    }




    //fungsi biar adapter ini punya method OnItemClickListener biar setiap datanya bisa diklik
    public interface  OnItemClickListener{
        void onItemClick(Barang Barang);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}

