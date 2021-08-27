# GroceryPal
Progetto di Sviluppo Applicazioni Mobili

### Funzionalità:

Server-Side [Python]
### Dipendenze Python:
* [flask](https://flask.palletsprojects.com/)
* [fuzzywuzzy](https://github.com/seatgeek/fuzzywuzzy)

### Funzionalità:
Ho utilizzato Flask per creare un REST che svolge il ruolo di proxy tra l'app e l'api di https://it.openfoodfacts.org creando una cache locale di foto e info sui prodotti, la libreria in questione mi permette di collegare le URL a una logica che strutta i metodi GET e POST per lo scambio di dati. In particolare:

## [/barcode/lang/bcode]
  * GET : restituisce un json con i dati relativi al prodotto, saranno dati di https://it.openfoodfacts.org se è la prima volta che viene richiesto
  il prodotto mentre dalla seconda volta in poi i dati saranno possibilmente modificati dagli utenti attraverso le post.
  * POST : l'utente una volta ricevuti i dati potrà modificarli, nel caso venga fatto viene eseguita una POST che andrà a modificare le info del
  prodotto
  
  P.S: tra le informazioni ci sono 2 counter , questi rendono permanenti le informazioni se X utenti sono concordi su queste ultime [cioè X utenti ricevono i dati e non li                modificano].
 
## /images/<bcode>.jpeg
 * GET : viene restituita l'immagine del prodotto,presente sul server.  

 
* [Grafana](https://grafana.com/docs/grafana/latest/installation/debian/)

* [JSON API Grafana Datasource](https://grafana.com/grafana/plugins/simpod-json-datasource/)

* Python 3.x


Per eseguire il programma:

sudo python3 appy.py (necessari permessi di root per eseguire le catture)

Per arrestare il programma:

ctrl + C

## Configurazione :
  1. Inserire come DataSource grafana-rrd-server porta di default 9000![image](https://user-images.githubusercontent.com/49340033/124386911-e05c6700-dcdc-11eb-861c-aa7487f499b5.png)

  2. Opzionale: Avvio da linea di comando di grafana-rrd-server -s [stepRRD] -p [porta] -r [directory files .rrd]
  3. Creazione Api Key di Grafana ![image](https://user-images.githubusercontent.com/49340033/124387161-b6f00b00-dcdd-11eb-969a-83f36b66d624.png)

  4. sudo python3 appy.py
  5. Creazione nuova config
      1. Scelta interfaccia cattura
      2. inserimento Api Grafana [Bearer --------] 
      3. Scelta modalità di aggregazione ip/prot7
      4. Scelta RRD step sec
      5. Scelta secondi entro il quale talker deve fare traffico per non essere eliminato
      6. Scelta nel numero di cicli (RDD step sec * Numero di cicli) in cui aggiornare la classifica nella dashboard
      7. Scelta numero di talkers da esporrè nei grafici in classifica
      8. Scelta se avviare da programma grafana-rrd-server
  7. Avvio di grafana-rrd-server
  
  ## Esecuzione:
  Modalità di aggregazione
  * IP
  ![image](https://user-images.githubusercontent.com/49340033/124630156-7087e100-de82-11eb-9152-4ce0f2a689d4.png)
  * prot7
  ![image](https://user-images.githubusercontent.com/49340033/124499407-3ce38300-ddbe-11eb-92a1-602c2f9eb23b.png)

## Come Avviene la Cattura
  La cattura avviene in un thread producer che esegue il comando tcpdump con timer di RRD_Step secondi, il produttore passa al consumatore il timestamp di fine  cattura e il nome del file da aprire.I file vengono scritti ogni RRD step secondi,se una cattura non è stata consumata in questo lasso di tempo è cancellata dal produttore.
  
## Come avviene l'aggiornamento degli RRD?
  L'aggiornamento si basa sul timestamp ottenuto a fine cattura del .pcap da parte del produttore, quindi ogni rrd dei talkers + l'rrd delle statistiche verranno aggiornati sullo stesso timestamp,un punto viene considerato Unkown se non viene effettuato un update per un periodo 3*RRD_step
  
## Come Avviene la Classificazione
  I top talkers vengono classificati sulla somma del bytes in ingresso / uscita / entrambi nel periodo di aggiornamento della classifica scelto
  (Ranking Refresh Time)*RRD_Step secondi.
  I grafici dei bytes si riferiscono alla somma dei bytes del periodo di classificazione
