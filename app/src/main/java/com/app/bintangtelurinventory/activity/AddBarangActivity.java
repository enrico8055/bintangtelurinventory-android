package com.app.bintangtelurinventory.activity;

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

import com.app.bintangtelurinventory.R;
import com.app.bintangtelurinventory.helper.SharedPrefManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBarangActivity extends AppCompatActivity implements Validator.ValidationListener {
    Button btn_savebarang;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Validator validator;

    @Length(min = 1, trim = true)
    EditText et_namabarang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barang);

        //init
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Tambah Barang" + "</font>"));

        validator = new Validator(this);
        validator.setValidationListener(this);
        btn_savebarang = findViewById(R.id.btn_savebarang);
        et_namabarang = findViewById(R.id.et_namabarang);

        btn_savebarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddBarangActivity.this);
                alert.setTitle("Tambah Barang");
                alert.setMessage("Cek lagi, yakin input barang ini?");
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
        Date tgl = new Date();
        com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(tgl);
        data.put("timestamp", timestamp);
        data.put("namabarang", et_namabarang.getText().toString());
        data.put("uuid", SharedPrefManager.getInstance(AddBarangActivity.this).getUserId());
        // Add a new document with a generated ID
        db.collection("barang")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddBarangActivity.this, "Berhasil Tambah Barang!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Toast.makeText(this, "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
    }
}