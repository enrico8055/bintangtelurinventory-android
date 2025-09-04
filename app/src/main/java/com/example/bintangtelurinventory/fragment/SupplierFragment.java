package com.example.bintangtelurinventory.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddBarangActivity;
import com.example.bintangtelurinventory.activity.AddSupplierActivity;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterBarang;
import com.example.bintangtelurinventory.adapter.RecyclerAdapterSupplier;
import com.example.bintangtelurinventory.helper.SharedPrefManager;
import com.example.bintangtelurinventory.modeldata.Barang;
import com.example.bintangtelurinventory.modeldata.Supplier;
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
import java.util.List;
import java.util.Locale;

public class SupplierFragment extends Fragment {
    EditText et_search;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView rv_supplier;
    FloatingActionButton btn_newsupp;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_supplier, container, false);

        //INIT
        rv_supplier = view.findViewById(R.id.rv_barang);
        btn_newsupp = view.findViewById(R.id.btn_newbarang);
        et_search = view.findViewById(R.id.et_search);
        rv_supplier.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapterSupplier adapter = new RecyclerAdapterSupplier();
        rv_supplier.setAdapter(adapter);

        //ambil semua data barang
        db.collection("supplier").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<Supplier> data = new ArrayList<Supplier>();
                        for (QueryDocumentSnapshot document : value) {
                            data.add(new Supplier(document.getId(), document.getData().get("nama").toString()));
                        }
                        adapter.setSuppliers(data);
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
                Supplier selectData = adapter.getSuppliers(viewHolder.getAdapterPosition());
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Hapus");
                alert.setMessage("Setelah di hapus supplier tidak akan kembali, yakin hapus?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //GET DATA PEMBELIAN
                        //untuk cek apaakah pelanggan sudah punya transaksi rinci penjualan
                        db.collection("pembelian").whereEqualTo("idsupplier", selectData.getIdsupplier())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){
                                            Toast.makeText(getContext(), "Gagal hapus supplier karena supplier sudah ada transaksi!", Toast.LENGTH_SHORT).show();
                                            adapter.notifyDataSetChanged();
                                        }else{
                                            //DELETE DATA SUPP
                                            db.collection("supplier").document(selectData.getIdsupplier())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getContext(), "Berhasil hapus supplier!", Toast.LENGTH_SHORT).show();
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
        }).attachToRecyclerView(rv_supplier); //pasang ke recycler view

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                db.collection("supplier").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                List<Supplier> data = new ArrayList<Supplier>();
                                for (QueryDocumentSnapshot document : value) {
                                    if(document.getId().contains(et_search.getText().toString()) || document.getString("nama").toLowerCase(Locale.ROOT).contains(et_search.getText().toString().toLowerCase()))
                                        data.add(new Supplier(document.getId(), document.getData().get("nama").toString()));
                                }
                                adapter.setSuppliers(data);
                            }
                        });
            }
        });

        btn_newsupp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buuka activity
                Intent intent = new Intent(getContext(), AddSupplierActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}