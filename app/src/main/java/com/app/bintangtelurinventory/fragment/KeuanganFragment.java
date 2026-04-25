package com.app.bintangtelurinventory.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.app.bintangtelurinventory.R;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class KeuanganFragment extends Fragment {

    TextView et_hasil;
    EditText et_hutang, et_cash, et_bri, et_bca, et_tray, et_sisacairan, et_investasi, et_telor, et_utang_rico_lama, et_utang_rico_baru, et_utang_via, et_utang_yuni;
    Button btn_hitung;
    ProgressBar progressBar;
    ScrollView main_sv;

    Handler handler = new Handler();
    Runnable delayedAction = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keuangan, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //INIT
        et_hutang = view.findViewById(R.id.editTextHutang);
        et_cash = view.findViewById(R.id.editTextCash);
        et_hasil = view.findViewById(R.id.hasilHitung);
        btn_hitung = view.findViewById(R.id.buttonHitung);
        et_bri = view.findViewById(R.id.editTextBRI);
        et_bca = view.findViewById(R.id.editTextBCA);
        et_tray = view.findViewById(R.id.editTextTray);
        et_investasi = view.findViewById(R.id.editTextInvestasi);
        et_sisacairan = view.findViewById(R.id.editTextSisaCairan);
        et_telor = view.findViewById(R.id.editTextTelor);
        et_utang_rico_lama = view.findViewById(R.id.editTextUtangRicoLama);
        et_utang_rico_baru = view.findViewById(R.id.editTextUtangRicoBaru);
        et_utang_via = view.findViewById(R.id.editTextUtangVia);
        et_utang_yuni = view.findViewById(R.id.editTextUtangYuni);
        main_sv = view.findViewById(R.id.main_sv);
        progressBar = view.findViewById(R.id.progressBar);

        main_sv.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // ambil total hutang hanya sekali
        et_hutang.setEnabled(false);
        db.collection("penjualan")
                .whereEqualTo("lunas", "belum")
                .orderBy("lunas", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double hutang = 0.0;

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            double total = 0.0;
                            Object totalObj = document.get("total");
                            if (totalObj instanceof Number) {
                                total = ((Number) totalObj).doubleValue();
                            } else if (totalObj instanceof String) {
                                total = Double.parseDouble((String) totalObj);
                            }

                            double titip = 0.0;
                            Object titipObj = document.get("titip");
                            if (titipObj instanceof Number) {
                                titip = ((Number) titipObj).doubleValue();
                            } else if (titipObj instanceof String) {
                                titip = Double.parseDouble((String) titipObj);
                            }

                            hutang += total - titip;
                        } catch (Exception e) {
                            Log.e("ParseError", "Error parsing total/titip in document: " + document.getId(), e);
                        }
                    }

                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                    formatter.setMaximumFractionDigits(0);
                    formatter.setMinimumFractionDigits(0);
                    et_hutang.setText(formatter.format(hutang));


                    //isi data sebelumnya
                    db.collection("keuangan")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        formatter.setMaximumFractionDigits(0);
                                        formatter.setMinimumFractionDigits(0);

                                        et_cash.setText(formatter.format(document.getDouble("cash")));
                                        et_bri.setText(formatter.format(document.getDouble("bri")));
                                        et_bca.setText(formatter.format(document.getDouble("bca")));
                                        et_tray.setText(formatter.format(document.getDouble("tray")));
                                        et_investasi.setText(formatter.format(Objects.requireNonNullElse(document.getDouble("investasi"), 0.0)));
                                        et_sisacairan.setText(formatter.format(Objects.requireNonNullElse(document.getDouble("sisacairan"), 0.0)));
                                        et_telor.setText(formatter.format(document.getDouble("telor")));
                                        et_utang_rico_lama.setText(formatter.format(document.getDouble("utangRico")));
                                        et_utang_rico_baru.setText(formatter.format(document.getDouble("utangRicoBaru")));
                                        et_utang_via.setText(formatter.format(document.getDouble("utangVia")));
                                        et_utang_yuni.setText(formatter.format(document.getDouble("utangYuni")));

                                        main_sv.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        break;
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to fetch penjualan", e);
                });


        //separator input
        setCurrencyTextWatcher(et_cash);
        setCurrencyTextWatcher(et_bri);
        setCurrencyTextWatcher(et_bca);
        setCurrencyTextWatcher(et_tray);
        setCurrencyTextWatcher(et_investasi);
        setCurrencyTextWatcher(et_sisacairan);
        setCurrencyTextWatcher(et_telor);
        setCurrencyTextWatcher(et_utang_rico_lama);
        setCurrencyTextWatcher(et_utang_rico_baru);
        setCurrencyTextWatcher(et_utang_via);
        setCurrencyTextWatcher(et_utang_yuni);

        //event
        btn_hitung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //penambahan
                double cash = Double.parseDouble(et_cash.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double bri = Double.parseDouble(et_bri.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double bca = Double.parseDouble(et_bca.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double tray = Double.parseDouble(et_tray.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double investasi = Double.parseDouble(et_investasi.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double sisacairan = Double.parseDouble(et_sisacairan.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double telor = Double.parseDouble(et_telor.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double hutang = Double.parseDouble(et_hutang.getText().toString().replaceAll("[Rp,.\\s]", ""));
                //pengurangan
                double rico_lama = Double.parseDouble(et_utang_rico_lama.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double rico_baru = Double.parseDouble(et_utang_rico_baru.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double via = Double.parseDouble(et_utang_via.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double yuni = Double.parseDouble(et_utang_yuni.getText().toString().replaceAll("[Rp,.\\s]", ""));

                double hasil = hutang + cash + bri + bca + tray + investasi + sisacairan + telor - rico_lama - rico_baru - via - yuni;
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                formatter.setMaximumFractionDigits(0);
                formatter.setMinimumFractionDigits(0);
                String formatted = formatter.format(hasil);
                et_hasil.setText(formatted);

                //insert ke db
                db.collection("keuangan")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                DocumentReference docRef = document.getReference();

                                // Update multiple fields here. Replace/add as needed.
                                docRef.update(
                                        "cash", cash,
                                        "bca", bca,
                                        "bri", bri,
                                        "tray", tray,
                                        "investasi", investasi,
                                        "sisacairan", sisacairan,
                                        "utangRico", rico_lama,
                                        "utangRicoBaru", rico_baru,
                                        "utangVia", via,
                                        "utangYuni", yuni,
                                        "total", hasil,
                                        "telor", telor
                                ).addOnSuccessListener(aVoid -> {
                                    et_hasil.setVisibility(View.VISIBLE);
//                                    Toast.makeText(getActivity(), "Sukses", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                });
                                break;
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Error fetching documents", e);
                        });


            }
        });

        return view;
    }

    private void populateDataInit() {

    }

    private void setCurrencyTextWatcher(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et_hasil.setVisibility(View.INVISIBLE);
                if (delayedAction != null) {
                    handler.removeCallbacks(delayedAction);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();

                if (!input.equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = input.replaceAll("[Rp,.\\s]", "");
                    if (cleanString.isEmpty()) cleanString = "0";

                    try {
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                        formatter.setMaximumFractionDigits(0);
                        formatter.setMinimumFractionDigits(0); // just to be safe

                        String formatted = formatter.format(parsed);

                        current = formatted;
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

}

