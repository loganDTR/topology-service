# Neo4j locale con Docker

Promemoria rapido per avviare, fermare e usare il database Neo4j in locale con persistenza dei dati.

## Prerequisiti

- Docker
- Docker Compose plugin (`docker compose`)

Verifica veloce:

```bash
docker --version
docker compose version
```

## Struttura consigliata

```text
.
├─ docker-compose.yml
├─ README.md
├─ .gitignore
├─ neo4j_auth.txt
└─ neo4j/
   ├─ data/
   ├─ logs/
   ├─ plugins/
   └─ import/
```

## Avvio

Dalla cartella del progetto:

```bash
docker compose up -d
```

Controlla che il container sia in esecuzione:

```bash
docker compose ps
```

Apri Neo4j Browser:

- http://localhost:7474

Accesso:

- user: `neo4j`
- password: quella salvata in `neo4j_auth.txt` nel formato `user/password` ad esempio `neo4j/UnaPasswordForte123!`

## Stop

```bash
docker compose down
```

Questo comando **non cancella i dati**, perché il database è persistito nelle cartelle locali sotto `./neo4j/`.

## Reset completo del database

Se vuoi ripartire da zero:

```bash
docker compose down
rm -rf ./neo4j/data ./neo4j/logs
mkdir -p ./neo4j/data ./neo4j/logs
```

Attenzione: questo elimina il contenuto del database.

## Query utili in Browser

### Vedere relazioni reali del database

```cypher
MATCH p=()-[r]->()
RETURN p
LIMIT 50
```

### Vedere nodi anche se isolati

```cypher
MATCH (n)
RETURN n
LIMIT 50
```

### Vedere lo schema logico

```cypher
CALL db.schema.visualization()
```

Nota importante:
la colonna sinistra di Neo4j Browser mostra solo informazioni su label, relazioni e property key.
Il **grafico** compare dopo aver eseguito una query che restituisce nodi e/o relazioni.

## Cartelle persistenti

- `./neo4j/data` → dati del database
- `./neo4j/logs` → log
- `./neo4j/plugins` → plugin aggiuntivi
- `./neo4j/import` → file CSV o altri file da importare

## Aggiornare l'immagine

Per aggiornare Neo4j:

```bash
docker compose pull
docker compose up -d
```

Prima di farlo, controlla eventuali note di compatibilità se cambi major/minor version.

## Consigli pratici

- Non committare `neo4j_auth.txt`
- Non committare `neo4j/data` e `neo4j/logs`
- Versiona solo file utili a ricreare l'ambiente:
  - `docker-compose.yml`
  - `README.md`
  - `.gitignore`
  - eventuali script
  - eventuali file di esempio in `neo4j/import/`

## Troubleshooting rapido

### Browser si apre ma non vedo il grafo
Non basta avere dati nel DB: devi eseguire una query che ritorni nodi o percorsi, ad esempio:

```cypher
MATCH p=()-[r]->()
RETURN p
LIMIT 25
```

### Porta occupata
Se `7474` o `7687` sono già occupate, cambia il mapping nel `docker-compose.yml`.

### Container non parte
Guarda i log:

```bash
docker compose logs -f neo4j
```
