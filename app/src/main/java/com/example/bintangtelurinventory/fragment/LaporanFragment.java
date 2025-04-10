package com.example.bintangtelurinventory.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.activity.AddPenjualanActivity;
import com.example.bintangtelurinventory.modeldata.Pelanggan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaporanFragment extends Fragment {
    EditText et_tglawal, et_tglakhir;
    ImageButton btn_tglawal, btn_tglakhir;
    Button btn_ambillaporan;
    DatePickerDialog datePickerDialog;
    ArrayList<String> datalaporan = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    ListView lv_laporan;
    TextView tv_tempData;
    TextView tv_tempData2;
    TextView tv_tempData3;
    TextView tv_tempData4;
    Spinner sp_pelanggan;
    String idpelanggan;
    String namapelanggan;
    TextView tv_judullaporan;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laporan, container, false);

        et_tglawal = view.findViewById(R.id.et_tglawal);
        et_tglakhir = view.findViewById(R.id.et_tglakhir);
        btn_tglawal = view.findViewById(R.id.btn_tglawal);
        tv_tempData = view.findViewById(R.id.tv_tempData);
        tv_tempData2 = view.findViewById(R.id.tv_tempData2);
        tv_tempData3 = view.findViewById(R.id.tv_tempData3);
        tv_tempData4 = view.findViewById(R.id.tv_tempData4);
        tv_judullaporan = view.findViewById(R.id.tv_judullaporan);
        btn_tglakhir = view.findViewById(R.id.btn_tglakhir);
        sp_pelanggan = (Spinner) view.findViewById(R.id.sp_pelanggan);
        lv_laporan = view.findViewById(R.id.lv_laporan);
        btn_ambillaporan = view.findViewById(R.id.btn_ambillaporan);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        et_tglawal.setText(formatter.format(date));
        et_tglakhir.setText(formatter.format(date));
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, datalaporan );
        lv_laporan.setAdapter(arrayAdapter);

        //ambil semua data pelanggan untuk isi spinner
        db.collection("pelanggan").orderBy("nama", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Pelanggan> data = new ArrayList<Pelanggan>();
                        if (task.isSuccessful()) {
                            data.add(new Pelanggan("all", "Semua Pelanggan"));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                data.add(new Pelanggan(document.getId(), document.getString("nama")));
                            }
                            ArrayAdapter<Pelanggan> spinnerAdapter = new ArrayAdapter<Pelanggan>(getContext(),android.R.layout.simple_spinner_item, data);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sp_pelanggan.setAdapter(spinnerAdapter);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
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

        btn_ambillaporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_tempData.setText("0");
                tv_tempData2.setText("0");
                tv_tempData3.setText("0");
                tv_tempData4.setText("0");
                datalaporan.clear();
                arrayAdapter.notifyDataSetChanged();
                datalaporan.add("*Dari " + et_tglawal.getText().toString() + " Sampai " + et_tglakhir.getText().toString() + "*");

                if(!namapelanggan.equals("Semua Pelanggan")){

                    String tglAwalFormatted = "";
                    String tglAkhirFormatted = "";
                    Date tglAwal = null;
                    Date tglAkhir = null;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tglAwal = sdf.parse(et_tglawal.getText().toString());
                        tglAkhir = sdf.parse(et_tglakhir.getText().toString());
                        SimpleDateFormat firestoreFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        tglAwalFormatted = firestoreFormat.format(tglAwal);
                        tglAkhirFormatted = firestoreFormat.format(tglAkhir);
                        Log.d("laporan", "Tgl Awal: " + tglAwalFormatted);
                        Log.d("laporan", "Tgl Akhir: " + tglAkhirFormatted);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    Timestamp timestampawal = new Timestamp(tglAwal);
                    Timestamp timestampakhir = new Timestamp(tglAkhir);


                    tv_judullaporan.setText("Laporan " + namapelanggan + ":");
                    Log.v("enrico2", idpelanggan);

                    db.collection("penjualan")
                            .whereGreaterThanOrEqualTo("timestamp", timestampawal) // Filter by start date
                            .whereLessThanOrEqualTo("timestamp", timestampakhir) // Filter by end date
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (!task.isSuccessful()) return;

                                    List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                                    int[] countJmlPenjualan = {0};

                                    for (QueryDocumentSnapshot document1 : task.getResult()) {
                                        if(document1.getString("idpelanggan").equals(String.valueOf(idpelanggan).trim())){
                                            countJmlPenjualan[0]++;
                                            tasks.add(db.collection("rincipenjualan")
                                                    .whereEqualTo("idpenjualan", document1.getId()) // Fetch only matching rincian
                                                    .get());
                                        }
                                    }

                                    // 🔹 Step 2: Process `rincipenjualan` Data Efficiently
                                    Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
                                        double totalPenjualan = 0.0, totalTelur = 0.0;

                                        for (Task<QuerySnapshot> rincipenjualanTask : tasks) {
                                            if (!rincipenjualanTask.isSuccessful()) continue;

                                            for (QueryDocumentSnapshot document2 : rincipenjualanTask.getResult()) {
                                                String namaBarang = document2.getString("namabarang");
                                                if (namaBarang != null && (namaBarang.toLowerCase().contains("telur") || namaBarang.toLowerCase().contains("telor"))) {
                                                    double jumlah = Double.parseDouble(document2.getString("jumlah"));
                                                    double harga = Double.parseDouble(document2.getString("hargasatuan"));
                                                    totalPenjualan += jumlah * harga;
                                                    totalTelur += jumlah;
                                                }
                                            }
                                        }

                                        tv_tempData.setText(String.valueOf(totalPenjualan));
                                        tv_tempData2.setText(String.valueOf(totalTelur));

                                        // 🔹 Step 3: Update UI Efficiently (Only Once)
                                        datalaporan.clear(); // Avoid duplicate data
                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                        formatRp.setCurrencySymbol("Rp. ");
                                        formatRp.setMonetaryDecimalSeparator(',');
                                        formatRp.setGroupingSeparator('.');
                                        kursIndonesia.setDecimalFormatSymbols(formatRp);

                                        datalaporan.add("Total Hasil Penjualan Telur: " + kursIndonesia.format(totalPenjualan));
                                        datalaporan.add("Total Telur Terjual : " + totalTelur + "kg");
                                        datalaporan.add("Total Transaksi Penjualan : " + countJmlPenjualan[0] + "x");

                                        arrayAdapter.notifyDataSetChanged();
                                    });
                                }
                            });


                }else{
                    tv_judullaporan.setText("Laporan " + namapelanggan + ":");

                    String tglAwalFormatted = "";
                    String tglAkhirFormatted = "";
                    Date tglAwal = null;
                    Date tglAkhir = null;
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tglAwal = sdf.parse(et_tglawal.getText().toString());
                        tglAkhir = sdf.parse(et_tglakhir.getText().toString());
                        SimpleDateFormat firestoreFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        tglAwalFormatted = firestoreFormat.format(tglAwal);
                        tglAkhirFormatted = firestoreFormat.format(tglAkhir);
                        Log.d("laporan", "Tgl Awal: " + tglAwalFormatted);
                        Log.d("laporan", "Tgl Akhir: " + tglAkhirFormatted);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    Timestamp timestampawal = new Timestamp(tglAwal);
                    Timestamp timestampakhir = new Timestamp(tglAkhir);



                    db.collection("penjualan")
                            .whereGreaterThanOrEqualTo("timestamp", timestampawal) // Filter by start date
                            .whereLessThanOrEqualTo("timestamp", timestampakhir) // Filter by end date
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (!task.isSuccessful()) return;

                                    Log.d("laporan", "Mulai perhitungan penjualan...");

                                    List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                                    int[] countJmlPenjualan = {0};

                                    for (QueryDocumentSnapshot document1 : task.getResult()) {
                                        countJmlPenjualan[0]++;
                                        tasks.add(db.collection("rincipenjualan")
                                                .whereEqualTo("idpenjualan", document1.getId()) // Fetch only matching rincian
                                                .get());
                                    }

                                    // 🔹 Step 2: Process `rincipenjualan` Data Efficiently
                                    Tasks.whenAllComplete(tasks).addOnCompleteListener(t -> {
                                        double totalPenjualan = 0.0, totalTelur = 0.0;

                                        Log.d("laporan", "Mulai perhitungan rinci penjualan...");


                                        for (Task<QuerySnapshot> rincipenjualanTask : tasks) {
                                            if (!rincipenjualanTask.isSuccessful()) continue;

                                            for (QueryDocumentSnapshot document2 : rincipenjualanTask.getResult()) {
                                                String namaBarang = document2.getString("namabarang");
                                                if (namaBarang != null && (namaBarang.toLowerCase().contains("telur") || namaBarang.toLowerCase().contains("telor"))) {
                                                    double jumlah = Double.parseDouble(document2.getString("jumlah"));
                                                    double harga = Double.parseDouble(document2.getString("hargasatuan"));
                                                    totalPenjualan += jumlah * harga;
                                                    totalTelur += jumlah;
                                                }
                                            }
                                        }

                                        tv_tempData.setText(String.valueOf(totalPenjualan));
                                        tv_tempData2.setText(String.valueOf(totalTelur));

                                        // 🔹 Step 3: Update UI Efficiently (Only Once)
                                        datalaporan.clear(); // Avoid duplicate data
                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                        formatRp.setCurrencySymbol("Rp. ");
                                        formatRp.setMonetaryDecimalSeparator(',');
                                        formatRp.setGroupingSeparator('.');
                                        kursIndonesia.setDecimalFormatSymbols(formatRp);

                                        datalaporan.add("Total Hasil Penjualan Telur: " + kursIndonesia.format(totalPenjualan));
                                        datalaporan.add("Total Telur Terjual : " + totalTelur + "kg");
                                        datalaporan.add("Total Transaksi Penjualan : " + countJmlPenjualan[0] + "x");

                                        arrayAdapter.notifyDataSetChanged();
                                    });
                                    Log.d("laporan", "Total transaksi ditemukan: " + countJmlPenjualan[0]);

                                }
                            });



                }

            }
        });

        btn_tglawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                et_tglawal.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        btn_tglakhir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                et_tglakhir.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}