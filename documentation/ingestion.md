# Ingestion (Ekstrakcija podataka)

## Ulazni podatak

Jedan veliki JSON fajl (`games.json`, ~900MB, ~120,000 igrica) u formatu:

```json
{
  "1234567": { "name": "...", "detailed_description": "...", "price": 19.99, ... },
  "7654321": { "name": "...", ... }
}
```

Fajl se **stream-uje** (token po token, preko `JsonParser`), tako da se nikad ne učitava ceo fajl u memoriju odjednom.

## Pravila validacije

### Obavezna polja (igrica se odbacuje ako nedostaju ili su nevalidna)

| Polje | Pravilo |
|---|---|
| `name` | mora postojati, ne sme biti prazno, mora sadržati samo Latinicu (bez korejskog/japanskog/kineskog pisma i sl.) |
| `detailed_description` | mora postojati, dužina 600-2500 karaktera, mora biti prepoznat kao engleski tekst |
| `price` | mora postojati, mora biti validan broj |
| `required_age` | mora postojati, mora biti validan broj |
| `release_date` | mora postojati, mora biti prepoznat validan format datuma |
| `categories` | mora postojati, mora biti neprazan niz |

### Opciona polja (mogu biti `null` ako nedostaju u izvoru)

`windows`, `mac`, `linux`, `estimated_owners`, `recommendations`, `average_playtime_forever`, `developers`, `publishers`, `genres`.

## Ponašanje pri pokretanju

Program pri svakom pokretanju **briše** postojeći sadržaj output foldera i kreće ispočetka.