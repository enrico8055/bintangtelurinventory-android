package com.app.bintangtelurinventory.modeldata;

public class Supplier {
    public String idsupplier;
    public String nama;

    public Supplier(String idsupplier, String nama) {
        this.idsupplier = idsupplier;
        this.nama = nama;
    }

    @Override
    public String toString() {
        return this.nama; // What to display in the Spinner list.
    }

    public String getIdsupplier() {
        return idsupplier;
    }

    public void setIdsupplier(String idsupplier) {
        this.idsupplier = idsupplier;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }
}
