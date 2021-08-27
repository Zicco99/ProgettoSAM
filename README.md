# GroceryPal
Progetto di Sviluppo Applicazioni Mobili

# Presentazione:
Lo scopo di questa applicazione è creare un sistema di gestione che agisce sull'inventario alimentare, permettendo di avere sul palmo di una mano un sumup di quello che si ha in frigo per evitare di comprare prodotti che abbiamo già e quindi prevenendo un possibile spreco alimentare.
The purpose of this food inventory management system is to facilitate knowledge of what products are stored in the refrigerator or pantry from anywhere and at any time, allowing to manage self-generated shopping lists and to launch notifications reminding the user of the scarcity or caducity of certain products when passing near establishments where they can be purchased.

This repository includes the system frontend, which has been designed to be intuitive and easy to use by all kinds of people with an Android smartphone. You can find the frontend of the project in this repository.

# Architettura:




# Client-Side [Android 8.0 (JAVA)] -> (API level 26+)
## Dipendenze:
* [com.google.firebase:firebase-auth](https://firebase.google.com/docs/auth/android/start) (Liberia che gestisce la comunicazione con firebase)
* [com.journeyapps:zxing-android-embedded:4.1.0](https://github.com/journeyapps/zxing-android-embedded) (Libreria che gestisce lo scan dei barcode)
* [com.android.volley:volley](https://github.com/google/volley) (Libreria che ottimizza le richieste GET/POST su base HTTP)
* [com.squareup.picasso:picasso](https://square.github.io/picasso/) (Semplifica il processo di caricamento delle immagini)

## Funzionalità:
* Aggiunta Frigo: <br> <img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_frigo.gif" width="200" height="400" />
* Aggiunta Prodotto: ![Alt Text](https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_prodotto.gif)
* Aggiunta Coinquilino: ![Alt Text](https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_coinquilino.gif)



## [/barcode/lang/bcode]
  * GET : restituisce un json con i dati relativi al prodotto, saranno dati di https://it.openfoodfacts.org se è la prima volta che viene richiesto
  il prodotto mentre dalla seconda volta in poi i dati saranno possibilmente modificati dagli utenti attraverso le post.
  * POST : l'utente una volta ricevuti i dati potrà modificarli, nel caso venga fatto viene eseguita una POST che andrà a modificare le info del
  prodotto.
  
P.S: tra le informazioni ci sono 2 counter, relativi all'immagine e al nome del prodotto , questi rendono permanenti le informazioni se X utenti sono concordi su queste ultime (cioè X utenti ricevono i dati e non modificano).
 
## [/images/bcode.jpeg]
 * GET : viene restituita l'immagine del prodotto , presente sul server.







# Server-Side [Python]

## Dipendenze Python:
* [flask](https://flask.palletsprojects.com/)
* [fuzzywuzzy](https://github.com/seatgeek/fuzzywuzzy)

## Funzionalità:
Ho utilizzato Flask per creare un REST che svolge il ruolo di proxy tra l'app e l'api di https://it.openfoodfacts.org creando una cache locale di foto e info sui prodotti, la libreria in questione mi permette di collegare le URL ad una logica che sfrutta i metodi GET e POST per lo scambio di dati. In particolare gli endpoint sono:

## /barcode/lang/bcode
  * GET : restituisce un json con i dati relativi al prodotto, saranno dati di https://it.openfoodfacts.org se è la prima volta che viene richiesto
  il prodotto mentre dalla seconda volta in poi i dati saranno possibilmente modificati dagli utenti attraverso le post.
  * POST : l'utente una volta ricevuti i dati potrà modificarli, nel caso venga fatto viene eseguita una POST che andrà a modificare le info del
  prodotto.
  
P.S: tra le informazioni ci sono 2 counter, relativi all'immagine e al nome del prodotto , questi rendono permanenti le informazioni se X utenti sono concordi su queste ultime (cioè X utenti ricevono i dati e non modificano).
 
## /images/bcode.jpeg
 * GET : viene restituita l'immagine del prodotto , presente sul server.
