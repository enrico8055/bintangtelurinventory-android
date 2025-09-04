package com.example.bintangtelurinventory.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.bintangtelurinventory.activity.EditPelangganActivity;
import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddPelangganActivity;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterPelanggan;
import com.example.bintangtelurinventory.helper.SharedPrefManager;
import com.example.bintangtelurinventory.modeldata.Pelanggan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PelangganFragment extends Fragment {
    EditText et_search;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView rv_pelanggan;
    FloatingActionButton btn_newpelanggan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pelanggan, container, false);

        //INIT
        rv_pelanggan = view.findViewById(R.id.rv_pelanggan);
        btn_newpelanggan = view.findViewById(R.id.btn_newpelanggan);
        et_search = view.findViewById(R.id.et_search);
        rv_pelanggan.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapterPelanggan adapter = new RecyclerAdapterPelanggan();
        rv_pelanggan.setAdapter(adapter);

        //ambil semua data pelanggan
        db.collection("pelanggan").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<Pelanggan> data = new ArrayList<Pelanggan>();

                        for (QueryDocumentSnapshot document : value) {
                            data.add(new Pelanggan(document.getId(), document.getData().get("nama").toString(), document.getData().get("alamat").toString(), document.getData().get("notelp").toString()));
                        }
                        adapter.setPelanggans(data);
                    }
                });

        //EVENT
        //event click per data di recycler view untuk update datanya
        adapter.setOnItemClickListener(new RecyclerAdapterPelanggan.OnItemClickListener() {
            @Override
            public void onItemClick(Pelanggan pelanggan) {
                //buuka activity
                Intent intent = new Intent(getContext(), EditPelangganActivity.class);
                intent.putExtra("idpelanggan", String.valueOf(pelanggan.idpelanggan.toString()));
                intent.putExtra("nama", pelanggan.nama.toString());
                intent.putExtra("notelp", pelanggan.notelp.toString());
                intent.putExtra("alamat", pelanggan.alamat.toString());
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
                //ambil semua data pelanggan
                db.collection("pelanggan").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                List<Pelanggan> data = new ArrayList<Pelanggan>();

                                for (QueryDocumentSnapshot document : value) {
                                    if(document.getId().contains(et_search.getText().toString() ) || document.getString("nama").toString().toLowerCase(Locale.ROOT).contains(et_search.getText().toString().toLowerCase() ))
                                        data.add(new Pelanggan(document.getId(), document.getData().get("nama").toString(), document.getData().get("alamat").toString(), document.getData().get("notelp").toString()));
                                }
                                adapter.setPelanggans(data);
                            }
                        });
            }
        });

        btn_newpelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), AddPelangganActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}