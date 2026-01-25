package dev.peerat.tools.codegen.unnamedtasks;

import dev.peerat.tools.codegen.engine.ElementEngine;

public class UnamedExample{
	
	public static void main(String[] args){
		
		ElementEngine engine = new ElementEngine();
		
		engine.rule("square", Integer.class, (i) -> engine.task("multiply", i,i).get());
		engine.rule("multiply", Integer.class, Integer.class, (a,b) -> a*b);
		
		System.out.println(engine.task("square", 5).get());
		
		/*
		task("create accessor", project);
		
		rule("create accessor", JavaProject.class, (project) -> {
			List<Class> classes = project.visit(collect(allClass())).toList();
			
			for(Class clazz : classes) task("create accessor", clazz);
			return project;
		});
		
		rule("create accessor", Class.class, (clazz) -> {
			List<Variable> variables = task("all variables", clazz);
			
			for() task("create accessor", clazz, variable);
		});
		
		rule("create accessor", Class.class, Variable.class (clazz, variable) -> {
			if(variable.getType().equals("Integer")){
				duplicate("create accessor", Class.class, Variable.class, clazz, variable with int);
			}
		});
		
		rule("all variables", ClassBase.class, (classBase) -> {
			List<Variable> variables = classBase.visit(collect(allVariable()).toList();
			
			if(classBase.getExtension() != null) variables.addAll(task("all variables", task("find clazz", classBase.getExtension)));
			
			return variables;
		});
		
		*/
		
	}

}
