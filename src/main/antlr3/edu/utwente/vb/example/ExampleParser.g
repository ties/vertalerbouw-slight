/**
 * Parser for the Example programming language
 * Authors: Niek Tax & Ties de Kock
 *
 */

grammar ExampleParser;

options {
  k=1;
  language = Java;
  output = AST;
}

/**
 * A program consists of several functions
 */
program 
  : declarations (function)*
  ;
  
declarations
  : declaration*
  ;

declaration
  : VAR 
 