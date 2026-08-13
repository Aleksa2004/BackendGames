# Chunking (Deljenje teksta)

`ChunkingService` deli duže opise na manje delove (chunk-ove) pre embedovanja.

## Pristup

- Cilj: `TARGET_CHUNK_SIZE` (600 karaktera)
- Preklapanje: `OVERLAP` (80 karaktera) između susednih chunk-ova, radi očuvanja konteksta na granici
- Deljenje se radi **po granicama rečenica** (regex na `. ! ?`), ne fiksno po broju karaktera - sprečava sečenje rečenica/reči na pola, što bi degradiralo kvalitet embedding-a
- Ako je opis kraći od `TARGET_CHUNK_SIZE`, vraća se kao jedan jedini chunk (bez deljenja)