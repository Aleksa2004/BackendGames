package com.example.games_extractor.dto;

public class Range<T> {

	private T min;
	private T max;

	public Range() {
	}

	public T getMin() {
		return min;
	}

	public void setMin(T min) {
		this.min = min;
	}

	public T getMax() {
		return max;
	}

	public void setMax(T max) {
		this.max = max;
	}
	
	@Override
	public String toString() {
		return "Range{" + "min=" + min + ", max=" + max + '}';
	}
}
