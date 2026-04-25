package com.app.bintangtelurinventory.fragment;

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
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.bintangtelurinventory.R;
import com.app.bintangtelurinventory.activity.AddPenjualanActivity;
import com.app.bintangtelurinventory.activity.RinciPenjualanActivity;
import com.app.bintangtelurinventory.activity.ScanBarcodeActivity;
import com.app.bintangtelurinventory.adapter.RecyclerAdapterHutang;
import com.app.bintangtelurinventory.helper.SharedPrefManager;
import com.app.bintangtelurinventory.modeldata.Penjualan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
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

    ProgressBar progressBar;


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
        progressBar = view.findViewById(R.id.progressBar);

        btn_newpenjualan.setVisibility(View.INVISIBLE);


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

                rv_penjualan.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        db.collection("penjualan")
                                .whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
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
                                            String tanggal = document.getData().get("tanggalpenjualan") != null ? document.getData().get("tanggalpenjualan").toString() : "";
                                            String idPelanggan = document.getData().get("idpelanggan") != null ? document.getData().get("idpelanggan").toString() : "";
                                            String namaPelanggan = document.getData().get("namapelanggan") != null ? document.getData().get("namapelanggan").toString() : "";
                                            String total = document.getData().get("total") != null ? document.getData().get("total").toString() : "0";
                                            String titip = document.getData().get("titip") != null ? document.getData().get("titip").toString() : "0";

                                            data.add(new Penjualan(document.getId(), tanggal, idPelanggan, namaPelanggan, total, titip));
                                        }
                                        adapter.setPenjualans(data);

                                        rv_penjualan.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
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

        if (requestCode == 8055 && resultCode == Activity.RESULT_OK && data != null) { //cek untuk memastikan result yang dikirimkan sesuai dengan yang kita butuhkan berdasarkan result code dan request code

            et_search.setText(data.getStringExtra("barcodedata"));

        }
    }
}