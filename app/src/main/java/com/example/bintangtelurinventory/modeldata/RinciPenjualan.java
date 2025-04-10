package com.example.bintangtelurinventory.modeldata;

public class RinciPenjualan {
    public String idbarang;
    public String idpenjualan;
    public String hargasatuan;
    public String jumlah;
    public String satuan;

    public RinciPenjualan(String idbarang, String idpenjualan, String hargasatuan, String jumlah, String satuan) {
        this.idbarang = idbarang;
        this.idpenjualan = idpenjualan;
        this.hargasatuan = hargasatuan;
        this.jumlah = jumlah;
        this.satuan = satuan;
    }

    public String getIdbarang() {
        return idbarang;
    }

    public void setIdbarang(String idbarang) {
        this.idbarang = idbarang;
    }

    public String getIdpenjualan() {
        return idpenjualan;
    }

    public void setIdpenjualan(String idpenjualan) {
        this.idpenjualan = idpenjualan;
    }

    public String getHargasatuan() {
        return hargasatuan;
    }

    public void setHargasatuan(String hargasatuan) {
        this.hargasatuan = hargasatuan;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }
}

