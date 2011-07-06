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
mvn package - compileer en unit test de code

Na het assembly commando staat de jar van de compiler in de target map.
De dependencies zitten ook in de example-compiler-...-jar-with-dependencies.jar 
Omdat de dependencies hier in zitten kan je de compiler starten zonder het 
classpath in te stellen.

java -jar target\example-compiler-0.1-jar-with-dependencies.jar


Wanneer je de compiler start probeert hij in code te lezen vanaf de 
standaardinvoer. Ook mogelijk en wellicht een pragmatischere oplossing is de 
compiler optie -file_input waarmee een bestand in de compiler kan worden 
ingevoerd. Een complete lijst met compileropties volgt hieronder:

-ast                Optie om de Abstract Syntax Tree behorend bij dit 
                    testprogramma terug te geven in code-notatie.
-debug              Zet het gooien van Excepties aan, deze optie is nodig 
                    zodat de testsuite fouten kan herkennen.
-debug_checker      Start een debug sessie voor de checker, welke vanaf 
                    ANTLR-works benaderbaar is.
-debug_codegen      Start een debug sessie voor de codegenerator, welke vanaf
                    ANTLR-works benaderbaar is.
-debug_parser       Start een debug sessie voor de parser, welke vanaf 
                    ANTLR-works benaderbaar is.
-debug_preparation  Start een debug sessie voor de codegeneratie 
                    voorbereidingsfase, welke vanaf ANTLR-works benaderbaar is.
-dot                Optie om de Abstract Syntax Tree behorend bij dit 
                    testprogramma terug te geven in DOT-notatie.
-file_input         Hier kan een input-file voor de compiler aan worden     
                    meegegeven.
-no_checker         Zorgt voor uitvoer van enkel de parserlaag.
-no_codegen         Zorgt voor uitvoer tot en met de checkerlaag.
-no_interpreter     Zorgt voor uitvoer tot en met de codegeneratielaag.

