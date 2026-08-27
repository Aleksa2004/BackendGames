package com.example.games_extractor.dto;
//DTO klasa koja predstavlja odgovor evaluacije RAG sistema
public class EvaluationResult {

	private double accuracy;
	private double completeness;
	private double score;
	private String reason;

	public EvaluationResult(double accuracy, double completeness, double score, String reason) {
		super();
		this.accuracy = accuracy;
		this.completeness = completeness;
		this.score = score;
		this.reason = reason;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getCompleteness() {
		return completeness;
	}

	public void setCompleteness(double completeness) {
		this.completeness = completeness;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
