package edu.utwente.vb.example;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.utwente.vb.*;
import edu.utwente.vb.example.Checker.program_return;
import edu.utwente.vb.example.CodeGenerator.OutputMode;
import edu.utwente.vb.example.asm.ASMAdapter;
import edu.utwente.vb.example.util.CheckerHelper;
import edu.utwente.vb.example.util.Utils;
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
			opt_debug_checker = false, opt_debug_parser = false,
			opt_debug_preparation = false, opt_debug_codegen = false,
			opt_debug = false;

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
			} else if (args[i].equals("-debug_preparation")) {
				out.println("// + debugging code generation preparation");
				opt_debug_preparation = true;
			} else if (args[i].equals("-debug_codegen")) {
				out.println("// + debugging code generation");
				opt_debug_codegen = true;
			} else if (args[i].equals("-file_input") && (i + 1 < args.length)) {
				opt_file_input = true;
				i++;
				filename = args[i];
				out.println("// using filename: " + filename);
			} else if (args[i].equals("-debug")) {
				opt_debug = true;
			} else {
				System.err
						.println("// error: unknown option '" + args[i] + "'");
				System.err
						.println("// valid options: -ast -dot "
								+ "-no_checker -no_codegen -no_interpreter"
								+ "-file_input <name> -debug_checker -debug_parser -debug_preparation -debug_codegen");
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

			Lexer lexer = new Lexer(stream);

			CommonTokenStream tokens = new CommonTokenStream(lexer);
			Parser parser;

			if (!opt_debug_parser) {
				parser = new Parser(tokens, new BlankDebugEventListener());
			} else {
				parser = new Parser(tokens);
			}

			if (opt_debug)
				parser.setDebug();
			parser.setTreeAdaptor(new TypedNodeAdaptor());

			Parser.program_return result = parser.program();
			TypedNode tree = (TypedNode) result.getTree();

			Checker checker;

			Checker.program_return checker_result = null;
			// Let op: Aanpak voor checker staat op pagina 227 van ANTLR boek
			if (!opt_no_checker) { // check the AST
				BufferedTreeNodeStream nodes = new BufferedTreeNodeStream(tree);

				if (!opt_debug_checker) {
					checker = new Checker(nodes, new BlankDebugEventListener());
					// checker = new SlightChecker(nodes);

				} else {
					checker = new Checker(nodes);
				}
				if (opt_debug)
					checker.setDebug();

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

			if (!opt_no_codegen) { // run codegenerator
				BufferedTreeNodeStream codgPrepareNodes = new BufferedTreeNodeStream((TypedNode) checker_result.getTree());
				CodegenPreparation prepare;
				if (!opt_debug_preparation) {
					prepare = new CodegenPreparation(codgPrepareNodes,
							new BlankDebugEventListener());
				} else {
					prepare = new CodegenPreparation(codgPrepareNodes);
				}
				prepare.setTreeAdaptor(new TypedNodeAdaptor());
				CodegenPreparation.program_return prepare_result = prepare
						.program();
				
				BufferedTreeNodeStream codgNodes = new BufferedTreeNodeStream(
						(TypedNode)prepare_result.getTree());
				codgNodes.setTokenStream(tokens);
				CodeGenerator codg;
				if (opt_debug_codegen) {
					codg = new CodeGenerator(codgNodes);
				} else {
					codg = new CodeGenerator(codgNodes,
							new BlankDebugEventListener());
				}
				String className = Utils.camelCaseName(new File(filename)
						.getName());

				codg.setTreeAdaptor(new TypedNodeAdaptor());
				codg.setOutputMode(OutputMode.FILE);
				codg.setFile(new File(className.concat(".class")));
				codg.setASMAdapter(new ASMAdapter(className, filename));

				CodeGenerator.program_return res = codg.program();
			}

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
