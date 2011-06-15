package edu.utwente.vb.example;

import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.utwente.vb.*;
import edu.utwente.vb.example.ExampleChecker.program_return;
import edu.utwente.vb.example.util.CheckerHelper;
import edu.utwente.vb.symbols.Prelude;
import edu.utwente.vb.symbols.SymbolTable;
import edu.utwente.vb.tree.TypedNode;
import edu.utwente.vb.tree.TypedNodeAdaptor;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import org.antlr.runtime.debug.BlankDebugEventListener;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

public class Compiler {
	private final static PrintStream out = System.out;

	private static boolean opt_ast = false, opt_dot = false,
			opt_no_checker = false, opt_no_codegen = false,
			opt_no_interpreter = false, opt_file_input = false,
			opt_debug_checker = false, opt_debug_parser = false;

	private static String filename = null;

	public static void parseOptions(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-ast")) {
				out.println("// - AST output");
				opt_ast = true;
			} else if (args[i].equals("-dot")) {
				out.println("// - DOT output");
				opt_dot = true;
			} else if (args[i].equals("-no_checker")) {
				out.println("// - checker disabled");
				opt_no_checker = true;
			} else if (args[i].equals("-no_codegen")) {
				out.println("// - codegen disabled");
				opt_no_codegen = true;
			} else if (args[i].equals("-no_interpreter")) {
				out.println("// - interpreter disabled");
				opt_no_interpreter = true;
			} else if (args[i].equals("-debug_checker")) {
				out.println("// + debugging checker");
				opt_debug_checker = true;
			} else if (args[i].equals("-debug_parser")) {
				out.println("// + debugging parser");
				opt_debug_parser = true;
			} else if (args[i].equals("-file_input") && (i + 1 < args.length)) {
				opt_file_input = true;
				i++;
				filename = args[i];
				out.println("// using filename: " + filename);
			} else {
				System.err.println("// error: unknown option '" + args[i] + "'");
				System.err.println("// valid options: -ast -dot "
						+ "-no_checker -no_codegen -no_interpreter"
						+ "-file_input <name> -debug_checker -debug_parser");
				throw new RuntimeException("Unknown options -- see readme");
			}
		}
	}

	public static void main(String[] args) {
		out.println("// Starting Example compiler");
		out.println("// =============== Options ===============");
		parseOptions(args);
		out.println("// =======================================");

		try {
			// Maak String template group aan de hand van file
			// FileReader groupFileR = new
			// FileReader("SlightCodeGenerator.stg");
			// StringTemplateGroup templates = new
			// StringTemplateGroup(groupFileR);
			// groupFileR.close();

			CharStream stream;
			if (opt_file_input) {
				out.println("// using input from " + filename);
				stream = new ANTLRFileStream(filename);
			} else {
				out.println("// using input from System.in");
				stream = new ANTLRInputStream(System.in);
			}

			ExampleLexer lexer = new ExampleLexer(stream);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ExampleParser parser;

			if (!opt_debug_parser) {
				parser = new ExampleParser(tokens,
						new BlankDebugEventListener());
			} else {
				parser = new ExampleParser(tokens);
			}
			parser.setTreeAdaptor(new TypedNodeAdaptor());

			ExampleParser.program_return result = parser.program();
			TypedNode tree = (TypedNode) result.getTree();

			ExampleChecker checker;
			
			ExampleChecker.program_return checker_result = null;
			//Let op: Aanpak voor checker staat op pagina 227 van ANTLR boek
			if (!opt_no_checker) { // check the AST
				BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(tree);
								
				if (!opt_debug_checker) {
					checker = new ExampleChecker(nodes, new BlankDebugEventListener());
//					checker = new SlightChecker(nodes);

				} else {
					checker = new ExampleChecker(nodes);
				}
				/* TODO: Patch de symbol table met default functies */
				SymbolTable<TypedNode> symtab = new SymbolTable<TypedNode>();
				Prelude pre = new Prelude();
				pre.inject(symtab);
				symtab.openScope();
				/* raar */
				CheckerHelper ch = new CheckerHelper(symtab);
				checker.setCheckerHelper(ch);
				
				checker.setTreeAdaptor(new TypedNodeAdaptor());
				checker_result = checker.program();
				symtab.closeScope();
			}
			
			{
				BufferedTreeNodeStream nodes = new BufferedTreeNodeStream((TypedNode)checker_result.getTree());
				CodegenPreparation prepare = new CodegenPreparation(nodes);
				prepare.setTreeAdaptor(new TypedNodeAdaptor());
				CodegenPreparation.program_return prepare_result = prepare.program();
			}
			//
			// if (!opt_no_interpreter) { // interpret the AST
			// BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(tree);
			// // CalcInterpreter interpreter = new CalcInterpreter(nodes);
			// // interpreter.program();
			// }
			//
			// if (!opt_no_codegen) { // run codegenerator
			// CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
			// nodes.setTokenStream(tokens);
			// SlightCodeGenerator codg = new SlightCodeGenerator(nodes);
			// codg.setTemplateLib(templates);
			//
			// SlightCodeGenerator.program_return r2 = codg.program();
			// StringTemplate output = (StringTemplate) r2.getTemplate();
			// System.out.println(output.toString());
			// }
            
			/* HIERONDER CODE VOOR CODEGENERATOR, LATER WEER AANZETTEN
			if (!opt_no_codegen) {
                // generate JVM assembler code using string template

                // read templates (src of code: [Parr 2007, p. 216])
                FileReader groupFiler = new FileReader("jvm.stg");
                StringTemplateGroup templates = new StringTemplateGroup(groupFiler);
                groupFiler.close();
                
                CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
                ExampleGenerator codegenerator = new ExampleGenerator(nodes);
                codegenerator.setTemplateLib(templates);
                ExampleGenerator.program_return r = codegenerator.program();
                StringTemplate output = (StringTemplate) r.getTemplate();
                System.out.println(output.toString());
            }*/

			
			
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
