# Output Format

Za svaku validnu igricu se generišu dva fajla:

game-extractor-output/
├── documents/
│ └── {NazivIgre}{appId}.txt
└── metadata/
└── {NazivIgre}{appId}.metadata.json

## Primer `.txt` fajla

Title: Armored Brigade II

Description: Building on the success of its acclaimed predecessor, Armored Brigade II elevates the wargaming experience...

## Primer `.metadata.json` fajla

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