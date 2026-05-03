package com.app.bintangtelurinventory.dialogfragment;

import android.icu.text.DecimalFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.app.bintangtelurinventory.R;
import com.app.bintangtelurinventory.activity.AddPenjualanActivity;
import com.app.bintangtelurinventory.helper.SharedPrefManager;
import com.app.bintangtelurinventory.modeldata.Barang;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddRinciPenjualanDialogFragment extends DialogFragment implements Validator.ValidationListener {
    Button btn_save;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button btn_exit;
    Spinner sp_barang;
    String namabarang;
    String idbarang;
    boolean isUpdatingAutoCountIkat = false;

    private Validator validator;
    @Length(min = 1)
    EditText et_hargasatuan, et_jumlah, et_jumlah_ikat;
    @Length(min = 1, trim = true)
    EditText et_satuan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_rinci_penjualan, container, false);

        //init
        validator = new Validator(this);
        validator.setValidationListener(this);

        //init
        btn_save = view.findViewById(R.id.btn_done);
        btn_exit = view.findViewById(R.id.btn_exit);
        sp_barang = view.findViewById(R.id.sp_barang);
        et_hargasatuan = view.findViewById(R.id.et_hargasatuan);
        et_jumlah = view.findViewById(R.id.et_jumlah);
        et_satuan = view.findViewById(R.id.et_satuan);
        et_jumlah_ikat = view.findViewById(R.id.et_jumlah_ikat);

        //ambil semua data barang untuk isi spinner
        db.collection("barang").whereEqualTo("uuid", SharedPrefManager.getInstance(getActivity()).getUserId()).orderBy("namabarang", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Barang> data = new ArrayList<Barang>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                data.add(new Barang(document.getId(), document.getString("namabarang")));
                            }
                            ArrayAdapter<Barang> spinnerAdapter = new ArrayAdapter<Barang>(getContext(), android.R.layout.simple_spinner_item, data);
                            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sp_barang.setAdapter(spinnerAdapter);
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }
                    }
                });

        //event
        sp_barang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Barang selectedData = (Barang) parent.getSelectedItem();
                idbarang = selectedData.idbarang;
                namabarang = selectedData.namabarang;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        et_hargasatuan.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    et_hargasatuan.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[Rp,.\\s]", "");

                    try {
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                        formatter.setMaximumFractionDigits(0); // remove decimal if needed
                        String formatted = formatter.format(parsed);

                        current = formatted;
                        et_hargasatuan.setText(formatted);
                        et_hargasatuan.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    et_hargasatuan.addTextChangedListener(this);
                }
            }
        });


        //otomatis hitung ikatnya terhadap kg dan sebaliknya
        DecimalFormat df = new DecimalFormat("#.##");
// KG → IKAT
        et_jumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingAutoCountIkat) return;

                String input = s.toString().trim();

                isUpdatingAutoCountIkat = true;

                if (input.isEmpty() || !input.matches("\\d+(\\.\\d+)?")) {
                    if (!et_jumlah_ikat.getText().toString().equals("")) {
                        et_jumlah_ikat.setText("");
                    }
                } else {
                    double kg = Double.parseDouble(input);
                    double ikat = kg / 15;
                    String result = df.format(ikat);

                    if (!et_jumlah_ikat.getText().toString().equals(result)) {
                        et_jumlah_ikat.setText(result);
                    }
                }

                isUpdatingAutoCountIkat = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


// IKAT → KG
        et_jumlah_ikat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingAutoCountIkat) return;

                String input = s.toString().trim();

                isUpdatingAutoCountIkat = true;

                if (input.isEmpty() || !input.matches("\\d+(\\.\\d+)?")) {
                    if (!et_jumlah.getText().toString().equals("")) {
                        et_jumlah.setText("");
                    }
                } else {
                    double ikat = Double.parseDouble(input);
                    double kg = ikat * 15;
                    String result = df.format(kg);

                    if (!et_jumlah.getText().toString().equals(result)) {
                        et_jumlah.setText(result);
                    }
                }

                isUpdatingAutoCountIkat = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss(); //tutup dialog
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();


            }
        });

        return view;
    }

    @Override
    public void onValidationSucceeded() {
        //action btn_save setelah divalidasi edittextnya oleh library
        ArrayList<String> data = new ArrayList<>();
        data.add(et_hargasatuan.getText().toString().replaceAll("[Rp,.\\s]", ""));
        data.add(idbarang);
        data.add(et_jumlah.getText().toString());
        data.add(namabarang);
        data.add(et_satuan.getText().toString());

        //kirim data ke activity
        AddPenjualanActivity addPenjualanActivity = (AddPenjualanActivity) getActivity();
        addPenjualanActivity.ambilDataDariDialogFragment(data);

        getDialog().dismiss();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Toast.makeText(getContext(), "Data Kurang Lengkap!", Toast.LENGTH_SHORT).show();
    }
}