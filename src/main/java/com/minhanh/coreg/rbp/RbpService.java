package com.minhanh.coreg.rbp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RbpService {
    @Autowired
    private RbpRepository rbpRepository;

    public Rbp filterByName(String name) { return rbpRepository.findByName(name); }
}
