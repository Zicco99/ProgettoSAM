# GroceryPal
Progetto di Sviluppo Applicazioni Mobili

# Presentazione:
Lo scopo di questa applicazione è creare un sistema di gestione che agisca sull'inventario alimentare, permettendo di avere sul palmo di una mano un sumup di quello che si ha in frigo per evitare di comprare prodotti che abbiamo già e quindi prevenendo un possibile spreco alimentare.

L'app è stata progettata per ricavare da un server rest proxy informazioni sul prodotto e utilizza firebase per mantenere un database sincronizzato tra più utenti, il caso tipico è una casa di studenti o coinquilini che devo condividere un frigo : attraverso quest'app si può sapere chi è il proprietario di ogni prodotto evitando bigliettini o altri significanti inutili con in più la possibilità di essere avvertiti per prodotti in scadenza.

Un'ottima funzionalità rispetto altre app è che il server proxy intermediario , attraverso l'utilizzo continuato di vari utenti e attraverso il loro contributo (nome,foto) è in grado di apprendere velocemente informazioni riguardo prodotti che non ci sono per esempio su https://it.openfoodfacts.org, garantendo un database aggiornato dai clienti.

TODO: un passo avanti sarebbe la classificazione dei prodotti attraverso il ML con conseguente implementazione della GroceryList.

# Architettura:
<img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/Capture.png" width="200" height="400" />





# Client-Side [Android 8.0 (JAVA)] -> (API level 26+)
## Dipendenze:
* [com.google.firebase:firebase-auth](https://firebase.google.com/docs/auth/android/start) (Liberia che gestisce la comunicazione con firebase)
* [com.journeyapps:zxing-android-embedded:4.1.0](https://github.com/journeyapps/zxing-android-embedded) (Libreria che gestisce lo scan dei barcode)
* [com.android.volley:volley](https://github.com/google/volley) (Libreria che ottimizza le richieste GET/POST su base HTTP)
* [com.squareup.picasso:picasso](https://square.github.io/picasso/) (Semplifica il processo di caricamento delle immagini)

## Funzionalità:
* <table>
  <tr>
    <td>
      Aggiunta Frigo: <br> <br>
      <img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_frigo.gif" width="200" height="400" />
    </td>
    
    <td>
    </td>
  
    <td>
      Aggiunta Prodotto: <br> <br>
      <img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_prodotto.gif" width="200" height="400" />
    </td>
    
    <td>
    </td>
   
    <td>
      Aggiunta Coinquilino: <br> <br>
      <img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/aggiunta_coinquilino.gif" width="200" height="400" />
    </td>
  </tr>
</table>
<br><br>

<img src="https://github.com/Zicco99/ProgettoSAM/blob/master/readme-content/a796e97e-c07c-4f63-b9ca-c821f5a5084c.jpg" width="400" height="100"/> <br>

### Ogni prodotto ha 2 azioni , disponibili solo si è il proprietario:
* Attivare/Disattivare la notifica di scadenza : Alle 8:30 del giorno di scadenza verrà visualizzata una notifica.
* Rimuovere il prodotto.

Android quando viene riavviato perde le sveglie,all'interno del progetto c'è un 


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
