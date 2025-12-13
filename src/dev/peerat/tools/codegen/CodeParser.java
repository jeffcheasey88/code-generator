package dev.peerat.tools.codegen;

import java.util.List;
import java.util.function.Consumer;

import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.JavaParser;
import dev.peerat.parser.java.JavaTokenizer;
import dev.peerat.parser.java.tree.JavaTreeType;
import dev.peerat.parser.tokens.Token;
import dev.peerat.parser.tokens.TokenizerResult;
import dev.peerat.tools.refractor.internal.RefractorEngine.JavaContainer;

public class CodeParser{
	
	private static final Parser PARSER = new Parser();
	
	private JavaElement element;
	private List<Token> tokens;
	
	public CodeParser(String code) throws Exception{
		PARSER.parse(this, code);
	}
	
	public CodeParser(){}
	
	public JavaElement getElement(){
		return this.element;
	}
	
	public List<Token> getTokens(){
		return this.tokens;
	}
	
	static class Parser{
		
		private JavaParser[] parsers;
		
		Parser(){
			this(JavaTokenizer.ALL, parser -> {});
		}
		
		Parser(int options, Consumer<JavaParser> modifier){
			JavaTreeType[] values = JavaTreeType.values();
			this.parsers = new JavaParser[values.length];
			for(int i = values.length-1, j = 0; i >= 0; i--, j++){
				JavaParser parser = new JavaParser(values[i], options);
				modifier.accept(parser);
				this.parsers[j] = parser;
			}
		}
		
		void parse(CodeParser result, String code) throws Exception{
			JavaContainer container = new JavaContainer();
			TokenizerResult tokens = null;
			for(int i = 0; i < parsers.length-1; i++){
				try {
					tokens = this.parsers[i].parse(code, container);
					result.element = container.getElement();
					if(result.element == null) continue;
					result.tokens = tokens.getTokens();
					return;
				} catch (Exception e){}
			}
			JavaFile file = new JavaFile();
			tokens = this.parsers[this.parsers.length-1].parse(code, file);
			result.element = file;
			result.tokens = tokens.getTokens();
		}
	}

}
