package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditPelangganActivity extends AppCompatActivity {
    EditText et_nama, et_alamat, et_notelp;
    Button btn_update, btn_delete;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pelanggan);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Edit Pelanggan" + "</font>"));

        String idpelanggan = getIntent().getStringExtra("idpelanggan");
        String nama = getIntent().getStringExtra("nama");
        String notelp = getIntent().getStringExtra("notelp");
        String alamat = getIntent().getStringExtra("alamat");

        et_nama = findViewById(R.id.et_nama);
        et_alamat = findViewById(R.id.et_alamat);
        et_notelp = findViewById(R.id.et_notel);
        btn_update = findViewById(R.id.btn_update);
        btn_delete = findViewById(R.id.btn_del);

        et_nama.setText(nama);
        et_alamat.setText(alamat);
        et_notelp.setText(notelp);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(EditPelangganActivity.this);
                alert.setTitle("Hapus");
                alert.setMessage("Setelah di hapus pelanggan tidak akan kembali, yakin hapus?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //GET DATA PENJUALAN
                        //untuk cek apaakah pelanggan sudah punya transaksi penjualan
                        db.collection("penjualan").whereEqualTo("idpelanggan", idpelanggan)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){
                                            Toast.makeText(EditPelangganActivity.this, "Gagal hapus pelanggan karena pelanggan sudah ada transaksi!", Toast.LENGTH_SHORT).show();
                                        }else{
                                            //DELETE DATA PENJUALAN
                                            db.collection("pelanggan").document(idpelanggan)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(EditPelangganActivity.this, "Berhasil hapus pelanggan!", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            finish();
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
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_alamat.length() > 0 && et_nama.length() > 0 && et_notelp.length() > 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(EditPelangganActivity.this);
                    alert.setTitle("Ubah Pelanggan");
                    alert.setMessage("Cek lagi, yakin ubah pelanggan ini?");
                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //UPDATE PELANGGAN
                            Map<String, Object> data = new HashMap<>();
                            data.put("nama", et_nama.getText().toString());
                            data.put("alamat", et_alamat.getText().toString());
                            data.put("notelp", et_notelp.getText().toString());
                            // Add a new document with a generated ID
                            db.collection("pelanggan").document(idpelanggan.toString())
                                    .update(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(EditPelangganActivity.this, "Berhasil Ubah Pelanggan!", Toast.LENGTH_SHORT).show();
                                            finish();
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
                }else {
                    Toast.makeText(EditPelangganActivity.this, "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}