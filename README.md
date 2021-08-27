# GroceryPal
Progetto di Sviluppo Applicazioni Mobili

### Architettura:




Client-Side [Android (JAVA)]
### Dipendenze:
* [com.google.firebase:firebase-auth](https://firebase.google.com/docs/auth/android/start) (Liberia che gestisce la comunicazione con firebase)
* [com.journeyapps:zxing-android-embedded:4.1.0](https://github.com/journeyapps/zxing-android-embedded) (Libreria che gestisce lo scan dei barcode)
* [com.android.volley:volley](https://github.com/google/volley) (Libreria che ottimizza le richieste GET/POST su base HTTP)
* [com.squareup.picasso:picasso](https://square.github.io/picasso/) (Semplifica il processo di caricamento delle immagini)
* 

### Funzionalità:
Ho utilizzato Flask per creare un REST che svolge il ruolo di proxy tra l'app e l'api di https://it.openfoodfacts.org creando una cache locale di foto e info sui prodotti, la libreria in questione mi permette di collegare le URL a una logica che strutta i metodi GET e POST per lo scambio di dati. In particolare:

## [/barcode/lang/bcode]
  * GET : restituisce un json con i dati relativi al prodotto, saranno dati di https://it.openfoodfacts.org se è la prima volta che viene richiesto
  il prodotto mentre dalla seconda volta in poi i dati saranno possibilmente modificati dagli utenti attraverso le post.
  * POST : l'utente una volta ricevuti i dati potrà modificarli, nel caso venga fatto viene eseguita una POST che andrà a modificare le info del
  prodotto.
  
P.S: tra le informazioni ci sono 2 counter, relativi all'immagine e al nome del prodotto , questi rendono permanenti le informazioni se X utenti sono concordi su queste ultime (cioè X utenti ricevono i dati e non modificano).
 
## [/images/bcode.jpeg]
 * GET : viene restituita l'immagine del prodotto , presente sul server.







###Server-Side [Python]

##Dipendenze Python:
* [flask](https://flask.palletsprojects.com/)
* [fuzzywuzzy](https://github.com/seatgeek/fuzzywuzzy)

##Funzionalità:
Ho utilizzato Flask per creare un REST che svolge il ruolo di proxy tra l'app e l'api di https://it.openfoodfacts.org creando una cache locale di foto e info sui prodotti, la libreria in questione mi permette di collegare le URL a una logica che strutta i metodi GET e POST per lo scambio di dati. In particolare:

#[/barcode/lang/bcode]
  * GET : restituisce un json con i dati relativi al prodotto, saranno dati di https://it.openfoodfacts.org se è la prima volta che viene richiesto
  il prodotto mentre dalla seconda volta in poi i dati saranno possibilmente modificati dagli utenti attraverso le post.
  * POST : l'utente una volta ricevuti i dati potrà modificarli, nel caso venga fatto viene eseguita una POST che andrà a modificare le info del
  prodotto.
  
P.S: tra le informazioni ci sono 2 counter, relativi all'immagine e al nome del prodotto , questi rendono permanenti le informazioni se X utenti sono concordi su queste ultime (cioè X utenti ricevono i dati e non modificano).
 
#[/images/bcode.jpeg]
 * GET : viene restituita l'immagine del prodotto , presente sul server.
