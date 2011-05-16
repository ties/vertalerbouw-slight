/**
 * Parser for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

grammar ExampleScanner;

options {
  k=1;
  language = Java;
  output = AST;
}

/**
 * A program consists of several functions
 */
program 
  : (function)*
  ;
  