package com.example.games_extractor.dto;
import com.example.games_extractor.model.Game;

//Java objekat koji predstavlja jedan rezultat Qdrant pretrage
public class SearchResult {
	private String fileName;
	private int chunkNumber;
	private String chunk;
	private float score;
	private String reason;
	private Game game;

	public SearchResult(String fileName, int chunkNumber, String chunk, float score,String reason,Game game) {
		super();
		this.fileName = fileName;
		this.chunkNumber = chunkNumber;
		this.chunk = chunk;
		this.score = score;
		this.reason=reason;
		this.game=game;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public int getChunkNumber() {
		return chunkNumber;
	}


	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}


	public String getChunk() {
		return chunk;
	}


	public void setChunk(String chunk) {
		this.chunk = chunk;
	}


	public float getScore() {
		return score;
	}


	public void setScore(float score) {
		this.score = score;
	}


	public String getReason() {
		return reason;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}


	public Game getGame() {
		return game;
	}


	public void setGame(Game game) {
		this.game = game;
	}
	
	
}
