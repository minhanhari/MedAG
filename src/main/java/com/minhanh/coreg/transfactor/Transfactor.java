package com.minhanh.coreg.transfactor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigInteger;

@Document("transfactors")
public class Transfactor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private BigInteger id;

	@Field("Regulatory factor")
	private String regulatoryFactor;
	@Field("Target Gene")
	private String targetGene;

	@Field("Target UTR")
	private String targetUtr;
	@Field("Target UTR length")
	private int length;
	@Field("Binding site start")
	private int siteStart;
	@Field("Binding site stop")
	private long siteStop;
	
	public Transfactor() {
		super();
	}

	public Transfactor(String regulatory_factor, String target_gene, String target_utr, int length, int site_start, long site_stop) {
		super();
		this.regulatoryFactor = regulatory_factor;
		this.targetGene = target_gene;
		this.targetUtr = target_utr;
		this.length = length;
		this.siteStart = site_start;
		this.siteStop = site_stop;
	}

	public String getRegulatoryFactor() {
		return regulatoryFactor;
	}

	public String getTargetGene() {
		return targetGene;
	}

	public String getTargetUtr() {
		return targetUtr;
	}

	public int getLength() {
		return length;
	}

	public int getSiteStart() {
		return siteStart;
	}

	public long getSiteStop() {
		return siteStop;
	}
}
