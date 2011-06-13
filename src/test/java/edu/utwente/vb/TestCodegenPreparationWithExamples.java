package edu.utwente.vb;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import static junit.framework.Assert.*;

import org.antlr.runtime.*;
import org.junit.Test;

import edu.utwente.vb.example.ExampleParser;
import edu.utwente.vb.example.ExampleChecker;
import edu.utwente.vb.example.CodegenPreparation;
import edu.utwente.vb.exceptions.*;

public class TestCodegenPreparationWithExamples extends AbstractGrammarTest {
  @Test(expected=SymbolTableException.class)
  public void testScopeIncorrectDeclarationinwrongscope() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/scope_incorrect_declarationinwrongscope.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Test van 'paren'

  */
  @Test
  public void testParenGood() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/paren_good.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   DeMorgan

  */
  @Test
  public void testBooleanDemorgan() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/boolean_demorgan.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Test correcte werking van scopes. Definitie van een variabele op een nieuwe scope staat hoger in rang dan de definitie van deze variabele in een scope lager.

  */
  @Test
  public void testScopeCorrect() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/scope_correct.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Constant declaration + initialization

  */
  @Test
  public void testDeclarationConstBecomesAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_const_becomes_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   If statement with parentheses

  */
  @Test
  public void testIfstatementElsePar() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/ifstatement_else_par.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  infer return type
  print = functie! Haakjes zijn mooi. Niet print 'statement'
  functie zonder parameter aanmaken
  functie zonder parameter aanroepen

  */
  @Test
  public void testFunctionReturn() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/function_return.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInferconstBecomesNonInferrableConflictingTypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_inferconst_becomes_non-inferrable_conflicting_types.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=RecognitionException.class)
  public void testParenNotopened() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/paren_notopened.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testDeclarationInfervarBecomesInferrableAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_infervar_becomes_inferrable_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntchartoint() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intchartoint.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testScopeIncorrectDoubledeclaration() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/scope_incorrect_doubledeclaration.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=MismatchedTokenException.class)
  public void testDeclarationConstNobecomesVoid() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_const_nobecomes_void.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInfervarBecomesNonInferrableVoidReturn() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_infervar_becomes_non-inferrable_void_return.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntbooltobool() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intbooltobool.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInferconstBecomesNonInferrableVoidReturn() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_inferconst_becomes_non-inferrable_void_return.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testAppliedOccurrenceInferconstUnknown() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_inferconst_unknown.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   While loop

  */
  @Test
  public void testWhileBraces() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/while_braces.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testAppliedOccurrenceVarSametype() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_var_sametype.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Variable declaration + initialization

  */
  @Test
  public void testDeclarationVarBecomesAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_var_becomes_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testDeclarationInferconstBecomesInferrableAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_inferconst_becomes_inferrable_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testAppliedOccurrenceConstEdit() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_const_edit.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Boolean logic
  Implication;
   a -> b

  */
  @Test
  public void testBooleanImplication() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/boolean_implication.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntstringtobool() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intstringtobool.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=RecognitionException.class)
  public void testParenNotclosed() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/paren_notclosed.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Some simple math

  */
  @Test
  public void testExpressionsBasicmath() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/expressions_basicmath.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=RecognitionException.class)
  public void testCommentIncorrectNotclosed() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/comment_incorrect_notclosed.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Correct usage of scope/variable shadowing

  */
  @Test
  public void testDeclarationVarRedeclare() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_var_redeclare.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Test van 'paren'

  */
  @Test
  public void testParenSeperatedParts() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/paren_seperated_parts.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInfervarBecomesNonInferrableUnknownPartSelf() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_infervar_becomes_non-inferrable_unknown_part_self.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=RecognitionException.class)
  public void testAppliedOccurrenceInferconst() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_inferconst.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testAppliedOccurrenceConstSametype() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_const_sametype.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testAppliedOccurrenceInfervarUnknown() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_infervar_unknown.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInferconstBecomesNonInferrableUnknownPartSelf() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_inferconst_becomes_non-inferrable_unknown_part_self.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   fibonacci(n)
  f(0) = 0
  f(1) = 1
  f(n) = f(n - 1) + f(n - 2)

  */
  @Test
  public void testPopularFibonacci() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/popular_fibonacci.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testAppliedOccurrenceInfervar() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_infervar.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   If statement without parentheses

  */
  @Test
  public void testIfstatementElseNopar() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/ifstatement_else_nopar.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Test typechecking op correcte bewerkingen

  */
  @Test
  public void testTypeExpressionsSametype() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_expressions_sametype.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Correct usage of scope/variable shadowing

  */
  @Test
  public void testDeclarationConstRedeclare() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_const_redeclare.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInferconstBecomesNonInferrableUnknownPartOther() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_inferconst_becomes_non-inferrable_unknown_part_other.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Test of je willekeurig een closedcompound mag beginnen

  */
  @Test
  public void testScopeWillekeurigeClosedCompound() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/scope_willekeurige_closed_compound.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntstringtochar() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intstringtochar.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testAppliedOccurrenceConstToVarSametype() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_const-to-var_sametype.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationVarNobecomesVoid() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_var_nobecomes_void.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  */
  @Test
  public void testAppliedOccurrenceVarToConstSametype() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_var-to-const_sametype.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Operator binding, contrived examples

  */
  @Test
  public void testOperatorBinding() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/operator_binding.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  infer return type

  */
  @Test
  public void testFunctionAssignInferReturn() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/function_assign_infer_return.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  infer return type

  */
  @Test
  public void testFunctionAssignReturn() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/function_assign_return.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Definition of variables

  */
  @Test
  public void testDeclarationVarNobecomesAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_var_nobecomes_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntbooltoint() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intbooltoint.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=MismatchedTokenException.class)
  public void testDeclarationConstNobecomesAlltypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_const_nobecomes_alltypes.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testAppliedOccurrenceConstTypeconflict() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_const_typeconflict.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   n!

  */
  @Test
  public void testPopularFaculty() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/popular_faculty.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
   Simple while loop - no parentheses

  */
  @Test
  public void testWhileNobraces() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/while_nobraces.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntstringtoint() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intstringtoint.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInfervarBecomesNonInferrableConflictingTypes() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_infervar_becomes_non-inferrable_conflicting_types.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testAppliedOccurrenceVarTypeconflict() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/applied_occurrence_var_typeconflict.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    /**
  Rekenen
  Comment

  */
  @Test
  public void testPopularHelloworld() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/popular_helloworld.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testTypeIncorrectIntstringtostring() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/type_incorrect_intstringtostring.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
    @Test(expected=SymbolTableException.class)
  public void testDeclarationInfervarBecomesNonInferrableUnknownPartOther() throws IOException, RecognitionException, URISyntaxException{
    File f = new File(getClass().getResource("/declaration_infervar_becomes_non-inferrable_unknown_part_other.ex").toURI());
    CodegenPreparation prepare = createCodegenPreparation(asCharStream(f), createParser(asCharStream(f)));
    prepare.program();
  }
  
}
