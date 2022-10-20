##Relazione HomeWork2

###Link progetto github: https://github.com/mauri5566/HomeworkIDD.git

Il sistema indicizza tre file di testo per titolo e per contenuto. La console da il benvenuto all'utente dandogli le due opzioni di ricerca: titolo o contenuto.

Se l'utente preme "t", gli viene chiesto di inserire il termine di ricerca. Il termine viene memorizzato in una variabile statica *searchTerm* e viene utilizzato per definire una *phraseQuery*. Il sistema allora indicizza i documenti salvando l'indice nella directory *target/idx0* e esegue la phrase query. Il sistema restituisce correttamente solo se si inserisce un searchTerm che includa il nome del file nella sua interezza (compreso .txt). Sarebbe stato possibile fare diversamente modificando il *Tokenizer* in modo da permettere all' *Analyzer* di dividere il searchTerm a seconda della posizione del punto (.), si sarebbe quindi utilizzata una *BooleanQuery* in grado di accettare delle *TermQuery*.

Se l'utente preme "c", avviene lo stesso procedimento. Dopo aver inserito il searchTerm esso, se costituito da più parole, viene diviso in sotto-stringhe divise da spazi. In seguito ognuna di questa sotto-stringhe è aggiunta a una *BooleanQuery* sotto condizione **SHOULD**. In questo modo l'utente puo inserire vari termini, anche in ordine sparso, ottenendo in ritorno uno o più documenti se i termini inseriti vi sono contenuti. Il sistema allora indicizza i documenti salvando l'indice nella directory *target/idx1* ed esegue la boolean query.

L'indicizzazione dei documenti avviene nel metodo *indexDocs*. Questo definisce l'analyzer che distingue i due campi "titolo" e "contenuto", il metodo *addFileToIndex()* legge i 3 file.txt nella directory "files" e ne ricava contenuto e titolo per definire 3 documenti Lucene. L'*indexWriter* aggiunge i documenti all'indice e fa il *commit* dei cambiamenti.