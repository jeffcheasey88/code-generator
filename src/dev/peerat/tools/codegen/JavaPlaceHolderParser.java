package dev.peerat.tools.codegen;

import java.util.function.Predicate;

import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.value.BiValue;
import dev.peerat.parser.java.value.StaticValue;
import dev.peerat.parser.java.value.Value;
import dev.peerat.tools.codegen.CodeParser.Parser;

public class JavaPlaceHolderParser extends Parser{

	public JavaPlaceHolderParser(){
		super(0, parser -> parser.setTokenizer(new JavaPlaceHolderTokenizer()));
	}
	
	public Predicate<JavaElement> parse(String code) throws Exception{
		CodeParser parsed = new CodeParser();
		super.parse(parsed, code);
		
		JavaElement result = parsed.getElement();
		return elementPredicate(result);
	}
	
	private Predicate<JavaElement> elementPredicate(JavaElement element){
		if(element instanceof Value) return valuePredicate((Value)element);
		return null;
	}  
	
	private Predicate<JavaElement> valuePredicate(Value value){
		if(value instanceof StaticValue) return predicate((StaticValue)value);
		if(value instanceof BiValue) return predicate((BiValue)value);
		return null;
	}
	
	private Predicate<JavaElement> predicate(StaticValue value){
		String exactValue = value.getToken().getValue();
		return value.getToken().getValue().equals("<!>") ? e -> true : e -> e instanceof StaticValue ? ((StaticValue)e).getToken().getValue().equals(exactValue) : false;
	}
	
	private Predicate<JavaElement> predicate(BiValue value){
		Predicate<JavaElement> leftPredicate = valuePredicate(value.left());
		Predicate<JavaElement> rightPredicate = valuePredicate(value.right());
		String action = value.getAction().getValue();
		return e -> {
			if(e instanceof BiValue){
				BiValue biValue = (BiValue)e;
				return biValue.getAction().getValue().equals(action) && leftPredicate.test(biValue.left()) && rightPredicate.test(biValue.right());
			}
			return false;
		};
	}
}
