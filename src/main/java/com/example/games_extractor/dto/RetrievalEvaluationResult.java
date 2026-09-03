package com.example.games_extractor.dto;
//Rezultat retrieval evaluacije za jedno query pitanje
public class RetrievalEvaluationResult {

	private Long id;
	private String query;
	private boolean hit;
	private boolean top1Hit;
	private float topScore;

	public RetrievalEvaluationResult(Long id, String query, boolean hit, boolean top1Hit, float topScore) {
		super();
		this.id = id;
		this.query = query;
		this.hit = hit;
		this.top1Hit = top1Hit;
		this.topScore = topScore;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isHit() {
		return hit;
	}

	public void setHit(boolean hit) {
		this.hit = hit;
	}

	public boolean isTop1Hit() {
		return top1Hit;
	}

	public void setTop1Hit(boolean top1Hit) {
		this.top1Hit = top1Hit;
	}

	public float getTopScore() {
		return topScore;
	}

	public void setTopScore(float topScore) {
		this.topScore = topScore;
	}

}
