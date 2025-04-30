package com.example.bintangtelurinventory.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bintangtelurinventory.R;
import com.google.firebase.firestore.DocumentReference;

import com.example.bintangtelurinventory.modeldata.Pelanggan;
import com.example.bintangtelurinventory.modeldata.Penjualan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class KeuanganFragment extends Fragment {

    TextView et_hasil;
    EditText et_hutang, et_cash, et_bri, et_bca, et_tray, et_telor, et_utang_rico_lama, et_utang_rico_baru, et_utang_via, et_utang_yuni;
    Button btn_hitung;

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
        et_telor = view.findViewById(R.id.editTextTelor);
        et_utang_rico_lama = view.findViewById(R.id.editTextUtangRicoLama);
        et_utang_rico_baru = view.findViewById(R.id.editTextUtangRicoBaru);
        et_utang_via = view.findViewById(R.id.editTextUtangVia);
        et_utang_yuni = view.findViewById(R.id.editTextUtangYuni);

        //ambil total hutang
        et_hutang.setEnabled(false);
        db.collection("penjualan")
                .whereEqualTo("lunas", "belum")
                .orderBy("lunas", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<Penjualan> data = new ArrayList<>();
                        double hutang = 0.0;

                        // Sort berdasarkan nama pelanggan
                        List<QueryDocumentSnapshot> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            documents.add(doc);
                        }

                        // Hitung total hutang
                        for (QueryDocumentSnapshot document : documents) {
                            try {
                                Object totalObj = document.get("total");
                                double total = 0.0;
                                if (totalObj instanceof Double) {
                                    total = (Double) totalObj;
                                } else if (totalObj instanceof Long) {
                                    total = ((Long) totalObj).doubleValue();
                                } else if (totalObj instanceof String) {
                                    try {
                                        total = Double.parseDouble((String) totalObj);
                                    } catch (NumberFormatException e) {
                                        total = 0.0;  // Default jika konversi gagal
                                    }
                                }

                                Object titipObj = document.get("titip");
                                double titip = 0.0;
                                if (titipObj instanceof Double) {
                                    titip = (Double) titipObj;
                                } else if (titipObj instanceof Long) {
                                    titip = ((Long) titipObj).doubleValue();
                                } else if (titipObj instanceof String) {
                                    try {
                                        titip = Double.parseDouble((String) titipObj);
                                    } catch (NumberFormatException e) {
                                        titip = 0.0;  // Default jika konversi gagal
                                    }
                                }

                                hutang += total - titip;
                            } catch (NumberFormatException e) {
                                Log.e("ParseError", "Error parsing total/titip in document: " + document.getId(), e);
                            }
                        }

                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                        formatter.setMaximumFractionDigits(0);
                        formatter.setMinimumFractionDigits(0);
                        String formatted = formatter.format(hutang);
                        et_hutang.setText(formatted);
                    }
                });

        //isi data sebelumnya
        db.collection("keuangan")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot document : value) {
                                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                                formatter.setMaximumFractionDigits(0);
                                formatter.setMinimumFractionDigits(0);

                                et_cash.setText(formatter.format(document.getDouble("cash")));
                                et_bri.setText(formatter.format(document.getDouble("bri")));
                                et_bca.setText(formatter.format(document.getDouble("bca")));
                                et_tray.setText(formatter.format(document.getDouble("tray")));
                                et_telor.setText(formatter.format(document.getDouble("telor")));
                                et_utang_rico_lama.setText(formatter.format(document.getDouble("utangRico")));
                                et_utang_rico_baru.setText(formatter.format(document.getDouble("utangRicoBaru")));
                                et_utang_via.setText(formatter.format(document.getDouble("utangVia")));
                                et_utang_yuni.setText(formatter.format(document.getDouble("utangYuni")));

                                btn_hitung.performClick();
                                break;
                            }
                        } else {
                        }
                    }
                });

        //separator input
        setCurrencyTextWatcher(et_cash);
        setCurrencyTextWatcher(et_bri);
        setCurrencyTextWatcher(et_bca);
        setCurrencyTextWatcher(et_tray);
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
                double telor = Double.parseDouble(et_telor.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double hutang = Double.parseDouble(et_hutang.getText().toString().replaceAll("[Rp,.\\s]", ""));
                //pengurangan
                double rico_lama = Double.parseDouble(et_utang_rico_lama.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double rico_baru = Double.parseDouble(et_utang_rico_baru.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double via = Double.parseDouble(et_utang_via.getText().toString().replaceAll("[Rp,.\\s]", ""));
                double yuni = Double.parseDouble(et_utang_yuni.getText().toString().replaceAll("[Rp,.\\s]", ""));

                double hasil = hutang + cash + bri + bca + tray + telor - rico_lama - rico_baru - via - yuni;
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

