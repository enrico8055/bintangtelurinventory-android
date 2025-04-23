package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bintangtelurinventory.R;
import com.example.bintangtelurinventory.adapter.AdapterPdfDocument;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class RinciPembelianActivity extends AppCompatActivity {
    AtomicReference<ArrayList<String>> namabarang = new AtomicReference<>(null);
    AtomicReference<ArrayList<String>> idbarang = new AtomicReference<>(null);
    ImageView btn_print;
    TextView tv_idpembelian, tv_tanggaltransaksi, tv_nama;
    String idsupplier, tanggaltransaksi, idpembelian, namasupplier = "???";
    Double totalHarga = 0.0;
    Double totalJumlah = 0.0;

    Button btn_ext, btn_delete;
    CheckBox cb_lunas;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rinci_pembelian);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Cetak Pembelian" + "</font>"));

        //INIT
        btn_print = findViewById(R.id.btn_print);
        cb_lunas = findViewById(R.id.cb_lunas1);
        btn_ext = findViewById(R.id.btn_ext);
        btn_delete = findViewById(R.id.btn_delete);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        //ambil data yang di passing
        Intent intent = getIntent();
        idsupplier = intent.getStringExtra("idsupplier");
        tanggaltransaksi = intent.getStringExtra("tanggaltransaksi");
        idpembelian = intent.getStringExtra("idpembelian");
        tv_idpembelian = findViewById(R.id.tv_idbeli);
        tv_tanggaltransaksi = findViewById(R.id.tv_tanggalpembelian);
        tv_nama = findViewById(R.id.tv_nama);
        tv_idpembelian.setText("Lihat Detail Pembelian");
        tv_tanggaltransaksi.setText(tanggaltransaksi);


//        //ambil nama supplier bedasarkan id
        db.collection("supplier").document(idsupplier.trim())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        RinciPembelianActivity.this.namasupplier = documentSnapshot.getString("nama");
                        tv_nama.setText(namasupplier);
                    }
                });

        db.collection("rincipembelian").whereEqualTo("idpembelian", idpembelian.trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    ArrayList<String> data = new ArrayList<>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document1 : task.getResult()) {
                            data.add(document1.getString("idbarang"));

                        }
                        idbarang.updateAndGet(v -> data);
                    }
                });



        //EVENT
        btn_print.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //PRINT NOTA
                //ambil semua data rinci yang idpenjualannya adalah sama dengan id
                db.collection("rincipembelian").whereEqualTo("idpembelian", idpembelian.trim())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //ambil alamat storage aplikasi ini di external storage android, kalo blm ada maka buat foldernya
                                    String path;
                                    File dir = new File("storage/self/primary/Download/" + RinciPembelianActivity.this.getResources().getString(R.string.app_name) + "/");
                                    if (!dir.exists()){
                                        dir.mkdir();
                                    }
                                    path = dir.getPath() + File.separator;

                                    //kalo lokasi pathnya sudah siap maka ..
                                    if (new File(path).exists())
                                        new File(path).delete();
                                    try {
                                        //design isi/kerangka pdfnya
                                        Document document = new  Document();
                                        PdfWriter.getInstance(document, new FileOutputStream(path));
                                        document.open();

                                        //definisikan ukuran halaman
                                        document.setPageSize(PageSize.LETTER);
                                        document.addCreationDate();
                                        document.addAuthor("Enrico");
                                        document.addCreator("Enrico");

                                        //custom style font
                                        BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED); //ubah style font, pastikan siapkan file otf itu kalo folder asset blm ada klik kanan di folder java - new - folder - asset folder

                                        //tambahkan 1 text
                                        Font titleFont = new Font(fontName, 45.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk1 = new Chunk("TRANSAKSI PEMBELIAN", titleFont);
                                        Paragraph paragraph1 = new Paragraph(chunk1);
                                        paragraph1.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph1);

                                        //tambahkan 1 text
                                        Font subtitleFont = new Font(fontName, 30.0f, Font.ITALIC, BaseColor.BLACK);
                                        Chunk chunk3 = new Chunk("Jl. Puri Cipageran Indah 2, Cimahi", subtitleFont);
                                        Paragraph paragraph3 = new Paragraph(chunk3);
                                        paragraph3.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph3);

                                        //tambahkan 1 text
                                        Chunk chunk4 = new Chunk("(+62)87749596976", subtitleFont);
                                        Paragraph paragraph4 = new Paragraph(chunk4);
                                        paragraph4.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph4);

                                        //tambahkan garis pembatas
                                        Chunk chunk6 = new Chunk("----------------------------------------------", subtitleFont);
                                        Paragraph paragraph6 = new Paragraph(chunk6);
                                        paragraph6.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph6);

                                        //tambahkan 2 text kanan kiri
                                        Font contentFont = new Font(fontName, 25.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk2 = new Chunk(tanggaltransaksi, contentFont);
                                        Chunk chunk5 = new Chunk(idpembelian, contentFont);
                                        Paragraph paragraph2 = new Paragraph(chunk2);
                                        paragraph2.add(new Chunk(new VerticalPositionMark()));
                                        paragraph2.add(chunk5);
                                        document.add(paragraph2);

                                        //tambahkan 1 text
                                        Chunk chunk8 = new Chunk(namasupplier, subtitleFont);
                                        Paragraph paragraph8 = new Paragraph(chunk8);
                                        paragraph8.setAlignment(Element.ALIGN_LEFT);
                                        document.add(paragraph8);


                                        //tambahkan garis pembatas
                                        Chunk chunk7 = new Chunk("----------------------------------------------", subtitleFont);
                                        Paragraph paragraph7 = new Paragraph(chunk7);
                                        paragraph7.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph7);

                                        //loop hasil query rinci penjualan
                                        for (QueryDocumentSnapshot document1 : task.getResult()) {
                                            Double totalPerItem = Double.valueOf(document1.getString("jumlah")) * Double.valueOf(document1.getString("hargasatuan"));

                                            //tambahkan 2 text kanan kiri untuk rinci penjualan
                                            Chunk chunk9 = new Chunk(document1.getString("namabarang"), contentFont);
                                            Chunk chunk10 = new Chunk(String.valueOf(document1.getString("jumlah")) +" " + document1.getString("satuan") + " x @" + String.valueOf(document1.getString("hargasatuan")) + " : " + totalPerItem , contentFont);
                                            Paragraph paragraph9 = new Paragraph(chunk9);
                                            paragraph9.add(new Chunk(new VerticalPositionMark()));
                                            paragraph9.add(chunk10);
                                            try {
                                                document.add(paragraph9);
                                            } catch (DocumentException e) {
                                                throw new RuntimeException(e);
                                            }
                                            totalJumlah += Double.valueOf(document1.getString("jumlah"));
                                            totalHarga += totalPerItem;
                                        }

                                        //tambahkan garis pembatas
                                        Chunk chunk11 = new Chunk("------------------------", subtitleFont);
                                        Paragraph paragraph11 = new Paragraph(chunk11);
                                        paragraph11.setAlignment(Element.ALIGN_RIGHT);
                                        document.add(paragraph11);

                                        //UNTUK KASI SEPARATOR TITIK RUPIAH
                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                        formatRp.setCurrencySymbol("Rp. ");
                                        formatRp.setMonetaryDecimalSeparator(',');
                                        formatRp.setGroupingSeparator('.');
                                        kursIndonesia.setDecimalFormatSymbols(formatRp);
                                        //tambahkan 1 text
                                        Chunk chunk12 = new Chunk("TOTAL : " + kursIndonesia.format(Double.valueOf(String.valueOf(totalHarga))), subtitleFont);
                                        Paragraph paragraph12 = new Paragraph(chunk12);
                                        paragraph12.setAlignment(Element.ALIGN_RIGHT);
                                        document.add(paragraph12);

                                        //tambahkan 1 text
                                        Font statusFont = new Font(fontName, 25.0f, Font.NORMAL, BaseColor.BLACK);

//                                        if(cb_lunas.isChecked()){
//                                            Chunk chunk13 = new Chunk("*lunas(r)*", statusFont);
//                                            Paragraph paragraph13 = new Paragraph(chunk13);
//                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
//                                            document.add(paragraph13);
//                                        }else{
//                                            Chunk chunk13 = new Chunk("*belum lunas(r)*", statusFont);
//                                            Paragraph paragraph13 = new Paragraph(chunk13);
//                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
//                                            document.add(paragraph13);
//                                        }

                                        totalHarga = 0.0;
                                        totalJumlah = 0.0;

                                        //tambahkan jarak
                                        document.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

                                        //tambahkan barcode
                                        MultiFormatWriter mWriter = new MultiFormatWriter();
                                        try {
                                            //BitMatrix class to encode entered text and set Width & Height
                                            BitMatrix mMatrix = mWriter.encode(idpembelian.toString().trim(), BarcodeFormat.QR_CODE, 400, 400);
                                            BarcodeEncoder mEncoder = new BarcodeEncoder();
                                            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);//creating bitmap of code
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            Image img = null;
                                            byte[] byteArray = stream.toByteArray();
                                            try {
                                                img = Image.getInstance(byteArray);
                                            } catch (
                                                    BadElementException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            img.setAlignment(Image.ALIGN_CENTER);
                                            document.add(img);
                                        }catch (WriterException e) {

                                            e.printStackTrace();
                                        }

                                        //tambahkan 1 text
                                        Chunk chunk13 = new Chunk("LAYANAN KONSUMEN \n HUB. (+62)87749596976 \n enrico.aurelius@gmail.com", subtitleFont);
                                        Paragraph paragraph13 = new Paragraph(chunk13);
                                        paragraph13.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph13);

                                        document.close();
                                        try{
                                            //bentuk pdfnya save di path yang sudah kita ambil tadi diatas
                                            PrintDocumentAdapter printDocumentAdapter = new AdapterPdfDocument(RinciPembelianActivity.this, path );

                                            //cetakpdf td menggunakkan fitur print di android
                                            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                                            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());
                                        }catch (Exception e){
                                            Log.e("X", "err");
                                        }
                                    } catch (DocumentException | IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    Log.w("TAG", "Error getting documents.", task.getException());
                                };
                            }
                        });
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(RinciPembelianActivity.this);
                alert.setTitle("Hapus");
                alert.setMessage("Setelah di hapus transaksi tidak akan kembali, yakin hapus?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DELETE DATA PENJUALAN
                        db.collection("pembelian").document(idpembelian)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //DELETE DATA RINCI PENJUALAN
                                        db.collection("rincipembelian").whereEqualTo("idpembelian", idpembelian)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        Toast.makeText(RinciPembelianActivity.this, "Hapus Pembelian Berhasil!", Toast.LENGTH_SHORT).show();
                                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                            if(document.getString("idpembelian").equals(idpembelian.trim())){
                                                                document.getReference().delete();
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
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
            }
        });

        btn_ext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}