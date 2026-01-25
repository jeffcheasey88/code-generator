package dev.peerat.tools.codegen.engine;

public class TaskResult<T>{
	
	private Object result;
	
	public TaskResult(Object result){
		this.result = result;
	}

	public T get(){
		return (T) this.result;
	}
}
