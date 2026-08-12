# Games Extractor - Backend

Backend servis za ekstrakciju, validaciju i pripremu podataka o igricama iz velikog JSON dataset-a (~120,000 zapisa), kao osnova za budući RAG (Retrieval-Augmented Generation) sistem za preporuku igara.

## Status projekta

U razvoju - trenutno implementirana faza ekstrakcije i validacije podataka. Sledeće faze (embedding, vektorska baza, RAG) su u planu.

## Tehnologije

- **Java** (Spring Boot)
- **Jackson** (streaming JSON parsiranje - `tools.jackson`)
- **Lingua** - detekcija jezika opisa igara

## Dokumentacija po celinama

Detaljna dokumentacija za svaku celinu nalazi se u [`documentation/`](./documentation) folderu:

- [`ingestion.md`](./documentation/ingestion.md) - ekstrakcija i validacija podataka
- [`data_cleaning.md`](./documentation/data_cleaning.md) - čišćenje teksta i detekcija jezika
- [`output_format.md`](./documentation/output_format.md) - struktura output fajlova
- [`chunking.md`](./documentation/chunking.md) - deljenje teksta na chunk-ove

## Kako pokrenuti

```bash
mvnw spring-boot:run -Dspring-boot.run.arguments=extract
```

## Planirani sledeći koraci (RAG pipeline)

1.  Embedding (Hugging Face, lokalno preko Ollama)
2.  Vektorska baza (Chroma)
3.  Similarity search
4.  Generisanje odgovora preko Groq API-ja

## Napomene

- Sirovi `games.json` fajl i generisani output se **ne čuvaju u repozitorijumu** - videti `.gitignore`