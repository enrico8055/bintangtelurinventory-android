package com.app.bintangtelurinventory.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

import com.app.bintangtelurinventory.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFviewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffffff\">" + "PDF Viewer" + "</font>"));


        //ambil data yang di passing
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");

        //buka pdf dan tampilkan
        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.fromFile(new File(path))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load();

    }

}