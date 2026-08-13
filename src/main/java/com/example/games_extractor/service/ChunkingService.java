package com.example.games_extractor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
//Service koji pravi chunkove,tako da ih dijeli prema recenicama a ne po karakteru
@Service
public class ChunkingService {
	//chunk counter za provjeru
	private static final int CHUNK_SIZE = 600;
	private static final int OVERLAP = 80;

	// deli tekst na rečenice 
	private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

	public List<String> chunkTxt(String text) {
		List<String> chunks = new ArrayList<>();

		if (text == null || text.isBlank()) {
			return chunks;
		}
		text = text.trim();

		if (text.length() <= CHUNK_SIZE) {
			chunks.add(text.trim());
			return chunks;
		}

		String[] sentences = SENTENCE_SPLIT.split(text);
		StringBuilder currentChunk = new StringBuilder();

		for (String sentence : sentences) {
			// ako recenica predje chunk size ipak je dodaj
			if (currentChunk.length() > 0 && currentChunk.length() + sentence.length() > CHUNK_SIZE) {
				chunks.add(currentChunk.toString().trim());

				// novi chank pocinje sa overlapom od proslog
				String overlapText = currentChunk.length() > OVERLAP
						? currentChunk.substring(currentChunk.length() - OVERLAP)
						: currentChunk.toString();
				
				currentChunk = new StringBuilder(overlapText);

			}

			currentChunk.append(sentence).append(" ");
		}

		// na kraju sta ostane dodaj u poslednji chunk
		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString().trim());
		}
		return chunks;
	}

}
