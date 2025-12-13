package dev.peerat.tools.codegen;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import dev.peerat.parser.java.JavaTokenType;
import dev.peerat.parser.tokens.Token;
import dev.peerat.parser.tokens.Tokenizer;
import dev.peerat.parser.tokens.TokenizerResult;

public class JavaPlaceHolderTokenizer implements Tokenizer{

	private static final Set<String> RESERVED_KEYWORD = new HashSet<>(
			Arrays.asList("abstract","assert","break","case","catch","continue","default","do","else","enum","extends","final","finally","for","if","implements","import","instanceof","interface","native","new","package","private","protected","public","return","static","strictfp","switch","synchronized","throw","throws","transient","try","volatile","while")
			);
	
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("^([0-9]+(_[0-9]+)*[lLfFdD]?|[0-9]+(_[0-9]+)*\\.[0-9]*(_[0-9]+)*([eE][+-]?[0-9]+(_[0-9]+)*)?[fFdD]?|\\.[0-9]+(_[0-9]+)*([eE][+-]?[0-9]+(_[0-9]+)*)?[fFdD]?|[0-9]+(_[0-9]+)*[eE][+-]?[0-9]+(_[0-9]+)*[fFdD]?|0[xX][0-9a-fA-F]+(_[0-9a-fA-F]+)*(\\.[0-9a-fA-F]+(_[0-9a-fA-F]+)*)?[pP][+-]?[0-9]+(_[0-9]+)*[fFdD]?|0[xX][0-9a-fA-F]+(_[0-9a-fA-F]+)*[lL]?|0[bB][01]+(_[01]+)*[lL]?|0[0-7]+(_[0-7]+)*[lL]?)$");

	private TokenizerResult parse(String[] lines){
		List<Token> tokens = new ArrayList<>();
		
		String stringBloc = null;
		
		for(int index = 0; index < lines.length; index++){
			int lineNumber = index+1;
			String line = lines[index];
			
			for(int i = 0; i < line.length(); i++){
				char c = line.charAt(i);
				if(stringBloc != null){
					if(c == '"'){
						if((i < line.length()-2 && line.charAt(i+1) == '"' && line.charAt(i+2) == '"')){
							Token token = new Token(lineNumber, i+1, stringBloc, JavaTokenType.STRING);
							tokens.add(token);
							stringBloc = null;
							i+=2;
							continue;
						}
					}
					stringBloc+=c;
					continue;
				}
				Token token = null;
				if(isNameValid(c)){
					String value = "";
					int j = i;
					for(; j < line.length(); j++){
						c = line.charAt(j);
						if(isNameValid(c)) value+=c;
						else break;
					}
					
					int baseCharIndex = i;
					i = j-1;
					boolean numeric = false;
					if((i+1) < line.length() && line.charAt(i+1) == '.'){
						String numericValue = value;
						int k = i;
						while((k+1) < line.length() && isNumericValid(line.charAt(k+1))){
							++k;
							numericValue+=line.charAt(k);
						}
						
						while(k > i && !isNumeric(numericValue)){
							k--;
							numericValue = numericValue.substring(0, numericValue.length()-1);
						}
						
						if(isNumeric(numericValue)){
							token = new Token(lineNumber, i+1, numericValue, JavaTokenType.NUMERIC);
							numeric = true;
							i = k;
						}
					}
					
					if(!numeric){
						if(isNumeric(value)){
							token = new Token(lineNumber, baseCharIndex+1, value, JavaTokenType.NUMERIC);
						}else{
							token = new Token(lineNumber, baseCharIndex+1, value, RESERVED_KEYWORD.contains(value) ? JavaTokenType.RESERVED : JavaTokenType.NAME);
						}
					}
				}else if(Character.isWhitespace(c)){
					continue;
				}
				else{
					if(c == '"'){
						String value;
						int j;
						if((i < line.length()-2 && line.charAt(i+1) == '"' && line.charAt(i+2) == '"')){
							value = "\"\"\"";
							j = i+3;
							boolean end = false;
							for(; j < line.length(); j++){
								c = line.charAt(j);
								if(c == '"' && j < line.length()-2 && line.charAt(j+1) == '"' && line.charAt(j+2) == '"'){
									value+="\"\"\"";
									end = true;
									j+=3;
									break;
								}
								value+=c;
							}
							if(!end){
								stringBloc = value;
								continue;
							}
						}else{
							value = "\"";
							j = i+1;
							for(; j < line.length(); j++){
								c = line.charAt(j);
								if(c == '"'){
									value+=c;
									j++;
									break;
								}else if(c == '\\'){
									value+=c+line.charAt(++j);
								}else{
									value+=c;
								}
							}
						}
						token = new Token(lineNumber, i+1, value, JavaTokenType.STRING);
						i = j-1;
					}else if(c == '\''){
						String value = "'";
						int j = i+1;
						for(; j < line.length(); j++){
							c = line.charAt(j);
							if(c == '\''){
								value+=c;
								j++;
								break;
							}else if(c == '\\'){
								value+=c+line.charAt(++j);
							}else{
								value+=c;
							}
						}
						token = new Token(lineNumber, i+1, value, JavaTokenType.CHAR);
						i = j-1;
					}else{
						if(c == '<' && (i+2) < line.length()){
							if(line.charAt(i+1) == '!' && line.charAt(i+2) == '>'){
								tokens.add(new Token(lineNumber, i+1, "<!>", JavaTokenType.NAME));
								i+=2;
								continue;
							}
						}
						boolean numeric = false;
						if(c == '.' && (i+1) < line.length()){
							String value = c+""+line.charAt(i+1);
							if(isNumeric(value)){
								++i;
								String buffer;
								while((i+1) < line.length() && isNumeric(buffer = value+line.charAt(i+1))){
									++i;
									value = buffer;
								}
								token = new Token(lineNumber, i+1, value, JavaTokenType.NUMERIC);
								numeric = true;
							}
						}
						if(!numeric) token = new Token(lineNumber, i+1, ""+c, JavaTokenType.DELIMITER);
					}
				}
				tokens.add(token);
			}
		}
		return new TokenizerResult(tokens, tokens, null, null);
	}
	
	@Override
	public TokenizerResult parse(BufferedReader reader) throws Exception{
		List<String> lines = new ArrayList<>();
		String line;
		while((line = reader.readLine()) != null) lines.add(line);
		
		return parse(lines.toArray(new String[0]));
	}
	
	@Override
	public TokenizerResult parse(String source){
		return parse(source.split("\n"));
	}
	
	private boolean isNumeric(String value){
		return NUMERIC_PATTERN.matcher(value).matches();
	}
	
	public boolean isNumericValid(char c){
		return c == '_' || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == 'l' || c== 'L' || c == 'p' || c == 'P' || c == 'x' || c == 'X' || c == '-' || c == '+' || c == '.' || Character.isDigit(c);
	}
	
	private boolean isNameValid(char c) {
		return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '$';
	}
	
}
