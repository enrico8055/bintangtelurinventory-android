package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.fragment.BarangFragment;
import com.example.bintangtelurinventory.fragment.HutangFragment;
import com.example.bintangtelurinventory.fragment.KeuanganFragment;
import com.example.bintangtelurinventory.fragment.LaporanFragment;
import com.example.bintangtelurinventory.fragment.PelangganFragment;
import com.example.bintangtelurinventory.fragment.PembelianFragment;
import com.example.bintangtelurinventory.fragment.PenjualanFragment;
import com.example.bintangtelurinventory.fragment.SupplierFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer_layout;
    NavigationView navigation_drawer;
    Toolbar toolbar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    String apkUrl = "";


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "v13.1", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();

        //REQUEST PERMISSION BLUETOOTH
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 201);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 201);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 201);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 201);
        } else {

        }

        //cek dan minta permission notification
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.POST_NOTIFICATIONS}, 201);
        }

        //ambil statusapp untuk memastikan apakah app ini sudah usang
        db.collection("statusApp").whereEqualTo("v13", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        boolean isEx = false;
                        for (QueryDocumentSnapshot document : value) {
                            isEx = true;
                        }
                        if (isEx == false) {
                            //logout
                            mAuth.signOut();
                            //kembalikan ke activity welcome lagi
                            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Aplikasi usang, silahkan update aplikasi!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });


        drawer_layout = findViewById(R.id.drawer_layout);
        navigation_drawer = findViewById(R.id.navigation_drawer);
        toolbar = findViewById(R.id.toolbar);

        //pasangkan toolbar buatan kita mengganikan action bar bawaan
        toolbar.setTitle("Transaksi Penjualan");
        toolbar.setTitleTextColor(getColor(R.color.white));

        //berikan toogle menu di toolbar/actionbar kita untuk membuka tutup navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.nav_open_string, R.string.nav_close_string);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        //set listener untuk setiap menu yang ada di navigation view / drawer
        navigation_drawer.setNavigationItemSelectedListener(MainActivity.this);
        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));

        //secara default pilih dan tampilkan fragment home di framelayout
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new PenjualanFragment()).commit();
            navigation_drawer.setCheckedItem(R.id.menu_penjualan);
            toolbar.setTitle("Penjualan");
        }
    }

    //method yang akan menangkap event click setiap item di navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_penjualan: //kalo menu home di pencet maka pasangkan fragment home ke frame layout dan checked nemu home tersebut di navigation drawer
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-penjualan") && document.getBoolean("menu-penjualan") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new PenjualanFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_penjualan);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Transaksi Penjualan");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });
                break;
            case R.id.menu_hutang:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-hutang") && document.getBoolean("menu-hutang") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HutangFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_hutang);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Daftar Hutang");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_pelanggan:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-pelanggan") && document.getBoolean("menu-pelanggan") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new PelangganFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_pelanggan);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Master Pelanggan");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_barang:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-barang") && document.getBoolean("menu-barang") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new BarangFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_barang);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Master Barang");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_laporan:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-laporan") && document.getBoolean("menu-laporan") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LaporanFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_barang);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Laporan");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_pembelian:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-pembelian") && document.getBoolean("menu-pembelian") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new PembelianFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_pembelian);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Transaksi Pembelian");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_supplier:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-supplier") && document.getBoolean("menu-supplier") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new SupplierFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_supplier);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Master Supplier");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });

                break;
            case R.id.menu_keuangan:
                db.collection("statusMenu")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("menu-keuangan") && document.getBoolean("menu-keuangan") == true) {
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new KeuanganFragment()).commit();
                                        navigation_drawer.setCheckedItem(R.id.menu_keuangan);
                                        navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                                        navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                                        toolbar.setTitle("Keuangan");
                                        drawer_layout.close();
                                    } else {
                                        Toast.makeText(getBaseContext(), "Sedang Maintenance ...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new KeuanganFragment()).commit();
                navigation_drawer.setCheckedItem(R.id.menu_keuangan);
                navigation_drawer.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
                navigation_drawer.setItemIconTintList(ColorStateList.valueOf(Color.BLACK));
                toolbar.setTitle("Keuangan");
                drawer_layout.close();
                break;
            case R.id.menu_update:
                //DOWNLOAD APK BARUNYA DAN MUNCULKAN POPUP UPDATE KE USER
                db.collection("statusApp")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    if (document.contains("download-update-apk-url")) {
                                        apkUrl = document.getString("download-update-apk-url");

                                        if (apkUrl != null && !apkUrl.isEmpty()) {
                                            Toast.makeText(MainActivity.this, "Tunggu bentar lagi download ...", Toast.LENGTH_SHORT).show();
                                            Log.v("enrico", Uri.parse(apkUrl).toString());

                                            DownloadManager.Request request = new DownloadManager.Request(
                                                    Uri.parse(apkUrl)
                                            );
                                            request.setTitle("Downloading Bintang Telur Update");
                                            request.setDescription("Please wait...");
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bt-update.apk");

                                            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                            long downloadId = manager.enqueue(request);

                                            BroadcastReceiver onComplete = new BroadcastReceiver() {
                                                public void onReceive(Context context, Intent intent) {
                                                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                                    if (id == downloadId) {
                                                        Uri apkUri = manager.getUriForDownloadedFile(downloadId);

                                                        Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                                        intent2.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                        context.startActivity(intent2); // Corrected this line
                                                        unregisterReceiver(this);
                                                    }
                                                }
                                            };

                                            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                            break;
                                        } else {
                                            Toast.makeText(MainActivity.this, "Maaf belum ada update terbaru ...", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Failed to fetch statusApp", e);
                        });


                break;

            case R.id.menu_logout:
                //logout
                mAuth.signOut();
                //kembalikan ke activity welcome lagi
                Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return false;
    }
}