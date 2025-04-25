package com.example.bintangtelurinventory.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddPembelianActivity;
import com.example.bintangtelurinventory.activity.AddPenjualanActivity;
import com.example.bintangtelurinventory.activity.RinciPembelianActivity;
import com.example.bintangtelurinventory.activity.RinciPenjualanActivity;
import com.example.bintangtelurinventory.activity.ScanBarcodeActivity;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterPembelian;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterPenjualan;
import com.example.bintangtelurinventory.modeldata.Pembelian;
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

public class PembelianFragment extends Fragment {
    RecyclerView rv_pembelian;
    EditText et_search;
    FloatingActionButton btn_newpembelian;
    Button btn_scan;

    ProgressBar progressBar;

    //panggil firebase databasenya
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //timer untuk saat user search data
    private Runnable searchRunnable;
    private Handler handler = new Handler();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pembelian, container, false);

        //init
        rv_pembelian = view.findViewById(R.id.rv_pembelian);
        btn_newpembelian = view.findViewById(R.id.btn_newpembelian);
        btn_scan = view.findViewById(R.id.btn_scan);
        et_search = view.findViewById(R.id.et_search);
        progressBar = view.findViewById(R.id.progressBar);
        rv_pembelian.setLayoutManager(new LinearLayoutManager(getContext()));


        RecyclerAdapterPembelian adapter = new RecyclerAdapterPembelian();
        rv_pembelian.setAdapter(adapter);


        //event click per data di recycler view untuk update datanya
        adapter.setOnItemClickListener(new RecyclerAdapterPembelian.OnItemClickListener() {
            @Override
            public void onItemClick(Pembelian pembelian) {
//                //buuka activity
                Intent intent = new Intent(getContext(), RinciPembelianActivity.class);
                intent.putExtra("idpembelian", String.valueOf(pembelian.getIdpembelian()));
                intent.putExtra("tanggaltransaksi", pembelian.getTanggaltransaksi());
                intent.putExtra("idsupplier", pembelian.getIdsupplier());
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
                rv_pembelian.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (et_search.getText().toString().contains("/")) {//ambil semua data pembelian berdasar tanggal
                            db.collection("pembelian")
                                    .whereGreaterThanOrEqualTo("tanggalpembelian", et_search.getText().toString()) // Filter by start date
                                    .whereLessThanOrEqualTo("tanggalpembelian", et_search.getText().toString())
                                    .orderBy("tanggalpembelian", Query.Direction.ASCENDING)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            List<Pembelian> data = new ArrayList<Pembelian>();

                                            //sort berdasar nama supplier
                                            List<QueryDocumentSnapshot> documents = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : value) {
                                                documents.add(doc);
                                            }
                                            Collections.sort(documents, (doc1, doc2) ->
                                                    doc1.getString("namasupplier").compareToIgnoreCase(doc2.getString("namasupplier"))
                                            );


                                            //tampilkan di view
                                            for (QueryDocumentSnapshot document : documents) {
                                                data.add(new Pembelian(document.getId(), document.getData().get("tanggalpembelian").toString(), document.getData().get("idsupplier").toString()));
                                            }
                                            adapter.setPembelians(data);

                                            rv_pembelian.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }else{ //ambil data pembelian berdasar id document
                            db.collection("pembelian")
                                    .whereEqualTo(FieldPath.documentId(), et_search.getText().toString())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            List<Pembelian> data = new ArrayList<Pembelian>();

                                            //sort berdasar nama supplier
                                            List<QueryDocumentSnapshot> documents = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : value) {
                                                documents.add(doc);
                                            }
                                            Collections.sort(documents, (doc1, doc2) ->
                                                    doc1.getString("namasupplier").compareToIgnoreCase(doc2.getString("namasupplier"))
                                            );


                                            //tampilkan di view
                                            for (QueryDocumentSnapshot document : documents) {
                                                data.add(new Pembelian(document.getId(), document.getData().get("tanggalpembelian").toString(), document.getData().get("idsupplier").toString()));
                                            }
                                            adapter.setPembelians(data);

                                            rv_pembelian.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                };

                handler.postDelayed(searchRunnable, 3000); // Delay 3 seconds (3000ms)


            }
        });

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        et_search.setText(formatter.format(date));

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), ScanBarcodeActivity.class);
                startActivityForResult(intent, 8055);
            }
        });

        btn_newpembelian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), AddPembelianActivity.class);
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