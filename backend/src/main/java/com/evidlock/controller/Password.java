package com.evidlock.controller;

public class Password {
    private String oldPw;
    private String newPw;

    public Password() {}

    public Password(String oldPw, String newPw) {
        this.oldPw = oldPw;
        this.newPw = newPw;
    }

    public String getOldPw() {
        return oldPw;
    }

    public void setOldPw(String oldPw) {
        this.oldPw = oldPw;
    }

    public String getNewPw() {
        return newPw;
    }

    public void setNewPw(String newPw) {
        this.newPw = newPw;
    }
}
