package edu.utwente.vb.example;

import java.io.FileReader;

import edu.utwente.vb.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import org.antlr.runtime.debug.BlankDebugEventListener;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

public class Compiler {
	private static boolean opt_ast = false, opt_dot = false,
			opt_no_checker = false, opt_no_codegen = false,
			opt_no_interpreter = false, opt_file_input = false,
			opt_debug_checker = false, opt_debug_parser = false;
	private static String filename = null;

	public static void parseOptions(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-ast"))
				opt_ast = true;
			else if (args[i].equals("-dot"))
				opt_dot = true;
			else if (args[i].equals("-no_checker"))
				opt_no_checker = true;
			else if (args[i].equals("-no_codegen"))
				opt_no_codegen = true;
			else if (args[i].equals("-no_interpreter"))
				opt_no_interpreter = true;
			else if (args[i].equals("-debug_checker"))
				opt_debug_checker = true;
			else if (args[i].equals("-debug_parser"))
				opt_debug_parser = true;
			else if (args[i].equals("-file_input") && (i + 1 < args.length)) {
				opt_file_input = true;
				i++;
				filename = args[i];
			} else {
				System.err.println("error: unknown option '" + args[i] + "'");
				System.err.println("valid options: -ast -dot "
						+ "-no_checker -no_codegen -no_interpreter"
						+ "-file_input <name> -debug_checker -debug_parser");
				throw new RuntimeException("Unknown options -- see readme");
			}
		}
	}

	public static void main(String[] args) {
		parseOptions(args);

		try {
			// Maak String template group aan de hand van file
//			FileReader groupFileR = new FileReader("SlightCodeGenerator.stg");
//			StringTemplateGroup templates = new
//			StringTemplateGroup(groupFileR);
//			groupFileR.close();

			CharStream stream;
			if (opt_file_input) {
				stream = new ANTLRFileStream(filename);
			} else {
				stream = new ANTLRInputStream(System.in);
			}
			
			ExampleLexer lexer = new ExampleLexer(stream);
			
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ExampleParser parser;

			if (!opt_debug_parser) {
				parser = new SlightParser(tokens, new BlankDebugEventListener());
//				parser = new SlightParser(tokens);
			} else {
				parser = new SlightParser(tokens);
			}
//			parser.setTreeAdaptor(new SlightTreeAdaptor());

			SlightParser.program_return result = parser.program();
			CommonTree tree = (CommonTree) result.getTree();

//			if (!opt_no_checker) { // check the AST
//				BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(tree);
//				SlightChecker checker;
//
//				if (!opt_debug_checker) {
//					checker = new SlightChecker(nodes,
//							new BlankDebugEventListener());
////					checker = new SlightChecker(nodes);
//				} else {
//					checker = new SlightChecker(nodes);
//				}
//				/* Patch de symbol table met default functies */
//				OverloadingSymbolTable<VarNode> symtab = checker.getSymbolTable();
//				symtab.openScope();
//				/* raar */
//				checker.setTreeAdaptor(new SlightTreeAdaptor());
//				checker.program();
//				symtab.closeScope();
//			}
//
//			if (!opt_no_interpreter) { // interpret the AST
//				BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(tree);
//				// CalcInterpreter interpreter = new CalcInterpreter(nodes);
//				// interpreter.program();
//			}
//
//			if (!opt_no_codegen) { // run codegenerator
//				CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
//				nodes.setTokenStream(tokens);
//				SlightCodeGenerator codg = new SlightCodeGenerator(nodes);
//				codg.setTemplateLib(templates);
//
//				SlightCodeGenerator.program_return r2 = codg.program();
//				StringTemplate output = (StringTemplate) r2.getTemplate();
//				System.out.println(output.toString());
//			}

			if (opt_ast) { // print the AST as string
				System.out.println(tree.toStringTree());
			} else if (opt_dot) { // print the AST as DOT specification
				DOTTreeGenerator gen = new DOTTreeGenerator();
				StringTemplate st = gen.toDOT(tree);
				System.out.println(st);
			}

			// } catch (SlightException e) {
			// System.err.print("ERROR: CalcException thrown by compiler: ");
			// System.err.println(e.getMessage());
		} catch (RecognitionException e) {
			System.err
					.print("ERROR: recognition exception thrown by compiler: ");
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.print("ERROR: uncaught exception thrown by compiler: ");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
