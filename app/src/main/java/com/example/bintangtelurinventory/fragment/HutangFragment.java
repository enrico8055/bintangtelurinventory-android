package com.example.bintangtelurinventory.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddPenjualanActivity;
import com.example.bintangtelurinventory.activity.RinciPenjualanActivity;
import com.example.bintangtelurinventory.activity.ScanBarcodeActivity;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterHutang;
import com.example.bintangtelurinventory.modeldata.Penjualan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HutangFragment extends Fragment {
    RecyclerView rv_penjualan;
    EditText et_search;
    FloatingActionButton btn_newpenjualan;
    Button btn_scan;

    //panggil firebase databasenya
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //timer untuk saat user search data
    private Runnable searchRunnable;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hutang, container, false);

        //init
        rv_penjualan = view.findViewById(R.id.rv_penjualan);
        btn_newpenjualan = view.findViewById(R.id.btn_newpenjualan);
        btn_scan = view.findViewById(R.id.btn_scan);
        et_search = view.findViewById(R.id.et_search);
        rv_penjualan.setLayoutManager(new LinearLayoutManager(getContext()));


        RecyclerAdapterHutang adapter = new RecyclerAdapterHutang();
        rv_penjualan.setAdapter(adapter);


        //event click per data di recycler view untuk update datanya
        adapter.setOnItemClickListener(new RecyclerAdapterHutang.OnItemClickListener() {
            @Override
            public void onItemClick(Penjualan penjualan) {
//                //buuka activity
                Intent intent = new Intent(getContext(), RinciPenjualanActivity.class);
                intent.putExtra("idpenjualan", String.valueOf(penjualan.getIdpenjualan()));
                intent.putExtra("tanggaltransaksi", penjualan.getTanggaltransaksi());
                intent.putExtra("idpelanggan", penjualan.getIdpelanggan());
                startActivity(intent);
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable); // Remove previous callback if exists
                }

                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                            db.collection("penjualan")
                                    .whereEqualTo("lunas", "belum")
                                    .orderBy("lunas", Query.Direction.ASCENDING)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            List<Penjualan> data = new ArrayList<Penjualan>();

                                            //sort berdasar nama pelanggan
                                            List<QueryDocumentSnapshot> documents = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : value) {
                                                documents.add(doc);
                                            }
                                            Collections.sort(documents, (doc1, doc2) ->
                                                    doc1.getString("namapelanggan").compareToIgnoreCase(doc2.getString("namapelanggan"))
                                            );


                                            //tampilkan di view
                                            for (QueryDocumentSnapshot document : documents) {
                                                data.add(new Penjualan(document.getId(), document.getData().get("tanggalpenjualan").toString(), document.getData().get("idpelanggan").toString()));
                                            }
                                            adapter.setPenjualans(data);
                                        }
                                    });
                    }
                };

                handler.postDelayed(searchRunnable, 3000); // Delay 3 seconds (3000ms)
            }
        });

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        et_search.setText("SEMUA HUTANG");
//        et_search.setText(formatter.format(date));

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
                startActivityForResult(intent, 8055);
            }
        });

        btn_newpenjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), AddPenjualanActivity.class);
                startActivityForResult(intent, 8055);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 8055 && resultCode == Activity.RESULT_OK && data != null){ //cek untuk memastikan result yang dikirimkan sesuai dengan yang kita butuhkan berdasarkan result code dan request code

            et_search.setText(data.getStringExtra("barcodedata"));

        }
    }
}