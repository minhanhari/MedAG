package com.minhanh.coreg.transfactor;

import org.springframework.data.annotation.Id;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;

@Node("TransFactor")
public class TransFactorEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    @Property("annotation_id")
    private String annotationId;
    @Property("symbol")
    private String geneSymbol;
    @Property("gene_id")
    private int geneId;
    @Property("description")
    private String description;
    @Property("synonyms")
    private String synonyms;

    public TransFactorEntity() {
        super();
    }

    public TransFactorEntity(String annotationId, String geneSymbol, int geneId, String description, String synonyms) {
        super();
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
