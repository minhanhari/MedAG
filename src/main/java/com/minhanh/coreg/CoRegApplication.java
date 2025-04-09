package com.minhanh.coreg;

import java.rmi.RemoteException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoRegApplication {

    public static void main(String[] args) throws RemoteException {
        SpringApplication.run(CoRegApplication.class, args);
    }
}
