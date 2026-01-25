package dev.peerat.tools.codegen.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TaskResult<T>{
	
	private Object result;
	private List<Object> results;
	
	public TaskResult(Object result){
		this.result = result;
	}
	
	void push(TaskResult<?> result){
		if(this.results == null){
			this.results = new ArrayList<>();
			this.results.add(this.result);
			this.result = null;
		}
		if(result.results == null){
			this.results.add(result.result);
		}else{
			this.results.addAll(result.results);
		}
	}

	public T get(){
		return (T) this.result;
	}
	
	public Collection<T> list(){
		return (Collection<T>)  (this.results == null ? Arrays.asList(this.result) :  this.results);
	}
}
