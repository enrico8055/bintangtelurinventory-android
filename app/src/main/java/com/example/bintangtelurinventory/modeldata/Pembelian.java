package com.example.bintangtelurinventory.modeldata;

import java.util.Date;

public class Pembelian {
    public String idpembelian = "";
    public String tanggaltransaksi = "";
    public String idsupplier = "";


    public Pembelian(String idpembelian, String tanggaltransaksi, String idsupplier) {
        this.idpembelian = idpembelian;
        this.tanggaltransaksi = tanggaltransaksi;
        this.idsupplier = idsupplier;
    }

    public String getIdpembelian() {
        return idpembelian;
    }

    public void setIdpembelian(String idpembelian) {
        this.idpembelian = idpembelian;
    }

    public String getTanggaltransaksi() {
        return tanggaltransaksi;
    }

    public void setTanggaltransaksi(String tanggaltransaksi) {
        this.tanggaltransaksi = tanggaltransaksi;
    }

    public String getIdsupplier() {
        return idsupplier;
    }

    public void setIdsupplier(String idsupplier) {
        this.idsupplier = idsupplier;
    }
}

