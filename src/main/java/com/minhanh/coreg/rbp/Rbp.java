package com.minhanh.coreg.rbp;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigInteger;

@Document("rbps")
public class Rbp implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    private BigInteger id;
    @Field("Annotation ID")
    private String annotationId;
    @Field("Gene Symbol")
    private String geneSymbol;
    @Field("Gene Id")
    private int geneId;
    @Field("Description")
    private String description;
    @Field("Synonyms")
    private String synonyms;

    public Rbp() { super(); }

    public Rbp(String annotationId, String geneSymbol, int geneId, String description, String synonyms) {
        this.annotationId = annotationId;
        this.geneSymbol = geneSymbol;
        this.geneId = geneId;
        this.description = description;
        this.synonyms = synonyms;
    }

    public String getAnnotationId() {
        return annotationId;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public int getGeneId() {
        return geneId;
    }

    public String getDescription() {
        return description;
    }

    public String getSynonyms() {
        return synonyms;
    }
}
