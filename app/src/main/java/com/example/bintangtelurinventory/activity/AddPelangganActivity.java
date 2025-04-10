package com.example.bintangtelurinventory.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPelangganActivity extends AppCompatActivity implements Validator.ValidationListener{
    Button btn_savepelanggan;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Validator validator;

    @Length(min=1, trim = true)
    EditText et_nama, et_alamat, et_notelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pelanggan);

        //init
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Tambah Pelanggan" + "</font>"));

        validator = new Validator(this);
        validator.setValidationListener(this);
        btn_savepelanggan = findViewById(R.id.btn_savebarang);
        et_nama = findViewById(R.id.et_namapelang);
        et_alamat = findViewById(R.id.et_namabarang);
        et_notelp = findViewById(R.id.et_notelp);

        btn_savepelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddPelangganActivity.this);
                alert.setTitle("Tambah Pelanggan");
                alert.setMessage("Cek lagi, yakin input pelanggan ini?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        validator.validate();
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

    }

    @Override
    public void onValidationSucceeded() {
        //INSERT PELANGGAN
        Map<String, Object> data = new HashMap<>();
        data.put("nama", et_nama.getText().toString());
        data.put("alamat", et_alamat.getText().toString());
        data.put("notelp", et_notelp.getText().toString());
        // Add a new document with a generated ID
        db.collection("pelanggan")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddPelangganActivity.this, "Berhasil Tambah Pelanggan!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
            Toast.makeText(this, "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
    }
}