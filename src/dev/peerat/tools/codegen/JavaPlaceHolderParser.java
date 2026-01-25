package dev.peerat.tools.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.value.AnnotationValue;
import dev.peerat.parser.java.value.ArrayAccessValue;
import dev.peerat.parser.java.value.ArrayValue;
import dev.peerat.parser.java.value.BiValue;
import dev.peerat.parser.java.value.CastValue;
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
		if(value instanceof AnnotationValue) return predicate((AnnotationValue)value);
		if(value instanceof ArrayAccessValue) return predicate((ArrayAccessValue)value);
		if(value instanceof ArrayValue) return predicate((ArrayValue)value);
		if(value instanceof CastValue) return predicate((CastValue)value);
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
	
	private Predicate<JavaElement> predicate(AnnotationValue value){
		Predicate<JavaElement>  annotation = elementPredicate(value.getAnnotation());
		return e -> { if(e instanceof AnnotationValue) return annotation.test(((AnnotationValue)e).getAnnotation());  return false; };
	}
	
	private Predicate<JavaElement> predicate(ArrayAccessValue value){
		Predicate<JavaElement> basePredicate = valuePredicate(value.base());
		Predicate<JavaElement> accessPredicate = valuePredicate(value.getAccessor());
		return e -> {
			if(e instanceof ArrayAccessValue){
				ArrayAccessValue arrayAccess = (ArrayAccessValue)e;
				return basePredicate.test(arrayAccess.base()) && accessPredicate.test(arrayAccess.getAccessor());
			}
			return false;
		};
	}
	
	private Predicate<JavaElement> predicate(ArrayValue value){
		List<Predicate<JavaElement>> elements = new ArrayList<>();
		for(Value element : value.getValues()) elements.add(elementPredicate(element));
		return e -> {
			if(e instanceof ArrayValue){
				ArrayValue arrayValue = (ArrayValue)e;
				if(arrayValue.getValues().size() != elements.size()) return false;
				for(int i = 0; i < elements.size(); i++){
					if(!elements.get(i).test(arrayValue.getValues().get(i))) return false;
				}
				return true;
			}
			return false;
		};
	}
	
	private Predicate<JavaElement> predicate(CastValue value){
		Predicate<JavaElement> typePredicate = elementPredicate(value.getType());
		Predicate<JavaElement> valuePredicate = valuePredicate(value.getValue());
		return e -> {
			if(e instanceof CastValue){
				CastValue castValue = (CastValue)e;
				return typePredicate.test(castValue.getType()) && valuePredicate.test(castValue.getValue());
			}
			return false;
		};
	}
	
}
