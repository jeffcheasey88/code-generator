package dev.peerat.tools.codegen.engine;

public class Task{
	
	private String name;
	private Executor executor;
	private Class<?>[] parameters;
	
	public Task(String name, Executor executor, Class<?>[] parameters){
		this.name = name;
		this.executor = executor;
		this.parameters = parameters;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean is(String name){
		return this.name.equals(name);
	}
	
	public boolean isAssignable(Object[] parameters){
		if(this.parameters.length != parameters.length) return false; //TODO CONTEXT
		for(int i = 0; i < this.parameters.length; i++){
			if(parameters[i] == null) continue;
			if(!this.parameters[i].isAssignableFrom(parameters[i].getClass())) return false;
		}
		return true;
	}
	
	public boolean isAssignable(Class<?>[] parameterTypes){
		if(this.parameters.length != parameterTypes.length) return false;
		for(int i = 0; i < this.parameters.length; i++){
			if(!this.parameters[i].equals(parameterTypes[i])) return false;
		}
		return true;
	}
	
	public <T> TaskResult<T> execute(Object[] parameters){
		return new TaskResult<>(this.executor.exec(parameters));
	}
	
}
