package com.salawubabatunde.seafarerbiometric.model;

public class Stats {
    private static Stats instance;

    private String totalSeafarer;
    private String pendingBiometric;
    private String captureBiometric;


    public Stats() {
    }

    public static Stats getInstance() {
        if (instance == null) {

            instance = new Stats();
        }
        return instance;
    }


    public String getTotalSeafarer() {
        return totalSeafarer;
    }

    public void setTotalSeafarer(String totalSeafarer) {
        this.totalSeafarer = totalSeafarer;
    }

    public String getPendingBiometric() {
        return pendingBiometric;
    }

    public void setPendingBiometric(String pendingBiometric) {
        this.pendingBiometric = pendingBiometric;
    }

    public String getCaptureBiometric() {
        return captureBiometric;
    }

    public void setCaptureBiometric(String captureBiometric) {
        this.captureBiometric = captureBiometric;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "totalSeafarer='" + totalSeafarer + '\'' +
                ", pendingBiometric='" + pendingBiometric + '\'' +
                ", captureBiometric='" + captureBiometric + '\'' +
                '}';
    }
}
