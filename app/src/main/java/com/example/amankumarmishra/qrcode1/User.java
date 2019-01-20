package com.example.amankumarmishra.qrcode1;

public class User {

    String sap;
    int score;

    public User(){}

    public User(String sap, int score){
        this.sap=sap;
        this.score= score;
    }

    public User(int score){
        this.score= score;
    }


    public int getScore() {
        return score;
    }

    public String getSap() {
        return sap;
    }

    public void setSap(String sap) {
        this.sap = sap;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
