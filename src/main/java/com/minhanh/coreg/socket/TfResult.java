package com.minhanh.coreg.socket;


import com.minhanh.coreg.transfactor.TransFactorEntity;

public class TfResult {
    private TransFactorEntity transFactorEntity;

    public TfResult(TransFactorEntity tf) {
        this.transFactorEntity = tf;
    }

    public TransFactorEntity getTransFactorEntity() {
        return transFactorEntity;
    }

    public void setTransFactorEntity(TransFactorEntity transFactorEntity) {
        this.transFactorEntity = transFactorEntity;
    }
}
