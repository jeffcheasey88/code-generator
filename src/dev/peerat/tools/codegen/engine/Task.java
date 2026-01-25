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
	
	public boolean is(String name){
		return this.name.equals(name);
	}
	
	Class<?>[] getParameters(){
		return this.parameters;
	}
	
	public <T> TaskResult<T> execute(Object[] parameters){
		return new TaskResult<>(this.executor.exec(parameters));
	}
	
}
