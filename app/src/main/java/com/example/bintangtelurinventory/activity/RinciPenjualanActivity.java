package com.example.bintangtelurinventory.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class RinciPenjualanActivity extends AppCompatActivity {
    AtomicReference<ArrayList<String>> namabarang = new AtomicReference<>(null);
    AtomicReference<ArrayList<String>> idbarang = new AtomicReference<>(null);
    ImageView btn_print;
    TextView tv_idpenjualan, tv_tanggaltransaksi, tv_nama, tv_alamatpelanggan, tv_no, tv_titip;
    String idpelanggan, tanggaltransaksi, idpenjualan, namapelanggan = "???", notelp, alamat;
    Double totalHarga = 0.0;
    Double totalJumlah = 0.0;

    Button btn_ext, btn_delete, btn_seedetail, btn_sharedetail;

    CheckBox cb_lunas;
    View vl_catatan;
    Image imageLogo;

    EditText et_titip;

    Handler handler = new Handler();
    Runnable delayedAction = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rinci_penjualan);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "Detail Penjualan" + "</font>"));

        //INIT
        btn_print = findViewById(R.id.btn_print);
        tv_titip = findViewById(R.id.tv_titip);
        cb_lunas = findViewById(R.id.cb_lunas1);
        btn_ext = findViewById(R.id.btn_ext);
        btn_delete = findViewById(R.id.btn_delete);
        vl_catatan = findViewById(R.id.vl_catatan);
        et_titip = findViewById(R.id.et_titip);
        btn_seedetail = findViewById(R.id.btn_seedetail);
        btn_sharedetail = findViewById(R.id.btn_sharedetail);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        //ambil data yang di passing
        Intent intent = getIntent();
        idpelanggan = intent.getStringExtra("idpelanggan");
        tanggaltransaksi = intent.getStringExtra("tanggaltransaksi");
        idpenjualan = intent.getStringExtra("idpenjualan");
        tv_idpenjualan = findViewById(R.id.tv_idbeli);
        tv_no = findViewById(R.id.tv_no);
        tv_alamatpelanggan = findViewById(R.id.tv_alamatpelanggan);
        tv_tanggaltransaksi = findViewById(R.id.tv_tanggalpembelian);
        tv_nama = findViewById(R.id.tv_nama);
        tv_idpenjualan.setText("ID : " + idpenjualan);
        tv_tanggaltransaksi.setText(tanggaltransaksi);


//        //ambil nama, no , alamat pelanggan bedasarkan id pelanggan
        db.collection("pelanggan").document(idpelanggan.trim())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        RinciPenjualanActivity.this.namapelanggan = documentSnapshot.getString("nama");
                        RinciPenjualanActivity.this.notelp = documentSnapshot.getString("notelp");
                        RinciPenjualanActivity.this.alamat = documentSnapshot.getString("alamat");
                        tv_nama.setText(namapelanggan);
                        tv_no.setText(String.valueOf(notelp));
                        tv_alamatpelanggan.setText(alamat);
                    }
                });

        db.collection("rincipenjualan").whereEqualTo("idpenjualan", idpenjualan.trim())
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

        db.collection("penjualan").document(idpenjualan.trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        String titip = document.getString("titip");
                        if(titip == null){
                            et_titip.setText("no data!");
                        }else {
                            et_titip.setText(titip);
                        }
                        String lunas = document.getString("lunas");
                        if(lunas == null){
                            cb_lunas.setChecked(false);
                        }else {
                            if(lunas.equals("ya")){
                                cb_lunas.setChecked(true);
                            }else{
                                cb_lunas.setChecked(false);
                            }
                        }
                    }
                });


        //EVENT


        et_titip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (delayedAction != null) {
                    handler.removeCallbacks(delayedAction);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                delayedAction = () -> {
                    String titip;
                    if (et_titip.getText().toString().isEmpty() ||
                            !et_titip.getText().toString().matches("\\d+(\\.\\d+)?") ||
                            Double.parseDouble(et_titip.getText().toString()) < 0) {
                        titip = "0";
                    } else {
                        titip = et_titip.getText().toString();
                    }
                    db.collection("penjualan")
                            .document(idpenjualan.trim())
                            .update(
                                    "titip", titip
                            )
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                };
                handler.postDelayed(delayedAction, 3000);
            }
        });

        cb_lunas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(cb_lunas.isChecked()){
                    et_titip.setVisibility(View.INVISIBLE);
                    tv_titip.setVisibility(View.INVISIBLE);

                    db.collection("penjualan")
                            .document(idpenjualan.trim())
                            .update(
                                    "lunas", "ya",
                                    "titip", "0"
                            )
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                }else{
                    et_titip.setVisibility(View.VISIBLE);
                    tv_titip.setVisibility(View.VISIBLE);

                    db.collection("penjualan")
                            .document(idpenjualan.trim())
                            .update("lunas", "belum")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getBaseContext(), "Status lunas berhasil diupdate", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getBaseContext(), "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });


        btn_seedetail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //PRINT NOTA PDF
                //ambil semua data rincipenjualan yang idpenjualannya adalah sama dengan idpenjualan
                db.collection("rincipenjualan").whereEqualTo("idpenjualan", idpenjualan.trim())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //ambil alamat storage aplikasi ini di external storage android, kalo blm ada maka buat foldernya
                                    String path;
                                    File dir = new File(getFilesDir(), getResources().getString(R.string.app_name)); // <-- diperbaiki di sini
                                    if (!dir.exists()) {
                                        dir.mkdirs(); // mkdirs lebih aman
                                    }
                                    path = new File(dir, "penjualan.pdf").getAbsolutePath(); // path lengkap ke file PDF

                                    //kalo lokasi pathnya sudah siap maka ..
                                    if (new File(path).exists())
                                        new File(path).delete();
                                    try {
                                        //design isi/kerangka pdfnya
                                        Document document = new  Document(PageSize.A4);
                                        PdfWriter.getInstance(document, new FileOutputStream(path));
                                        document.open();

                                        //definisikan ukuran halaman
                                        document.setPageSize(PageSize.LETTER);
                                        document.addCreationDate();
                                        document.addAuthor("Enrico");
                                        document.addCreator("Enrico");

                                        //custom style font
                                        BaseFont fontName = BaseFont.createFont("assets/fonts/Montrell-Bold.ttf", "UTF-8", BaseFont.EMBEDDED); //ubah style font, pastikan siapkan file otf itu kalo folder asset blm ada klik kanan di folder java - new - folder - asset folder

                                        //TAMBAHKAN LOGO BINTANG TELUR
                                        try {
                                            // get input stream
                                            Drawable drawable = getResources().getDrawable(R.drawable.logonota);
                                            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
                                            Bitmap bitmap = bitmapDrawable.getBitmap();
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //use the compression format of your need
                                            InputStream is = new ByteArrayInputStream(stream.toByteArray());
                                            Bitmap bmp = BitmapFactory.decodeStream(is);
                                            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            imageLogo = Image.getInstance(stream.toByteArray());
                                            imageLogo.scaleAbsolute(30, 30);
                                            imageLogo.setAlignment(Element.ALIGN_CENTER);
                                        }
                                        catch(IOException ex)
                                        {
                                            return;
                                        }

                                        Image imgBarcode = null;
                                        //tambahkan barcode
                                        MultiFormatWriter mWriter = new MultiFormatWriter();
                                        try {
                                            //BitMatrix class to encode entered text and set Width & Height
                                            BitMatrix mMatrix = mWriter.encode(idpenjualan.toString().trim(), BarcodeFormat.QR_CODE, 20, 20);
                                            BarcodeEncoder mEncoder = new BarcodeEncoder();
                                            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);//creating bitmap of code
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            byte[] byteArray = stream.toByteArray();
                                            try {
                                                imgBarcode = Image.getInstance(byteArray);
                                            } catch (
                                                    BadElementException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            imgBarcode.setAlignment(Image.ALIGN_LEFT);
                                        }catch (WriterException e) {

                                            e.printStackTrace();
                                        }


                                        //tambahkan 1 text
                                        Font titleFont = new Font(fontName, 30.0f, Font.BOLD, BaseColor.BLACK);
                                        Chunk chunk1 = new Chunk("BINTANG TELUR", titleFont);
                                        Paragraph paragraph1 = new Paragraph(chunk1);
                                        paragraph1.setAlignment(Element.ALIGN_CENTER);

                                        //tempelkan 2 item diatas
                                        PdfPTable table = new PdfPTable(3);
                                        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                                        table.getDefaultCell().setFixedHeight(150f);
                                        table.addCell(imageLogo);
                                        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                                        table.addCell(paragraph1);
                                        table.addCell(imgBarcode);
                                        document.add(table);

                                        //tambahkan 1 text
                                        Font subtitleFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk3 = new Chunk("Jl. Puri Cipageran Indah 2, Cimahi", subtitleFont);
                                        Paragraph paragraph3 = new Paragraph(chunk3);
                                        paragraph3.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph3);

                                        //tambahkan garis pembatas
                                        Font sepFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk6 = new Chunk("------------------------------------------------------------------------", sepFont);
                                        Paragraph paragraph6 = new Paragraph(chunk6);
                                        paragraph6.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph6);

                                        //tambahkan 2 text kanan kiri
                                        Font contentFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk2 = new Chunk(tanggaltransaksi, contentFont);
                                        Chunk chunk5 = new Chunk(idpenjualan, contentFont);
                                        Paragraph paragraph2 = new Paragraph(chunk2);
                                        paragraph2.add(new Chunk(new VerticalPositionMark()));
                                        paragraph2.add(chunk5);
                                        document.add(paragraph2);

                                        //tambahkan 1 text
                                        Chunk chunk8 = new Chunk(namapelanggan, subtitleFont);
                                        Paragraph paragraph8 = new Paragraph(chunk8);
                                        paragraph8.setAlignment(Element.ALIGN_LEFT);
                                        document.add(paragraph8);


                                        //tambahkan garis pembatas
                                        Chunk chunk7 = new Chunk("------------------------------------------------------------------------", sepFont);
                                        Paragraph paragraph7 = new Paragraph(chunk7);
                                        paragraph7.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph7);

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

                                            Font mainFont = new Font(fontName, 35.0f, Font.NORMAL, BaseColor.BLACK);
                                            //tambahkan text
                                            Chunk chunk18 = new Chunk(document1.getString("namabarang"), mainFont);
                                            Paragraph paragraph18 = new Paragraph(chunk18);
                                            paragraph18.setAlignment(Element.ALIGN_LEFT);
                                            document.add(paragraph18);

                                            //tambahkan text
                                            Chunk chunk19 = new Chunk(String.valueOf(document1.getString("jumlah")) +" " + document1.getString("satuan") + " x @" + kursIndonesia.format(Double.valueOf(document1.getString("hargasatuan")))  + " : " + kursIndonesia.format(totalPerItem) , mainFont);
                                            Paragraph paragraph19 = new Paragraph(chunk19);
                                            paragraph18.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph19);

                                            document.add(new Paragraph("\n\n"));
                                            totalJumlah += Double.valueOf(document1.getString("jumlah"));
                                            totalHarga += totalPerItem;
                                        }

                                        //UNTUK KASI SEPARATOR TITIK RUPIAH
                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                        formatRp.setCurrencySymbol("Rp. ");
                                        formatRp.setMonetaryDecimalSeparator(',');
                                        formatRp.setGroupingSeparator('.');
                                        kursIndonesia.setDecimalFormatSymbols(formatRp);
                                        //tambahkan 1 text
                                        Font totalFont = new Font(fontName, 35.0f, Font.BOLD, BaseColor.BLACK);
                                        Chunk chunk12 = new Chunk("TOTAL : " + kursIndonesia.format(Double.valueOf(String.valueOf(totalHarga))), totalFont);
                                        Paragraph paragraph12 = new Paragraph(chunk12);
                                        paragraph12.setAlignment(Element.ALIGN_RIGHT);
                                        document.add(paragraph12);

                                        //tambahkan 1 text
                                        Font statusFont = new Font(fontName, 25.0f, Font.NORMAL, BaseColor.BLACK);

                                        if(cb_lunas.isChecked()){
                                            Chunk chunk13 = new Chunk("*lunas(r)*", statusFont);
                                            Paragraph paragraph13 = new Paragraph(chunk13);
                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph13);
                                        }else{
                                            Chunk chunk13 = new Chunk("*belum lunas(r) -" + " titip Rp. " + et_titip.getText().toString()+" - kurang Rp. "+ String.valueOf(Double.valueOf(String.valueOf(totalHarga)) - Integer.valueOf(et_titip.getText().toString()))+"*", statusFont);
                                            Paragraph paragraph13 = new Paragraph(chunk13);
                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph13);
                                        }

                                        totalHarga = 0.0;
                                        totalJumlah = 0.0;

                                        document.close();
                                        //VIEW PDF DENGAN VIEWER BAWAAN ANDROID
                                        try{
                                            //bentuk pdfnya save di path yang sudah kita ambil tadi diatas
                                            PrintDocumentAdapter printDocumentAdapter = new AdapterPdfDocument(RinciPenjualanActivity.this, path );

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

        btn_sharedetail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //PRINT NOTA PDF
                //ambil semua data rincipenjualan yang idpenjualannya adalah sama dengan idpenjualan
                db.collection("rincipenjualan").whereEqualTo("idpenjualan", idpenjualan.trim())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //ambil alamat storage aplikasi ini di external storage android, kalo blm ada maka buat foldernya
                                    String path;
                                    File dir = new File(getFilesDir(), getResources().getString(R.string.app_name)); // <-- diperbaiki di sini
                                    if (!dir.exists()) {
                                        dir.mkdirs(); // mkdirs lebih aman
                                    }
                                    path = new File(dir, "penjualan.pdf").getAbsolutePath(); // path lengkap ke file PDF

                                    //kalo lokasi pathnya sudah siap maka ..
                                    if (new File(path).exists())
                                        new File(path).delete();
                                    try {
                                        //design isi/kerangka pdfnya
                                        Document document = new  Document(PageSize.A4);
                                        PdfWriter.getInstance(document, new FileOutputStream(path));
                                        document.open();

                                        //definisikan ukuran halaman
                                        document.setPageSize(PageSize.LETTER);
                                        document.addCreationDate();
                                        document.addAuthor("Enrico");
                                        document.addCreator("Enrico");

                                        //custom style font
                                        BaseFont fontName = BaseFont.createFont("assets/fonts/Montrell-Bold.ttf", "UTF-8", BaseFont.EMBEDDED); //ubah style font, pastikan siapkan file otf itu kalo folder asset blm ada klik kanan di folder java - new - folder - asset folder

                                        //TAMBAHKAN LOGO BINTANG TELUR
                                        try {
                                            // get input stream
                                            Drawable drawable = getResources().getDrawable(R.drawable.logonota);
                                            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
                                            Bitmap bitmap = bitmapDrawable.getBitmap();
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //use the compression format of your need
                                            InputStream is = new ByteArrayInputStream(stream.toByteArray());
                                            Bitmap bmp = BitmapFactory.decodeStream(is);
                                            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            imageLogo = Image.getInstance(stream.toByteArray());
                                            imageLogo.scaleAbsolute(30, 30);
                                            imageLogo.setAlignment(Element.ALIGN_CENTER);
                                        }
                                        catch(IOException ex)
                                        {
                                            return;
                                        }

                                        Image imgBarcode = null;
                                        //tambahkan barcode
                                        MultiFormatWriter mWriter = new MultiFormatWriter();
                                        try {
                                            //BitMatrix class to encode entered text and set Width & Height
                                            BitMatrix mMatrix = mWriter.encode(idpenjualan.toString().trim(), BarcodeFormat.QR_CODE, 20, 20);
                                            BarcodeEncoder mEncoder = new BarcodeEncoder();
                                            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);//creating bitmap of code
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            byte[] byteArray = stream.toByteArray();
                                            try {
                                                imgBarcode = Image.getInstance(byteArray);
                                            } catch (
                                                    BadElementException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            imgBarcode.setAlignment(Image.ALIGN_LEFT);
                                        }catch (WriterException e) {

                                            e.printStackTrace();
                                        }


                                        //tambahkan 1 text
                                        Font titleFont = new Font(fontName, 30.0f, Font.BOLD, BaseColor.BLACK);
                                        Chunk chunk1 = new Chunk("BINTANG TELUR", titleFont);
                                        Paragraph paragraph1 = new Paragraph(chunk1);
                                        paragraph1.setAlignment(Element.ALIGN_CENTER);

                                        //tempelkan 2 item diatas
                                        PdfPTable table = new PdfPTable(3);
                                        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                                        table.getDefaultCell().setFixedHeight(150f);
                                        table.addCell(imageLogo);
                                        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                                        table.addCell(paragraph1);
                                        table.addCell(imgBarcode);
                                        document.add(table);

                                        //tambahkan 1 text
                                        Font subtitleFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk3 = new Chunk("Jl. Puri Cipageran Indah 2, Cimahi", subtitleFont);
                                        Paragraph paragraph3 = new Paragraph(chunk3);
                                        paragraph3.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph3);

                                        //tambahkan garis pembatas
                                        Font sepFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk6 = new Chunk("------------------------------------------------------------------------", sepFont);
                                        Paragraph paragraph6 = new Paragraph(chunk6);
                                        paragraph6.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph6);

                                        //tambahkan 2 text kanan kiri
                                        Font contentFont = new Font(fontName, 30.0f, Font.NORMAL, BaseColor.BLACK);
                                        Chunk chunk2 = new Chunk(tanggaltransaksi, contentFont);
                                        Chunk chunk5 = new Chunk(idpenjualan, contentFont);
                                        Paragraph paragraph2 = new Paragraph(chunk2);
                                        paragraph2.add(new Chunk(new VerticalPositionMark()));
                                        paragraph2.add(chunk5);
                                        document.add(paragraph2);

                                        //tambahkan 1 text
                                        Chunk chunk8 = new Chunk(namapelanggan, subtitleFont);
                                        Paragraph paragraph8 = new Paragraph(chunk8);
                                        paragraph8.setAlignment(Element.ALIGN_LEFT);
                                        document.add(paragraph8);


                                        //tambahkan garis pembatas
                                        Chunk chunk7 = new Chunk("------------------------------------------------------------------------", sepFont);
                                        Paragraph paragraph7 = new Paragraph(chunk7);
                                        paragraph7.setAlignment(Element.ALIGN_CENTER);
                                        document.add(paragraph7);

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

                                            Font mainFont = new Font(fontName, 35.0f, Font.NORMAL, BaseColor.BLACK);
                                            //tambahkan text
                                            Chunk chunk18 = new Chunk(document1.getString("namabarang"), mainFont);
                                            Paragraph paragraph18 = new Paragraph(chunk18);
                                            paragraph18.setAlignment(Element.ALIGN_LEFT);
                                            document.add(paragraph18);

                                            //tambahkan text
                                            Chunk chunk19 = new Chunk(String.valueOf(document1.getString("jumlah")) +" " + document1.getString("satuan") + " x @" + kursIndonesia.format(Double.valueOf(document1.getString("hargasatuan")))  + " : " + kursIndonesia.format(totalPerItem) , mainFont);
                                            Paragraph paragraph19 = new Paragraph(chunk19);
                                            paragraph18.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph19);

                                            document.add(new Paragraph("\n\n"));
                                            totalJumlah += Double.valueOf(document1.getString("jumlah"));
                                            totalHarga += totalPerItem;
                                        }

                                        //UNTUK KASI SEPARATOR TITIK RUPIAH
                                        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
                                        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
                                        formatRp.setCurrencySymbol("Rp. ");
                                        formatRp.setMonetaryDecimalSeparator(',');
                                        formatRp.setGroupingSeparator('.');
                                        kursIndonesia.setDecimalFormatSymbols(formatRp);
                                        //tambahkan 1 text
                                        Font totalFont = new Font(fontName, 35.0f, Font.BOLD, BaseColor.BLACK);
                                        Chunk chunk12 = new Chunk("TOTAL : " + kursIndonesia.format(Double.valueOf(String.valueOf(totalHarga))), totalFont);
                                        Paragraph paragraph12 = new Paragraph(chunk12);
                                        paragraph12.setAlignment(Element.ALIGN_RIGHT);
                                        document.add(paragraph12);

                                        //tambahkan 1 text
                                        Font statusFont = new Font(fontName, 25.0f, Font.NORMAL, BaseColor.BLACK);

                                        if(cb_lunas.isChecked()){
                                            Chunk chunk13 = new Chunk("*lunas(r)*", statusFont);
                                            Paragraph paragraph13 = new Paragraph(chunk13);
                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph13);
                                        }else{
                                            Chunk chunk13 = new Chunk("*belum lunas(r) -" + " titip Rp. " + et_titip.getText().toString()+" - kurang Rp. "+ String.valueOf(Double.valueOf(String.valueOf(totalHarga)) - Integer.valueOf(et_titip.getText().toString()))+"*", statusFont);
                                            Paragraph paragraph13 = new Paragraph(chunk13);
                                            paragraph13.setAlignment(Element.ALIGN_RIGHT);
                                            document.add(paragraph13);
                                        }

                                        totalHarga = 0.0;
                                        totalJumlah = 0.0;

                                        document.close();

                                        //SHARE PDF
//                                        File filePdf = new File(dir, "penjualan.pdf");
//                                        Uri uri = FileProvider.getUriForFile(
//                                                RinciPenjualanActivity.this,
//                                                getPackageName() + ".provider",
//                                                filePdf
//                                        );
//
//                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                                        shareIntent.setType("application/pdf");
//                                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                                        try {
//                                            startActivity(Intent.createChooser(shareIntent, "Share PDF"));
//                                        } catch (ActivityNotFoundException e) {
//                                        }


                                        //CONVERT PDF KE IMG LALU SHARE
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            try {
                                                File file = new File(dir, "penjualan.pdf");

                                                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                                                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

                                                int pageCount = pdfRenderer.getPageCount();
                                                ArrayList<Uri> imageUris = new ArrayList<>();

                                                for (int i = 0; i < pageCount; i++) {
                                                    PdfRenderer.Page page = pdfRenderer.openPage(i);
                                                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);

                                                    Canvas canvas = new Canvas(bitmap);
                                                    canvas.drawColor(Color.WHITE); // penting biar background nggak hitam

                                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                                    page.close();

                                                    String fileName = String.format("penjualan_page_%02d.jpg", i + 1);
                                                    File imageFile = new File(dir, fileName);
                                                    FileOutputStream fos = new FileOutputStream(imageFile);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                                    fos.flush();
                                                    fos.close();

                                                    Uri imageUri = FileProvider.getUriForFile(
                                                            getBaseContext(),
                                                            getPackageName() + ".provider",
                                                            imageFile
                                                    );
                                                    imageUris.add(imageUri);
                                                }

                                                pdfRenderer.close();
                                                fileDescriptor.close();

                                                // Share multiple images
                                                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                                shareIntent.setType("image/jpeg");
                                                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                startActivity(Intent.createChooser(shareIntent, "Share all pages as images"));

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(getBaseContext(), "Gagal memproses PDF", Toast.LENGTH_SHORT).show();
                                            }
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

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REQUEST PERMISSION BLUETOOTH
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(RinciPenjualanActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RinciPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 201);
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(RinciPenjualanActivity.this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RinciPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 201);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(RinciPenjualanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RinciPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 201);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(RinciPenjualanActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RinciPenjualanActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 201);
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(RinciPenjualanActivity.this);
                    alert.setTitle("Cetak Nota");
                    alert.setMessage("Yakin Cetak Nota? Pastikan Sudah Connect Dengan Printer Bluetooth!");
                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(false){
                                        Toast.makeText(RinciPenjualanActivity.this, "Printer Bluetooth Tidak Terdeteksi!!", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(RinciPenjualanActivity.this, "Printing...", Toast.LENGTH_SHORT).show();
                                        //PRINT NOTA DIRECT KE PRINTER ESC/POS
                                        db.collection("rincipenjualan").whereEqualTo("idpenjualan", idpenjualan.trim())
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
                                                                    "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, RinciPenjualanActivity.this.getResources().getDrawableForDensity(R.drawable.logonota, DisplayMetrics.DENSITY_XXXHIGH)) + "</img> \n" +
                                                                            "[L]\n" +
                                                                            "[C]<u><font size='big'>Bintang Telur</font></u>\n" +
                                                                            "[C]Jl. Puri Cipageran Indah 2, Cimahi \n" +
//                                                        "[C]--------------------------------\n" +
                                                                            "[L]<b><font size='tall'>" + tanggaltransaksi + "</font></b>[R]<b>" + namapelanggan + "</b>\n" +
                                                                            "[L]<b>" + idpenjualan + "</b>\n" +
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

                                                                layout += "[L]<b><font size='tall'>" + document1.getString("namabarang") + "</font></b>\n";
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
                                                                layout += "[R]<b>" + "*lunas(r)*" + "</b>\n";
                                                            } else {
                                                                layout += "[R]*belum lunas(r)*" + "\n";
                                                                layout += "\n";
                                                                if(et_titip.getText().toString().equals("0") || et_titip.getText().toString().equals("") || et_titip.getText().toString() == null){

                                                                }else {
                                                                    layout += "[R]<font size='tall'>titip " + kursIndonesia.format(Double.valueOf(et_titip.getText().toString())) + "</font>\n";
                                                                    layout += "\n";
                                                                    layout += "[R]<font size='tall'>kurang " + kursIndonesia.format(Double.valueOf(String.valueOf(Double.valueOf(String.valueOf(totalHarga)) - Integer.valueOf(et_titip.getText().toString())))) + "</font>" + "\n";
                                                                }
                                                            }
                                                            totalHarga = 0.0;
                                                            totalJumlah = 0.0;

                                                            layout += "[R]<qrcode size='7'>" + idpenjualan.toString().trim() + "</qrcode>";
                                                            layout += "\n";

                                                            try {
                                                                printer.printFormattedText(layout);
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
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(RinciPenjualanActivity.this);
                alert.setTitle("Hapus");
                alert.setMessage("Setelah di hapus transaksi tidak akan kembali, yakin hapus?");
                alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DELETE DATA PENJUALAN
                        db.collection("penjualan").document(idpenjualan)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //DELETE DATA RINCI PENJUALAN
                                        db.collection("rincipenjualan").whereEqualTo("idpenjualan", idpenjualan)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        Toast.makeText(RinciPenjualanActivity.this, "Hapus Penjualan Berhasil!", Toast.LENGTH_SHORT).show();
                                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                            if(document.getString("idpenjualan").equals(idpenjualan.trim())){
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