package com.example.bintangtelurinventory.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddBarangActivity;
import com.example.bintangtelurinventory.activity.EditPelangganActivity;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterBarang;
import com.example.bintangtelurinventory.helper.SharedPrefManager;
import com.example.bintangtelurinventory.modeldata.Barang;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BarangFragment extends Fragment {
    EditText et_search;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView rv_barang;
    FloatingActionButton btn_newbarang;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barang, container, false);

        //INIT
        rv_barang = view.findViewById(R.id.rv_barang);
        btn_newbarang = view.findViewById(R.id.btn_newbarang);
        et_search = view.findViewById(R.id.et_search);
        rv_barang.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapterBarang adapter = new RecyclerAdapterBarang();
        rv_barang.setAdapter(adapter);

        //ambil semua data barang
        db.collection("barang").whereIn("uuid", Arrays.asList(SharedPrefManager.getInstance(getActivity()).getUserId())).orderBy("namabarang", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value == null || value.isEmpty()) {
                            adapter.setBarangs(new ArrayList<>());
                            return;
                        }

                        List<Barang> data = new ArrayList<Barang>();
                        for (QueryDocumentSnapshot document : value) {
                            data.add(new Barang(document.getId(), document.getData().get("namabarang").toString()));
                        }
                        adapter.setBarangs(data);
                    }
                });
        //event
        //memberikan reaksi terhadap item item di recycler view, misal item bisa di drag /  bisa di swipe kana / kiri
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //DELETE
                //jika item di swipe maka
                Barang selectData = adapter.getBarangs(viewHolder.getAdapterPosition());
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Hapus");
                alert.setMessage("Setelah di hapus barang tidak akan kembali, yakin hapus?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //GET DATA RINCI PENJUALAN
                        //untuk cek apaakah pelanggan sudah punya transaksi rinci penjualan
                        db.collection("rincipenjualan").whereEqualTo("idbarang", selectData.getIdbarang())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){
                                            Toast.makeText(getContext(), "Gagal hapus barang karena barang sudah ada transaksi!", Toast.LENGTH_SHORT).show();
                                            adapter.notifyDataSetChanged();
                                        }else{
                                            //DELETE DATA PENJUALAN
                                            db.collection("barang").document(selectData.getIdbarang())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getContext(), "Berhasil hapus barang!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                        }
                                                    });
                                        }
                                    }
                                });

                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }).attachToRecyclerView(rv_barang); //pasang ke recycler view

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                db.collection("barang").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                List<Barang> data = new ArrayList<Barang>();
                                Log.d("xxxx", value.toString());
                                for (QueryDocumentSnapshot document : value) {
                                    if(document.getId().contains(et_search.getText().toString()) || document.getString("namabarang").toLowerCase(Locale.ROOT).contains(et_search.getText().toString().toLowerCase()))
                                        data.add(new Barang(document.getId(), document.getData().get("namabarang").toString()));
                                }
                                adapter.setBarangs(data);
                            }
                        });
            }
        });

        btn_newbarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), AddBarangActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}