package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bintangtelurinventory.dialogfragment.AddRinciPembelianDialogFragment;
import com.example.bintangtelurinventory.dialogfragment.AddRinciPenjualanDialogFragment;
import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.adapter.AdapterPdfDocument;
import com.example.bintangtelurinventory.modeldata.Pelanggan;
import com.example.bintangtelurinventory.modeldata.Supplier;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPembelianActivity extends AppCompatActivity {
    EditText et_tanggal;
    Button  btn_addrincipembelian;
    ImageButton btn_clear, btn_date;
    String idsupplier, namasupplier;
    Spinner sp_pelanggan;
    DatePickerDialog datePickerDialog;
    FloatingActionButton btn_save;
    ListView lv_rincibeli;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<ArrayList<String>> rincipembelian= new ArrayList<>();
    ArrayList<String> displayRincipembelian = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    Integer totalHarga = 0;
    Integer totalJumlah = 0;
    CheckBox cb_lunas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pembelian);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Tambah Pembelian" + "</font>"));


        //INIT
        et_tanggal = (EditText) findViewById(R.id.editTextDate);
        btn_addrincipembelian = (Button) findViewById(R.id.btn_addrincipenjualan);
        sp_pelanggan = (Spinner) findViewById(R.id.sp_barang);
        btn_date = (ImageButton) findViewById(R.id.btn_date);
        btn_save = findViewById(R.id.btn_save);
        btn_clear = findViewById(R.id.btn_clear);
        lv_rincibeli = findViewById(R.id.lv_rincijual);
        cb_lunas = findViewById(R.id.cb_lunas);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,displayRincipembelian );

//        btn_date.setEnabled(false);
        lv_rincibeli.setAdapter(arrayAdapter);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        et_tanggal.setText(formatter.format(date));

        //ambil semua data supplier untuk isi spinner
        db.collection("supplier").orderBy("nama", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Supplier> data = new ArrayList<Supplier>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                data.add(new Supplier(document.getId(), document.getString("nama")));
                            }
                            ArrayAdapter<Supplier> spinnerAdapter = new ArrayAdapter<Supplier>(AddPembelianActivity.this,android.R.layout.simple_spinner_item, data);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sp_pelanggan.setAdapter(spinnerAdapter);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        //EVENT
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddPembelianActivity.this);
                alert.setTitle("Hapus List Barang");
                alert.setMessage("Yakin ingin hapus daftar barang saat ini?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rincipembelian.clear();
                        displayRincipembelian.clear();
                        arrayAdapter.notifyDataSetChanged();
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

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddPembelianActivity.this);
                alert.setTitle("Tambah Pembelian");
                alert.setMessage("Cek lagi, yakin input pembelian ini?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!rincipembelian.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date tgl = null;
                            try {
                                tgl = sdf.parse(et_tanggal.getText().toString());
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            SimpleDateFormat firestoreFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                            String tglFormatted = firestoreFormat.format(tgl);
                            Timestamp timestamp = new Timestamp(tgl);

                            //INSERT PEMBELIAN
                            Map<String, Object> data = new HashMap<>();
                            data.put("idsupplier", idsupplier);
                            data.put("namasupplier", namasupplier);
                            data.put("tanggalpembelian", formatDate(et_tanggal.getText().toString()));
                            data.put("tglpembelianformatted", tglFormatted);
                            data.put("timestamp", timestamp);
                            // Add a new document with a generated ID
                            db.collection("pembelian")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference1) {
                                            //INSERT RINCI PEMBELIAN
                                            rincipembelian.forEach((e) -> {
                                                Map<String, Object> data1 = new HashMap<>();
                                                data1.put("hargasatuan", e.get(0));
                                                data1.put("idbarang", e.get(1));
                                                data1.put("idpembelian", documentReference1.getId().toString());
                                                data1.put("jumlah", e.get(2));
                                                data1.put("namabarang", e.get(3));
                                                data1.put("satuan", e.get(4));
                                                // Add a new document with a generated ID
                                                db.collection("rincipembelian")
                                                        .add(data1)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Toast.makeText(AddPembelianActivity.this, "Berhasil Menambahkan Pembelian! Mencetak Invoice ...", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();
                                                                rincipembelian.clear();
                                                                displayRincipembelian.clear();
                                                                arrayAdapter.notifyDataSetChanged();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                finish();
                                                            }
                                                        });
                                            });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            finish();
                                        }
                                    });

                        }else {
                            Toast.makeText(AddPembelianActivity.this, "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
                        }
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

        btn_addrincipembelian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //munculkan dialog fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                AddRinciPembelianDialogFragment dialogFragment = new AddRinciPembelianDialogFragment();
                dialogFragment.setCancelable(false); //biar kalo dipencet diloar dialog tidak terclose dialognya
                dialogFragment.show(fragmentManager, "AddRinciPembelianDialogFragment");
            }
        });

        sp_pelanggan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Supplier selectedData = (Supplier) parent.getSelectedItem();
                idsupplier = selectedData.idsupplier;
                namasupplier = selectedData.nama;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // perform click event on edit text
        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(AddPembelianActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                et_tanggal.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    public static String formatDate(String dateStr) {
        try {
            // Parse string input ke Date
            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);

            // Format ulang dengan format dd/MM/yyyy
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void ambilDataDariDialogFragment(ArrayList<String> data){
        rincipembelian.add(data);
        String temp = data.get(3) + " " + data.get(2) + data.get(4) + " @ " + NumberFormat.getNumberInstance(new Locale("in", "ID")).format(Double.parseDouble(data.get(0))) + " :: Rp. " + NumberFormat.getNumberInstance(new Locale("in", "ID")).format(Double.parseDouble(String.valueOf(Double.valueOf(data.get(2)) * Double.valueOf(data.get(0)))));
        displayRincipembelian.add(temp);
        arrayAdapter.notifyDataSetChanged();
    }
}