package com.minhanh.coreg.transfactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransfactorService {
    @Autowired
    private TransfactorRepository transfactorRepository;

    public List<Transfactor> filterByGenes(String[] genes) {
        return transfactorRepository.findByGenes(genes);
    }

    public List<Transfactor> filterByName(String name) {
        return transfactorRepository.findByName(name);
    }
}
