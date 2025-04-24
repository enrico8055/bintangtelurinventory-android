package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.bintangtelurinventory.dialogfragment.AddRinciPenjualanDialogFragment;
import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.adapter.AdapterPdfDocument;
import com.example.bintangtelurinventory.modeldata.Pelanggan;
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPenjualanActivity extends AppCompatActivity {
    EditText et_tanggal, et_titip2;
    Button  btn_addrincipenjualan;
    ImageButton btn_clear, btn_date;
    String idpelanggan, namapelanggan;
    Spinner sp_pelanggan;
    DatePickerDialog datePickerDialog;
    FloatingActionButton btn_save;
    ListView lv_rincijual;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<ArrayList<String>> rinciPenjualan = new ArrayList<>();
    ArrayList<String> displayRinciPenjualan = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    Double totalHarga = 0.0;
    Double totalJumlah = 0.0;
    CheckBox cb_lunas;
    Image imageLogo;
    View vl_catatan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_penjualan);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Tambah Penjualan" + "</font>"));


        //INIT
        et_tanggal = (EditText) findViewById(R.id.editTextDate);
        btn_addrincipenjualan = (Button) findViewById(R.id.btn_addrincipenjualan);
        sp_pelanggan = (Spinner) findViewById(R.id.sp_barang);
        btn_date = (ImageButton) findViewById(R.id.btn_date);
        btn_save = findViewById(R.id.btn_save);
        btn_clear = findViewById(R.id.btn_clear);
        lv_rincijual = findViewById(R.id.lv_rincijual);
        et_titip2 = findViewById(R.id.et_titip2);
        vl_catatan = findViewById(R.id.vl_catatan);
        cb_lunas = findViewById(R.id.cb_lunas);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,displayRinciPenjualan );

        lv_rincijual.setAdapter(arrayAdapter);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        et_tanggal.setText(formatter.format(date));

        //ambil semua data pelanggan untuk isi spinner
        db.collection("pelanggan").orderBy("nama", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Pelanggan> data = new ArrayList<Pelanggan>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                data.add(new Pelanggan(document.getId(), document.getString("nama")));
                            }
                            ArrayAdapter<Pelanggan> spinnerAdapter = new ArrayAdapter<Pelanggan>(AddPenjualanActivity.this,android.R.layout.simple_spinner_item, data);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sp_pelanggan.setAdapter(spinnerAdapter);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        //EVENT
        cb_lunas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(cb_lunas.isChecked()){
                    vl_catatan.setVisibility(View.INVISIBLE);
                }else{
                    vl_catatan.setVisibility(View.VISIBLE);
                }
            }
        });

        et_titip2.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    et_titip2.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[Rp,.\\s]", "");

                    try {
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                        formatter.setMaximumFractionDigits(0); // remove decimal if needed
                        String formatted = formatter.format(parsed);

                        current = formatted;
                        et_titip2.setText(formatted);
                        et_titip2.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    et_titip2.addTextChangedListener(this);
                }
            }
        });


        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddPenjualanActivity.this);
                alert.setTitle("Hapus List Barang");
                alert.setMessage("Yakin ingin hapus daftar barang saat ini?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rinciPenjualan.clear();
                        displayRinciPenjualan.clear();
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
            @SuppressWarnings("all")
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AddPenjualanActivity.this);
                alert.setTitle("Tambah Penjualan");
                alert.setMessage("Cek lagi, yakin input penjualan ini?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!rinciPenjualan.isEmpty()) {
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

                            //INSERT PENJUALAN
                            Map<String, Object> data = new HashMap<>();
                            data.put("idpelanggan", idpelanggan);
                            data.put("namapelanggan", namapelanggan);
                            data.put("tanggalpenjualan", formatDate(et_tanggal.getText().toString()));
                            data.put("tglpenjualanformatted", tglFormatted);
                            data.put("timestamp", timestamp);
                            data.put("titip", et_titip2.getText().toString().replaceAll("[Rp,.\\s]", ""));
                            if (cb_lunas.isChecked()) {
                                data.put("lunas", "ya");
                            } else {
                                data.put("lunas", "belum");
                            }
                            // Add a new document with a generated ID
                            db.collection("penjualan")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference1) {
                                            //INSERT RINCI PENJUALAN
                                            rinciPenjualan.forEach((e) -> {
                                                Map<String, Object> data1 = new HashMap<>();
                                                data1.put("hargasatuan", e.get(0));
                                                data1.put("idbarang", e.get(1));
                                                data1.put("idpenjualan", documentReference1.getId().toString());
                                                data1.put("jumlah", e.get(2));
                                                data1.put("namabarang", e.get(3));
                                                data1.put("satuan", e.get(4));
                                                data1.put("timestamp", timestamp);
                                                // Add a new document with a generated ID
                                                db.collection("rincipenjualan")
                                                        .add(data1)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                Toast.makeText(AddPenjualanActivity.this, "Berhasil Menambahkan Penjualan! Mencetak Invoice ...", Toast.LENGTH_SHORT).show();


                                                                //PRINT NOTA
                                                                //REQUEST PERMISSION BLUETOOTH
                                                                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(AddPenjualanActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                                                                    ActivityCompat.requestPermissions(AddPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 201);
                                                                } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(AddPenjualanActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                                                                    ActivityCompat.requestPermissions(AddPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 201);
                                                                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(AddPenjualanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                                                    ActivityCompat.requestPermissions(AddPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 201);
                                                                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(AddPenjualanActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                                                    ActivityCompat.requestPermissions(AddPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 201);
                                                                } else {
                                                                    AlertDialog.Builder alert = new AlertDialog.Builder(AddPenjualanActivity.this);
                                                                    alert.setTitle("Cetak Nota");
                                                                    alert.setMessage("Yakin Cetak Nota? Pastikan Sudah Connect Dengan Printer Bluetooth!");
                                                                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                            if(false){
                                                                                Toast.makeText(AddPenjualanActivity.this, "Printer Bluetooth Tidak Terdeteksi!!", Toast.LENGTH_SHORT).show();
                                                                            }else {
                                                                                Toast.makeText(AddPenjualanActivity.this, "Printing...", Toast.LENGTH_SHORT).show();
                                                                                //PRINT NOTA DIRECT KE PRINTER ESC/POS
                                                                                db.collection("rincipenjualan").whereEqualTo("idpenjualan", documentReference1.getId().trim())
                                                                                        .get()
                                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    EscPosPrinter printer = null;
                                                                                                    try {
                                                                                                        printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
                                                                                                    } catch (EscPosConnectionException e) {
                                                                                                        throw new RuntimeException(e);
                                                                                                    }

                                                                                                    //SIAPKAN LAYOUT NOTA
                                                                                                    String layout =
                                                                                                            "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, AddPenjualanActivity.this.getResources().getDrawableForDensity(R.drawable.logonota, DisplayMetrics.DENSITY_XXXHIGH)) + "</img> \n" +
                                                                                                                    "[L]\n" +
                                                                                                                    "[C]<u><font size='big'>Bintang Telur</font></u>\n" +
                                                                                                                    "[C]Jl. Puri Cipageran Indah 2, Cimahi \n" +
                                                                                                                    "[L]<b><font size='tall'>" + et_tanggal.getText().toString() + "</font></b>[R]<b>" + namapelanggan + "</b>\n" +
                                                                                                                    "[L]<b>" + documentReference1.getId().trim() + "</b>\n" +
                                                                                                                    "[C]--------------------------------\n" +
                                                                                                                    "[L]\n";

                                                                                                    //loop hasil query rinci penjualan
                                                                                                    for (QueryDocumentSnapshot document1 : task.getResult()) {
                                                                                                        Double totalPerItem = Double.valueOf(document1.getString("jumlah")) * Double.valueOf(document1.getString("hargasatuan"));

                                                                                                        //UNTUK KASI SEPARATOR TITIK RUPIAH
                                                                                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                                                                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                                                                                        formatRp.setCurrencySymbol(" ");
                                                                                                        formatRp.setMonetaryDecimalSeparator(',');
                                                                                                        formatRp.setGroupingSeparator('.');
                                                                                                        kursIndonesia.setDecimalFormatSymbols(formatRp);

                                                                                                        layout += "[L]<b><font size='tall'>" + document1.getString("namabarang") + "</font size></b>\n";
                                                                                                        layout += "[L]<b><font size='tall'>" + String.valueOf(document1.getString("jumlah")) + " " + document1.getString("satuan") + " x @" + kursIndonesia.format(Double.valueOf(document1.getString("hargasatuan"))) + "" + "</font></b>\n";
                                                                                                        layout += "[L]<b><font size='tall'>Rp." + kursIndonesia.format(totalPerItem) + "</font></b>\n";
                                                                                                        layout += "[L]\n";

                                                                                                        totalJumlah += Double.valueOf(document1.getString("jumlah"));
                                                                                                        totalHarga += totalPerItem;
                                                                                                    }

                                                                                                    layout += "[C]--------------------------------\n";
                                                                                                    layout += "[L]\n";

                                                                                                    //UNTUK KASI SEPARATOR TITIK RUPIAH
                                                                                                    DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                                                                                    DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                                                                                    formatRp.setCurrencySymbol("Rp. ");
                                                                                                    formatRp.setMonetaryDecimalSeparator(',');
                                                                                                    formatRp.setGroupingSeparator('.');
                                                                                                    kursIndonesia.setDecimalFormatSymbols(formatRp);

                                                                                                    //tambahkan 1 text
                                                                                                    layout += "[R]<b><font size='tall'>" + "TOTAL : " + kursIndonesia.format(Double.valueOf(String.valueOf(totalHarga))) + "</font></b>\n";

                                                                                                    if (cb_lunas.isChecked()) {
                                                                                                        layout += "[R]<b>" + "*lunas*" + "</b>\n";
                                                                                                    } else {
                                                                                                        layout += "[R]*belum lunas*" + "\n";
                                                                                                        layout += "\n";
                                                                                                        if(et_titip2.getText().toString().replaceAll("[Rp,.\\s]", "").equals("0") || et_titip2.getText().toString().replaceAll("[Rp,.\\s]", "").equals("") || et_titip2.getText().toString().replaceAll("[Rp,.\\s]", "") == null){

                                                                                                        }else {
                                                                                                            layout += "[R]<font size='tall'>titip " + kursIndonesia.format(Double.valueOf(et_titip2.getText().toString().replaceAll("[Rp,.\\s]", ""))) + "</font>\n";
                                                                                                            layout += "\n";
                                                                                                            layout += "[R]<font size='tall'>kurang " + kursIndonesia.format(Double.valueOf(String.valueOf(Double.valueOf(String.valueOf(totalHarga)) - Integer.valueOf(et_titip2.getText().toString().replaceAll("[Rp,.\\s]", ""))))) + "</font>" + "\n";
                                                                                                        }
                                                                                                    }
                                                                                                    totalHarga = 0.0;
                                                                                                    totalJumlah = 0.0;

                                                                                                    layout += "[R]<qrcode size='7'>" + documentReference1.getId().trim() + "</qrcode>";
                                                                                                    layout += "\n";

                                                                                                    try {
                                                                                                        printer.printFormattedText(layout);
                                                                                                        rinciPenjualan.clear();
                                                                                                        displayRinciPenjualan.clear();
                                                                                                        arrayAdapter.notifyDataSetChanged();
                                                                                                    } catch (EscPosConnectionException e) {
                                                                                                        throw new RuntimeException(e);
                                                                                                    } catch (EscPosParserException e) {
                                                                                                        throw new RuntimeException(e);
                                                                                                    } catch (EscPosEncodingException e) {
                                                                                                        throw new RuntimeException(e);
                                                                                                    } catch (EscPosBarcodeException e) {
                                                                                                        throw new RuntimeException(e);
                                                                                                    }
                                                                                                }

                                                                                            }
                                                                                        });
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
                            Toast.makeText(AddPenjualanActivity.this, "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
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

        btn_addrincipenjualan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //munculkan dialog fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                AddRinciPenjualanDialogFragment dialogFragment = new AddRinciPenjualanDialogFragment();
                dialogFragment.setCancelable(false); //biar kalo dipencet diloar dialog tidak terclose dialognya
                dialogFragment.show(fragmentManager, "AddRinciPenjualanDialogFragment");
            }
        });

        sp_pelanggan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pelanggan selectedData = (Pelanggan) parent.getSelectedItem();
                idpelanggan = selectedData.idpelanggan;
                namapelanggan = selectedData.nama;
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
                datePickerDialog = new DatePickerDialog(AddPenjualanActivity.this,
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


        final int SWIPE_THRESHOLD = 200; // Kurangi threshold agar lebih responsif
        GestureDetector gestureDetector = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();

                if (Math.abs(deltaX) > SWIPE_THRESHOLD) { // Pastikan geseran cukup jauh
                    int position = lv_rincijual.pointToPosition((int) e1.getX(), (int) e1.getY());

                    if (position != ListView.INVALID_POSITION) {
                        View listItem = lv_rincijual.getChildAt(position - lv_rincijual.getFirstVisiblePosition());

                        if (listItem != null) {
                            // Animasi geser keluar sebelum dihapus
                            listItem.animate()
                                    .translationX(listItem.getWidth()) // Geser ke kanan penuh
                                    .alpha(0) // Fade out
                                    .setDuration(300)
                                    .withEndAction(() -> {
                                        displayRinciPenjualan.remove(position);
                                        rinciPenjualan.remove(position);

                                        lv_rincijual.setAdapter(arrayAdapter);
                                        arrayAdapter.notifyDataSetChanged();
                                    })
                                    .start();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        lv_rincijual.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));



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
        rinciPenjualan.add(data);
        String temp = data.get(3) + " " + data.get(2) + data.get(4) + " @ " + NumberFormat.getNumberInstance(new Locale("in", "ID")).format(Double.parseDouble(data.get(0))) + " :: Rp. " + NumberFormat.getNumberInstance(new Locale("in", "ID")).format(Double.parseDouble(String.valueOf(Double.valueOf(data.get(2)) * Double.valueOf(data.get(0)))));
        displayRinciPenjualan.add(temp);
        arrayAdapter.notifyDataSetChanged();
    }
}