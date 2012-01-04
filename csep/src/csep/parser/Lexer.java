package csep.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;

import com.aptana.editor.coffee.parsing.Terminals;
import com.aptana.editor.coffee.parsing.lexer.CoffeeScanner;
import com.aptana.editor.coffee.parsing.lexer.CoffeeSymbol;

public class Lexer extends csep.parser.antlr.internal.InternalCoffeeScriptLexer {
	private final static Logger logger = Logger.getLogger(Lexer.class);
	private CoffeeScanner aptanaScanner;
	private CommonToken prevToken = null;
	/**
	 * We have to keep a track of it, because the aptana scanner doesn't do that
	 */
	private int tokenIndex = 0;

	public Lexer(CharStream in) {
		super(in);
		aptanaScanner = new CoffeeScanner();
		String content = in.substring(0, in.size() - 1);
		aptanaScanner.setSource(content);
	}

	public Lexer(String str) {
		this(new ANTLRStringStream(str));
	}

	/**
	 * To enable xtend test cases with multiline strings
	 * @param str
	 */
	public Lexer(CharSequence str) {
		this(str.toString());
	}

	/**
	 * Get next token.  If an exception is thrown by the underlying lexer,
	 * keep calling it, and append an invalid token at the very end.
	 */
	@Override
	public Token nextToken() {
		Token token = null;
		CoffeeSymbol symbol = null;
		try {
			symbol = aptanaScanner.nextAnyToken();
			if (symbol == null) {
				// XXX: why do we get a null symbol?
				token = CommonToken.INVALID_TOKEN;
			}
			else if (symbol.getId() == Terminals.EOF) {
				token = CommonToken.EOF_TOKEN;
			}
			else {
				token = new BeaverToken(symbol);
			}
		}
		catch (Exception e) {
			// Xtext wants token to be CommonToken, INVALID_TOKEN_TYPE, and HIDDEN_CHANNEL
			String text = e.getLocalizedMessage();
			if (text == null)
				text = "simply " + e.getClass().getSimpleName();
			CommonToken ct = new CommonToken(Token.INVALID_TOKEN_TYPE,
					text);
			ct.setChannel(Token.HIDDEN_CHANNEL);
			if (prevToken != null) {
				int start = prevToken.getStopIndex() + 1;
				int stop = start + 1; // TODO: get more informative errors with length of token
				ct.setStartIndex(start);
				ct.setStopIndex(stop);
			}
			token = ct;
		}
		token.setTokenIndex(tokenIndex);
		if (symbol != null && symbol.hidden)
			token.setChannel(Token.HIDDEN_CHANNEL);
		tokenIndex++;
		if (token instanceof CommonToken) {
			if (prevToken != null && token.getType() > 0) {
				/*
				TODO: Ensure that token positions are increasing
				assert ((CommonToken)token).getStartIndex() >= prevToken.getStopIndex():
					"Position not follows, prevToken: " + prevToken + ", token: " + token;
				*/
			}
			prevToken = (CommonToken)token;
		}
		logger.debug("token: " + token);
		return token;
	}
	
	/**
	 * Read the whole input and tokenize it
	 * 
	 * @return a list of tokens
	 * @throws Exception
	 */
	public List<Token> tokenize() throws Exception {
		List<Token> symbols = new ArrayList<Token>();
		while (true) {
			Token token = nextToken();
			if (Token.EOF == token.getType()) {
				break;
			} else {
				symbols.add(token);
			}
		}
		return symbols;
	}

	/**
	 * Tokenize the whole input, skipping hidden terminals
	 * 
	 * @return a simplified string representation of the token stream in the
	 *         form of "tokenName:text"
	 * @throws Exception
	 */
	public List<String> tokenizeToStrings() throws Exception {
		List<String> strings = new ArrayList<String>();
		for (Token t : tokenize()) {
			if (t.getChannel() == Token.DEFAULT_CHANNEL) {
				strings.add(Helper.getNameAndText(t));
			}
		}
		return strings;
	}

	@Override
	/**
	 * Because super gets the message from an empty map
	 */
	public String getErrorMessage(Token t) {
		String message = super.getErrorMessage(t);
		if (message == null)
			message = t.getText();
		if (message == null)
			message = t.toString();
		return message;
	}
}
