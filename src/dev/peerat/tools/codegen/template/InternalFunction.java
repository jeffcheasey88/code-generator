package dev.peerat.tools.codegen.template;

import static dev.peerat.parser.java.visitor.JavaVisitor.classBase;
import static dev.peerat.parser.java.visitor.JavaVisitor.collect;
import static dev.peerat.parser.java.visitor.JavaVisitor.function;

import java.util.List;
import java.util.function.Consumer;

import dev.peerat.parser.java.ClassBase;
import dev.peerat.parser.java.Function;
import dev.peerat.parser.java.Variable;
import dev.peerat.parser.java.builder.JavaBuilder;
import dev.peerat.parser.java.builder.JavaFunctionBuilder;
import dev.peerat.parser.java.visitor.JavaClassBaseVisitor;
import dev.peerat.parser.java.visitor.JavaFunctionVisitor;

public class InternalFunction{

	private String name;
	private List<Variable> parameters;

	public InternalFunction(String name, List<Variable> parameters){
		this.name = name;
		this.parameters = parameters;
	}
	
	public boolean exist(ClassBase base){
		return get(base) != null;
	}
	
	public Function createIfAbsent(ClassBase base) throws Exception{
		return createIfAbsent(base, null);
	}
	
	public Function createIfAbsent(ClassBase base, Consumer<JavaFunctionBuilder> builder) throws Exception{
		Function function = get(base);
		return function != null ? function : create(base, builder);
	}
	
	public Function create(ClassBase base) throws Exception{
		return create(base, null);
	}
	
	public Function create(ClassBase base, Consumer<JavaFunctionBuilder> builder) throws Exception{
		JavaFunctionBuilder functionBuilder = JavaBuilder.ofFunction(null, this.name);
		if(this.parameters != null){
			for(Variable variable : this.parameters) functionBuilder.parameter(variable);
		}
		if(builder != null) builder.accept(functionBuilder);
		Function function = functionBuilder.build();
		base.addFunction(function);
		return function;
	}
	
	private JavaFunctionVisitor visitor(){
		return function()
				.name(s -> s.equals(this.name))
				.validate(f -> {
					if((f.getParameters() == null || f.getParameters().isEmpty()) && (this.parameters == null || this.parameters.isEmpty())) return true;
					if(f.getParameters().size() == this.parameters.size()){
						for(int index = 0; index < this.parameters.size(); index++){
							Variable functionParameter = f.getParameters().get(index);
							Variable currentParameter = this.parameters.get(index);
							if(!functionParameter.getType().toString().equals(currentParameter.getType().toString())) return false;
						}
						return true;
					}
					return false;
				});
	}
	
	public JavaClassBaseVisitor visitor(Consumer<JavaFunctionVisitor> visitorConsumer){
		JavaFunctionVisitor visitor = visitor();
		visitorConsumer.accept(visitor);
		return classBase().oneChild(visitor);
	}
	
	public Function get(ClassBase base){
		return base.visit(classBase().oneChild(collect(visitor()))).toElement();
	}
	
	public boolean remove(ClassBase base){
		Function function = get(base);
		if(function == null) return false;
		base.getElements().remove(function);
		return true;
	}
}
