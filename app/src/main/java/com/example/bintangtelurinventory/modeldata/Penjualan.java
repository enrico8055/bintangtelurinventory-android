package com.example.bintangtelurinventory.modeldata;

import java.util.Date;

public class Penjualan {
    public String idpenjualan;
    public String tanggaltransaksi;
    public String idpelanggan;


    public Penjualan(String idpenjualan, String tanggaltransaksi, String idpelanggan) {
        this.idpenjualan = idpenjualan;
        this.tanggaltransaksi = tanggaltransaksi;
        this.idpelanggan = idpelanggan;
    }

    public String getIdpenjualan() {
        return idpenjualan;
    }

    public void setIdpenjualan(String idpenjualan) {
        this.idpenjualan = idpenjualan;
    }

    public String getTanggaltransaksi() {
        return tanggaltransaksi;
    }

    public void setTanggaltransaksi(String tanggaltransaksi) {
        this.tanggaltransaksi = tanggaltransaksi;
    }

    public String getIdpelanggan() {
        return idpelanggan;
    }

    public void setIdpelanggan(String idpelanggan) {
        this.idpelanggan = idpelanggan;
    }
}

