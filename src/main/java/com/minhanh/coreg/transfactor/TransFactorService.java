package com.minhanh.coreg.transfactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransFactorService {
    @Autowired
    private TransFactorRepository rbpRepository;

    public TransFactorEntity filterByName(String name) { return rbpRepository.findByName(name); }
}
