package com.example.bintangtelurinventory.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.modeldata.Penjualan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RecyclerAdapterPenjualan extends RecyclerView.Adapter<RecyclerAdapterPenjualan.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<Penjualan> penjualans = new ArrayList<>();
    private OnItemClickListener listener;
    AtomicReference<Double> totalHarga = new AtomicReference<>(0.0);



    //constructor class ini agar konstructor bisa menangkap data saat pembentukan object adapter nanti
    public RecyclerAdapterPenjualan() {
    }


    //setter data
    public void setPenjualans(List<Penjualan> penjualans) {
        this.penjualans = penjualans;
        notifyDataSetChanged(); //method ini akan trigger jika ada data berubah di database
    }



    //getter data
    public Penjualan getNotes(int position){
        return penjualans.get(position); //kembalikan data note berdasarkan index yang dikitim
    }



    //buat kerangka/prototype item2nya sesuai dengan desain card yang sudah kita buat di folder res - layout - card_design_for_recycler_item.xml
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_namapelanggan;
        private TextView tv_tanggaltransaksi;
        private TextView tv_idpenjualan;
        private TextView tv_totalharga;

        public ViewHolder(@NonNull View itemView) { //constructor
            super(itemView);

            //panggil item item di layout card_design_for_recycler_item.xml kita
            tv_namapelanggan = itemView.findViewById(R.id.tv_idbeli);
            tv_tanggaltransaksi = itemView.findViewById(R.id.tv_tanggalpembelian);
            tv_idpenjualan = itemView.findViewById(R.id.tv_idpembelian);
            tv_totalharga = itemView.findViewById(R.id.tv_totalharga);

            //biar kalo fungsi onclicklistener(yang kita buat sendiri di bawah) adapter ini dipanggil akan mengembalikan data berupa data note yang diklik saat ini
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener!=null&&getAdapterPosition()!=RecyclerView.NO_POSITION){
                        listener.onItemClick(penjualans.get(getAdapterPosition()));
                    }
                }
            });

        }
    }



    //ini fungsi untuk memanggil layout / bentuk desain ui card_design_for_recycler_item.xmlnya
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_design_for_recycler_item_penjualan, parent, false);

        return new ViewHolder(view);
    }



    //fungsi ini untuk menempelkan data yang dikirim ke class ini saat nanti membentuk object adapter ke card_design_for_recycler_item.xml yang sudah kita panggil fungsi oncreateviewholder
    //selain menempelkan data disini kita bisa tempelkan juga fungsi event click
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {
        Penjualan curr = penjualans.get(position);

        //ambil semua data rinci penjualan untuk dapat total harga
        db.collection("rincipenjualan").whereEqualTo("idpenjualan", curr.getIdpenjualan().trim())
            .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document1 : task.getResult()) {
                            Double totalPerItem = Double.valueOf(document1.getString("jumlah")) * Double.valueOf(document1.getString("hargasatuan"));
                            totalHarga.updateAndGet(v -> v + totalPerItem);
                        }
                        //UNTUK KASI SEPARATOR TITIK RUPIAH
                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                        formatRp.setCurrencySymbol("Rp. ");
                        formatRp.setMonetaryDecimalSeparator(',');
                        formatRp.setGroupingSeparator('.');
                        kursIndonesia.setDecimalFormatSymbols(formatRp);
                        holder.tv_totalharga.setText(kursIndonesia.format(Double.valueOf(String.valueOf(totalHarga))));
                        totalHarga.set(0.0);

                    }
                });

        //ambil nama pelanggan bedasarkan id pelanggan
        db.collection("pelanggan").document(curr.getIdpelanggan().trim())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //isikan data ke item textviewnya
                            holder.tv_tanggaltransaksi.setText(curr.getTanggaltransaksi().toString());
                            holder.tv_namapelanggan.setText(documentSnapshot.getString("nama"));
                            holder.tv_idpenjualan.setText(curr.getIdpenjualan());
                    }
                });
    }



    //ini fungsi untuk mendapatkan jumlah data yang akan ditampilkan
    @Override
    public int getItemCount() {
        return penjualans.size();
    }




    //fungsi biar adapter ini punya method OnItemClickListener biar setiap datanya bisa diklik
    public interface  OnItemClickListener{
        void onItemClick(Penjualan penjualan);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}

