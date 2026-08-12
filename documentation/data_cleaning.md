# Data Cleaning (Čišćenje podataka)

## Čišćenje opisa (`cleanText`)

Uklanja iz `detailed_description`:
- HTML tagove
- HTML entitete (npr. `&nbsp;`)
- kontrolne karaktere
- višestruke razmake (svodi na jedan)

## Čišćenje naziva za fajl sistem (`cleanName`)

- Uklanja karaktere nedozvoljene u imenima fajlova na Windows-u (`\ / : * ? " < > |`)
- Zamenjuje razmake sa `_`

## Detekcija jezika

Koriste se dve odvojene provere, za dva različita polja:

- **`isEnglish(opis)`** - statistički detektor jezika (`lingua` biblioteka), radi analizu **celog opisa** (600-2500 karaktera) i procenjuje da li tekst pripada engleskom jeziku. Pouzdano radi jer ima dovoljno teksta za statističku analizu.
- **`hasOnlyLatinCharacters(naziv)`** - regex provera da li naziv igrice sadrži samo dozvoljene karaktere (Latinica, brojevi, standardna interpunkcija). Koristi se umesto statističke detekcije jezika jer su nazivi prekratki (2-4 reči) za pouzdanu `lingua` analizu.

Obe provere su neophodne jer proveravaju različita polja - opis može biti engleski dok naziv sadrži strano pismo, i obrnuto.