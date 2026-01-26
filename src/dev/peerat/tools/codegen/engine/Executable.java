package dev.peerat.tools.codegen.engine;

public class Executable{
	
	private Task task;
	private Object[] parameters;
	
	public Executable(Task task, Object[] parameters){
		this.task = task;
		this.parameters = parameters;
	}
	
	public Task getTask(){
		return this.task;
	}
	
	public Object[] getParameters(){
		return this.parameters;
	}

}
