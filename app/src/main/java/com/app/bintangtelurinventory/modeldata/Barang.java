package com.app.bintangtelurinventory.modeldata;

public class Barang {
    public String idbarang;
    public String namabarang;

    public Barang(String namabarang) {
        this.namabarang = namabarang;
    }

    public Barang(String idbarang, String namabarang) {
        this.idbarang = idbarang;
        this.namabarang = namabarang;
    }

    public String getNamabarang() {
        return namabarang;
    }

    public void setNamabarang(String namabarang) {
        this.namabarang = namabarang;
    }

    @Override
    public String toString() {
        return this.namabarang; // What to display in the Spinner list.
    }

    public String getIdbarang() {
        return idbarang;
    }

    public void setIdbarang(String idbarang) {
        this.idbarang = idbarang;
    }
}
