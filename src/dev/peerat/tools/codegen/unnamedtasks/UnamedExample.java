package dev.peerat.tools.codegen.unnamedtasks;

import static dev.peerat.parser.java.builder.JavaBuilder.ofClass;
import static dev.peerat.parser.java.builder.JavaBuilder.ofFile;
import static dev.peerat.parser.java.builder.JavaBuilder.ofVariable;
import static dev.peerat.parser.java.visitor.JavaVisitor.allClass;
import static dev.peerat.parser.java.visitor.JavaVisitor.allVariable;
import static dev.peerat.parser.java.visitor.JavaVisitor.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.peerat.parser.java.ClassBase;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.Variable;
import dev.peerat.tools.codegen.engine.ElementEngine;
import dev.peerat.tools.codegen.engine.TaskResult;

public class UnamedExample{
	
	public static void main(String[] args){
		
		ElementEngine engine = new ElementEngine();
		
		engine.rule("create accessor", JavaProject.class, (project) -> {
			System.out.println("create accessor JavaProject");
			List<dev.peerat.parser.java.Class> classes = project.visit(collect(allClass())).toList();
			
			for(dev.peerat.parser.java.Class clazz : classes) engine.task("create accessor", clazz);
			return project;
		});
		
		engine.rule("create accessor", dev.peerat.parser.java.Class.class, (clazz) -> {
			System.out.println("create accessor Class");
			List<Variable> variables = new ArrayList<>();
			engine.context(() -> {
				variables.addAll(engine.<List<Variable>>task("all variables").get());
			},clazz);
			
			for(Variable variable : variables){
				TaskResult<Variable> result = engine.task("create accessor", variable);
			}
			
			return clazz;
		});
		
		engine.rule("create accessor", dev.peerat.parser.java.Class.class, Variable.class, (clazz, variable) -> {
			System.out.println("create accessor Class, Variable");
			if(variable.getType().getName().getValue().equals("Integer")){
				engine.duplicate("create accessor", dev.peerat.parser.java.Class.class, Variable.class, clazz, ofVariable(variable).setType("int").build());
			}
			
			return variable;
		});
		
		engine.rule("all variables", ClassBase.class, (classBase) -> {
			System.out.println("all variables ClassBase");
			List<Variable> variables = classBase.visit(collect(allVariable())).toList();
			
			if(classBase instanceof dev.peerat.parser.java.Class){
				dev.peerat.parser.java.Class clazz = (dev.peerat.parser.java.Class)classBase;
				if(clazz.getExtension() != null) variables.addAll(engine.<Collection<Variable>>task("all variables", engine.task("find clazz", clazz.getExtension()).get()).get());
			}
			
			return variables;
		});
		
		JavaProject p0 = new JavaProject();
		p0.addFile(ofFile().clazz(ofClass("Test").element(ofVariable("Integer", "i"))).build(), false);
		engine.task("create accessor", p0);
	}

}
