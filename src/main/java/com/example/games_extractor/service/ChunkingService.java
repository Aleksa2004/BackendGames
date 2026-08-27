package com.example.games_extractor.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

//Service koji pravi chunkove,tako da ih dijeli prema recenicama a ne po karakteru
@Service
public class ChunkingService {
	// chunk counter za provjeru
	private int chunkCounter = 0;

	private static final int CHUNK_SIZE = 600;
	private static final int MAX_CHUNK_SIZE = 699;
	private static final int OVERLAP_MAX = 70;
	private static final int LONG_SENTENCE_LIMIT = 400;

	// dijeli tekst na recenice
	private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

	public List<String> chunkTxt(String text) {

	    List<String> chunks = new ArrayList<>();

	    if (text == null || text.isBlank()) {
	        return chunks;
	    }

	    text = text.trim();

	    if (text.length() <= CHUNK_SIZE) {
	        chunks.add(text);
	        chunkCounter++;
	        return chunks;
	    }

	    String[] sentences = SENTENCE_SPLIT.split(text);

	    StringBuilder currentChunk = new StringBuilder();

	    int i = 0;

	    while (i < sentences.length) {

	        String sentence = sentences[i].trim();

	        if (sentence.isEmpty()) {
	            i++;
	            continue;
	        }

	        int currentLength = currentChunk.length();
	        int sentenceLength = sentence.length();

	        if (sentenceLength > LONG_SENTENCE_LIMIT) {

	            // Ako vec imamo nesto u trenutnom chunku,
	            // pokusavamo ga dopuniti dijelom velike recenice.
	            if (currentLength > 0) {

	                int remainingSpace =
	                        MAX_CHUNK_SIZE - currentLength - 1;

	                int commaPosition =
	                        findBestComma(sentence, remainingSpace);

	                if (commaPosition != -1 && commaPosition > 0) {

	                    String firstPart =
	                            sentence.substring(0, commaPosition + 1).trim();

	                    String secondPart =
	                            sentence.substring(commaPosition + 1).trim();

	                    currentChunk.append(firstPart);

	                    chunks.add(currentChunk.toString().trim());
	                    chunkCounter++;

	                    currentChunk = new StringBuilder(secondPart);

	                    i++;
	                    continue;
	                }
	            }

	            // Ako nema pogodnog zareza,
	            // pokusavamo cijelu recenicu staviti u trenutni chunk.
	            if (currentLength > 0) {

	                chunks.add(currentChunk.toString().trim());
	                chunkCounter++;

	                currentChunk = new StringBuilder();
	            }

	            currentChunk.append(sentence).append(" ");
	            i++;
	            continue;
	        }

	        // Normalna recenica moze stati u trenutni chunk
	        if (currentLength + sentenceLength + 1 <= MAX_CHUNK_SIZE) {

	            currentChunk.append(sentence).append(" ");
	            i++;
	            continue;
	        }

	        // Ne moze stati
	        if (currentLength > 0) {

	            String finishedChunk =
	                    currentChunk.toString().trim();

	            chunks.add(finishedChunk);
	            chunkCounter++;

	            String[] finishedSentences =
	                    SENTENCE_SPLIT.split(finishedChunk);

	            String overlapText =
	                    createOverlap(Arrays.asList(finishedSentences));

	            // Provjera moze li overlap + nova recenica stati.
	            if (overlapText.length() + sentenceLength + 1
	                    > MAX_CHUNK_SIZE) {

	                currentChunk = new StringBuilder();

	            } else {

	                currentChunk = new StringBuilder(overlapText);

	                if (currentChunk.length() > 0) {
	                    currentChunk.append(" ");
	                }
	            }

	            continue;
	        }

	        // Ako je trenutni chunk prazan,
	        // a recenica je normalne velicine,
	        // stavljamo je direktno.
	        currentChunk.append(sentence).append(" ");
	        i++;
	    }

	    // Posljednji chunk
	    if (currentChunk.length() > 0) {

	        chunks.add(currentChunk.toString().trim());
	        chunkCounter++;
	    }

	    return chunks;
	}
	public int getTotalnChunck() {
		return chunkCounter;
	}

	private String createOverlap(List<String> sentences) {

	    StringBuilder overlap = new StringBuilder();

	    for (int i = sentences.size() - 1; i >= 0; i--) {

	        String sentence = sentences.get(i).trim();

	        if (sentence.isEmpty()) {
	            continue;
	        }

	        int newLength = sentence.length();

	        if (overlap.length() > 0) {
	            newLength += 1 + overlap.length();
	        }

	        if (newLength > OVERLAP_MAX) {
	            break;
	        }

	        if (overlap.length() > 0) {
	            overlap.insert(0, " ");
	        }

	        overlap.insert(0, sentence);
	    }

	    return overlap.toString();
	}
	private int findBestComma(String sentence, int target) {

	    int bestPosition = -1;
	    int bestDistance = Integer.MAX_VALUE;

	    for (int i = 0; i <= Math.min(target, sentence.length()-1); i++) {

	        if (sentence.charAt(i) == ',') {

	            int distance = Math.abs(i - target);

	            if (distance < bestDistance) {
	                bestDistance = distance;
	                bestPosition = i;
	            }
	        }
	    }

	    return bestPosition;
	}
}
