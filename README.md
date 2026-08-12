# Games Extractor - Backend

Backend servis za ekstrakciju, validaciju i pripremu podataka o igricama iz velikog JSON dataset-a (~120,000 zapisa), kao osnova za buduci RAG (Retrieval-Augmented Generation) sistem za preporuku igara.

## Status projekta

U razvoju - trenutno implementirana faza ekstrakcije i validacije podataka. Sledece faze (chankovanje, embedding, vektorska baza, RAG) su u planu.

## Tehnologije

- **Java** (Spring Boot)
- **Jackson** (streaming JSON parsiranje - `tools.jackson`)
- **Lingua** - detekcija jezika opisa igara

## Sta projekat radi

Ulazni podatak je jedan veliki JSON fajl (`games.json`, ~900MB, ~120,000 igrica) u formatu:

```json
{
  "1234567": { "name": "...", "detailed_description": "...", "price": 19.99, ... },
  "7654321": { "name": "...", ... }
}
```

Fajl se **stream-uje** (token po token, preko `JsonParser`), tako da se nikad ne ucitava ceo fajl u memoriju odjednom.

Za svaku igricu se proverava da li je **validna** prema sledecim pravilima:

### Obavezna polja (igrica se odbacuje ako nedostaju ili su nevalidna)

| Polje | Pravilo |
|---|---|
| `name` | mora postojati, ne sme biti prazno |
| `detailed_description` | mora postojati, duzina 600-2500 karaktera,mora biti prepoznat kao engleski tekst |
| `price` | mora postojati, mora biti validan broj |
| `required_age` | mora postojati, mora biti validan broj |
| `release_date` | mora postojati, mora biti prepoznat validan format datuma |
| `categories` | mora postojati, mora biti neprazan niz |

Ime igrice takodje mora sadrzati samo engleska slova, brojeve i standardnu interpunkciju (odbacuju se nazivi sa npr. korejskim/japanskim/kineskim karakterima).

### Opciona polja (mogu biti `null` ako nedostaju u izvoru)

`windows`, `mac`, `linux`, `estimated_owners`, `recommendations`, `average_playtime_forever`, `developers`, `publishers`, `genres`.

## Output struktura

Za svaku validnu igricu se generisu dva fajla:
game-extractor-output/
├── documents/
│   └── {NazivIgre}__{appId}.txt
└── metadata/
    └── {NazivIgre}__{appId}.metadata.json

### Primer `.txt` fajla:
```
===NASLOV===

Armored Brigade II

===OPIS IGRE===

Building on the success of its acclaimed predecessor, Armored Brigade II elevates the wargaming experience by skillfully blending accessibility with tactical depthall within a new, more immersive 3D graphic landscape . This finely-tuned balance makes it an ideal choice for both newcomers and veterans of military wargames. Engage in a simulator that offers both pausable real-time and turn-based gameplay, where every decision counts. Authentic, detailed gameplay captivates players of all levels, immersing them in a world where strategy and realism converge.
```
### Primer `.metadata.json` fajla:

```json
{
  "app_id" : "1234567",
  "release_date" : "May 25, 2022",
  "price" : 19.99,
  "required_age" : 0,
  "windows" : true,
  "mac" : false,
  "linux" : false,
  "estimated_owners" : "100000-200000",
  "recommendations" : 1250,
  "average_playtime_forever" : 340,
  "developers" : ["Team17"],
  "publishers" : ["Team17"],
  "genres" : ["Indie", "RPG"],
  "categories" : ["Single-player"]
}
```
## Kako pokrenuti

1. Postaviti putanju do izvornog JSON fajla u `GameExtractorService.JSON_PATH`
2. Postaviti izlaznu putanju u `GameExtractorService.OUTPUT_DIR`
3. Po potrebi podesiti `GAMES_COUNT` (broj igrica za obradu - koristi se za testiranje na manjem uzorku pre punog run-a na celom dataset-u)
4. Pokrenuti Spring Boot aplikaciju (`GamesExtractorApplication`)

Program pri svakom pokretanju **brise** postojeci sadrzaj output foldera i krece ispocetka.

## Planirani sledeci koraci (RAG pipeline)

1.  Ekstrakcija i validacija podataka
2.  Chankovanje teksta (`ChunkingService`) - deljenje dugih opisa na manje delove po granicama recenica, sa preklapanjem (overlap) radi ocuvanja konteksta
3.  Embedding - pretvaranje teksta u vektore preko Hugging Face Inference API-ja
4.  Cuvanje vektora u vektorskoj bazi (Chroma)
5.  Similarity search - pronalazenje najrelevantnijih igrica za korisnicki upit
6.  Generisanje odgovora preko Groq API-ja (LLM), na osnovu pronadjenog konteksta

## Napomene

- Sirovi `games.json` fajl i generisani output se **ne cuvaju u repozitorijumu** (veliki fajlovi, generisani podaci) - videti `.gitignore`
- Detekcija jezika koristi `lingua` biblioteku (`fromAllLanguages()`)