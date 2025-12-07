package dev.peerat.tools.codegen.template;

import static dev.peerat.parser.java.visitor.JavaVisitor.classBase;
import static dev.peerat.parser.java.visitor.JavaVisitor.collect;
import static dev.peerat.parser.java.visitor.JavaVisitor.variable;

import java.util.function.Consumer;

import dev.peerat.parser.java.ClassBase;
import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.Variable;
import dev.peerat.parser.java.builder.JavaBuilder;
import dev.peerat.parser.java.builder.JavaVariableBuilder;
import dev.peerat.parser.java.value.Value;
import dev.peerat.parser.java.visitor.JavaVariableVisitor;
import dev.peerat.parser.visitor.Visitor;

public class InternalVariable{
	
	private String type;
	private String name;
	private Value value;
	
	public InternalVariable(String type, String name, Value value){
		this.type = type;
		this.name = name;
		this.value = value;
	}
	
	public InternalVariable(String type, String name, String value){
		this(type, name, JavaBuilder.ofValue(value));
	}

	public boolean exist(ClassBase clazz){
		return get(clazz) != null;
	}
	
	public Variable createIfAbsent(ClassBase clazz) throws Exception{
		return createIfAbsent(clazz, null);
	}
	
	public Variable createIfAbsent(ClassBase clazz, Consumer<JavaVariableBuilder> builder) throws Exception{
		Variable variable = get(clazz);
		return variable != null ? variable : create(clazz, builder);
	}
	
	public Variable create(ClassBase clazz) throws Exception{
		return create(clazz, null);
	}
	
	public Variable create(ClassBase clazz, Consumer<JavaVariableBuilder> builder) throws Exception{
		JavaVariableBuilder variableBuilder = JavaBuilder.ofVariable(type, name, value);
		if(builder != null) builder.accept(variableBuilder);
		Variable variable = variableBuilder.build();
		clazz.addVariable(variable);
		return variable;
	}
	
	public JavaVariableVisitor visitor(){
		return variable().name(s -> s.equals(this.name)).type(s -> s.equals(this.type));
	}

	public Visitor<JavaElement> visitor(Consumer<JavaVariableVisitor> visitorConsumer){
		JavaVariableVisitor visitor = visitor();
		visitorConsumer.accept(visitor);
		return classBase().oneChild(visitor);
	}
	
	public Variable get(ClassBase clazz){
		return clazz.visit(classBase().oneChild(collect(visitor()))).toElement();
	}
	
	public boolean remove(ClassBase clazz){
		Variable variable = get(clazz);
		if(clazz == null) return false;
		clazz.getElements().remove(variable);
		return true;
	}
}
