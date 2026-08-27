package com.example.games_extractor.dto;

import java.util.List;
//DTO klasa koja predstavlja zahtjev za slanje poruke Groq API-u tokom evaluacije
public class EvaluationGroqRequest {
	private String model;
	private List<Message> messages;
	private double temperature;
	private ResponseFormat response_format;
	public EvaluationGroqRequest(String model, List<Message> messages, double temperature,
			ResponseFormat response_format) {
		super();
		this.model = model;
		this.messages = messages;
		this.temperature = temperature;
		this.response_format = response_format;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public ResponseFormat getResponse_format() {
		return response_format;
	}
	public void setResponse_format(ResponseFormat response_format) {
		this.response_format = response_format;
	}
	
	
}
