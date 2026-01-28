package dev.peerat.tools.codegen.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Task{
	
	private String name;
	private Executor executor;
	private Class<?>[] parameters;
	private Function<Object, Object>[] mappers;
	
	public Task(String name, Executor executor, Class<?>[] parameters, Function<Object, Object>[] mappers){
		this.name = name;
		this.executor = executor;
		this.parameters = parameters;
		this.mappers = mappers;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getParameterCount(){
		return this.parameters.length;
	}
	
	public Class<?>[] getParameters(){
		return this.parameters;
	}
	
	public Function<Object, Object>[] getMappers(){
		return this.mappers;
	}
	
	public Executor getExecutor(){
		return this.executor;
	}
	
	public boolean is(String name){
		return this.name.equals(name);
	}
	
	public Object[] isAssignable(Object[] parameters){
		if(parameters.length < this.parameters.length) return null;
		Object[] result = new Object[this.parameters.length];
		List<Object> list = new ArrayList<>(parameters.length);
		for(Object obj : parameters) list.add(obj);
		int index;
		int resultIndex = 0;
		for(Class<?> type : this.parameters){
			index = -1;
			for(int i = 0; i < list.size(); i++){
				Object parameter = list.get(i);
				if(parameter == null) index = i;
				else if(type.isAssignableFrom(parameter.getClass())){
					index = i;
					break;
				}
			}
			if(index < 0) return null;
			result[resultIndex++] = list.remove(index);
		}
		return result;
	}
	
	public boolean isAssignable(Class<?>[] parameterTypes){
		if(this.parameters.length != parameterTypes.length) return false;
		for(int i = 0; i < this.parameters.length; i++){
			if(!this.parameters[i].equals(parameterTypes[i])) return false;
		}
		return true;
	}
	
	public <T> TaskResult<T> execute(Object[] parameters){
		if(mappers != null){
			for(int i = 0; i < this.mappers.length; i++){
				Function<Object, Object> mapper = this.mappers[i];
				if(mapper == null) continue;
				parameters[i] = mapper.apply(parameters[i]);
			}
		}
		return new TaskResult<>(this.executor.exec(parameters));
	}
	
}
