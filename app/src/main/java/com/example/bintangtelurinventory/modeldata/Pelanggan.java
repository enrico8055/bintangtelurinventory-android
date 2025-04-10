package com.example.bintangtelurinventory.modeldata;

public class Pelanggan {
    public String idpelanggan;
    public String nama;

    public String alamat, notelp;


    public Pelanggan(String idpelanggan, String nama) {
        this.idpelanggan = idpelanggan;
        this.nama = nama;
    }

    public Pelanggan(String idpelanggan, String nama, String alamat, String notelp) {
        this.idpelanggan = idpelanggan;
        this.nama = nama;
        this.alamat = alamat;
        this.notelp = notelp;
    }

    @Override
    public String toString() {
        return this.nama; // What to display in the Spinner list.
    }

    public String getIdpelanggan() {
        return idpelanggan;
    }

    public void setIdpelanggan(String idpelanggan) {
        this.idpelanggan = idpelanggan;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNotelp() {
        return notelp;
    }

    public void setNotelp(String notelp) {
        this.notelp = notelp;
    }
}
