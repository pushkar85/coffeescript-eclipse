package csep.tests

import csep.parser.Lexer
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.Test
import junit.framework.Assert

@org.junit.runner.RunWith(typeof(XtextRunner))
@InjectWith(typeof(InjectorProviderCustom))
class LexerTest {
	
	@Test
	def void testBasic() {
		check('''
			number = 42
			answer=42
		''',
		'''
IDENTIFIER:number
EQUAL:=
NUMBER:42
TERMINATOR:
IDENTIFIER:answer
EQUAL:=
NUMBER:42
TERMINATOR:
''')
	}

    @Test
    def void testIncomplete() {
    	checkCount('', 0)
    	checkCount('if ', 2)
		checkCount('if a ', 3)
	}
	
	@Test
	def void testMoreOutdents() {
		checkCount('''
		  a = 1
		    b
		  c
		  ''', 9)
    }
    
	def void check(CharSequence input, CharSequence expectedStr) {
		val lexer = new Lexer(input)
		val tokens = lexer.tokenizeToStrings()
		val tokensStr = tokens.join("\n")
		Assert::assertEquals(expectedStr.toString().trim(), tokensStr)
	}
	
	def void checkCount(CharSequence input, int expectLength) {
		val lexer = new Lexer(input)
		val tokens = lexer.tokenizeToStrings()
		Assert::assertEquals(expectLength, tokens.size())
	}
	
}