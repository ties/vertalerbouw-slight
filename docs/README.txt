///////////////////////////////////////////////////////////////////////////////
////////////                      EXAMPLE                           ///////////
///////////         A Python like Type-inferring langauge          ////////////
//////////             Authors: Niek Tax & Ties de Kock           /////////////
///////////////////////////////////////////////////////////////////////////////

De compiler wordt gecompileerd met hulp van maven. 
Maven2 is een dependency management en build tool voor java. 
De volgende commando's zijn relevant:

mvn clean - verwijder alle build artifacts
mvn compile - compileer en test de code
mvn assembly:assembly - compileer en unit test de code

Na het assembly commando staat de jar van de compiler in de target map.
De dependencies zitten ook in de example-compiler-...-jar-with-dependencies.jar 
Omdat de dependencies hier in zitten kan je de compiler starten zonder het 
classpath in te stellen.

java -jar target\example-compiler-0.1-jar-with-dependencies.jar


Wanneer je de compiler start probeert hij in code te lezen vanaf de 
standaardinvoer. Ook mogelijk en wellicht een pragmatischere oplossing is de 
compiler optie -file_input waarmee een bestand in de compiler kan worden 
ingevoerd. Een complete lijst met compileropties volgt hieronder:

	-debug_parser	Zet de parser in debug mode voor de parser, voor debug 
                    met antlrworks. Hiervoor moet je de target-classes 
                    directory van de compiler op je classpath toevoegen.
	-debug_checker	Zet de checker in debug mode voor de checker, voor debug 
                    met antlrworks. 
	-file_input     Argument:[filenaam]. Lees invoer vanuit bestand.
    -dot            Geeft uitvoer van de Abstract Syntax Tree in DOT formaat.
    -no_checker     Voert de compilerstappen uit t/m de Parser.
