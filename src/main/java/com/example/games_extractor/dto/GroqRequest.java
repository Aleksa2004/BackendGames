package com.example.games_extractor.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
//Atributi koje Groq zeli da primi pri slanju zahtjeva
public class GroqRequest {
	private String model;
	private List<Message> messages;
	private double temperature;
	
	@JsonProperty("response_format")
	private ResponseFormat responseFormat;

	public GroqRequest(String model, List<Message> messages, double temperature,ResponseFormat responseFormat) {
		this.model = model;
		this.messages = messages;
		this.temperature = temperature;
		this.responseFormat=responseFormat;
	}
	public GroqRequest(
	        String model,
	        List<Message> messages,
	        double temperature) {

	    this.model = model;
	    this.messages = messages;
	    this.temperature = temperature;
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

	public ResponseFormat getResponseFormat() {
		return responseFormat;
	}

	public void setResponseFormat(ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}
	

}
