package com.example.games_extractor.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//Ova klasa govori hibernate-u da je mapirana u bazi

@Entity
@Table(name = "embeddings_doc")
public class EmbeddingDoc {
	// primarni kljuc
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	// nullable znaci da mora imati vrijedonst(NOT NULL u SQL-u)
	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "file_path", nullable = false)
	private String filePath;

	@Column(name = "file_size_bajt", nullable = false)
	private Long fileSizeBajt;

	@Column(name = "chunk_size", nullable = false)
	private Integer chunkSize;

	@Column(name = "characterCount", nullable = false)
	private long characterCount;

	@Column(name = "chunk_count", nullable = false)
	private Integer chunkCount;

	@Column(name = "embedding_model", nullable = false)
	private String embeddingModel;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EmbeddingStatus status = EmbeddingStatus.PENDING;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name="tenant" ,nullable = false)
	private String tenant;
	
	@Column(name="dataset" ,nullable = false)
	private String dataset;

	// geteri i seteri

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Long getFileSize() {
		return fileSizeBajt;
	}

	public void setFileSize(Long fileSizeBajt) {
		this.fileSizeBajt = fileSizeBajt;
	}

	public Integer getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(Integer chunkSize) {
		this.chunkSize = chunkSize;
	}

	public Integer getChunkCount() {
		return chunkCount;
	}

	public long getCharacterCount() {
		return characterCount;
	}

	public void setCharacterCount(long characterCount) {
		this.characterCount = characterCount;
	}

	public void setChunkCount(Integer chunkCount) {
		this.chunkCount = chunkCount;
	}

	public EmbeddingStatus getStatus() {
		return status;
	}

	public void setStatus(EmbeddingStatus status) {
		this.status = status;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getEmbeddingModel() {
		return embeddingModel;
	}

	public void setEmbeddingModel(String embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getFileSizeBajt() {
		return fileSizeBajt;
	}

	public void setFileSizeBajt(Long fileSizeBajt) {
		this.fileSizeBajt = fileSizeBajt;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}
	
	
}
