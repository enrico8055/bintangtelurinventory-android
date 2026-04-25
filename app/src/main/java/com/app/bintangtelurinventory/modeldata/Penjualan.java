package com.app.bintangtelurinventory.modeldata;

public class Penjualan {
    public String idpenjualan = "";
    public String tanggaltransaksi = "";
    public String idpelanggan = "";
    public String namapelanggan = "";
    public String total = "0";


    public String titip = "0";


    public Penjualan(String idpenjualan, String tanggaltransaksi, String idpelanggan, String namapelanggan, String total, String titip) {
        this.idpenjualan = idpenjualan;
        this.tanggaltransaksi = tanggaltransaksi;
        this.idpelanggan = idpelanggan;
        this.namapelanggan = namapelanggan;
        this.total = total;
        this.titip = titip;
    }

    public String getTitip() {
        return titip;
    }

    public void setTitip(String titip) {
        this.titip = titip;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getNamapelanggan() {
        return namapelanggan;
    }

    public void setNamapelanggan(String namapelanggan) {
        this.namapelanggan = namapelanggan;
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

