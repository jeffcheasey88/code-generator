package dev.peerat.tools.codegen.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExecutionContext{
	
	private List<Task> history;
	private Map<Task, List<Object[]>> parameters;
	private Object[] dependencies;
	
	public ExecutionContext(){
		this.history = new LinkedList<>();
		this.parameters = new HashMap<>();
	}
	
	public List<Task> getHistory(){
		return new LinkedList<>(history);
	}
	
	public <T> T getDependency(Class<T> type){
		Object assignable = null;
		boolean multiple = false;
		for(Object dependency : dependencies){
			if(dependency.getClass().equals(type)) return (T) dependency;
			if(type.isAssignableFrom(dependency.getClass())){
				if(assignable != null){
					multiple = true;
					continue;
				}
				assignable = dependency;
			}
		}
		if(multiple) throw new IllegalArgumentException("Multiple dependencies can be assign from "+type);
		return (T) assignable;
	}

	void task(Task task){
		this.history.add(task);
	}
	
	void done(TaskResult<?> result){
		Task last = this.history.remove(this.history.size()-1);
		
		List<Object[]> list = this.parameters.get(last);
		if(list != null){
			this.parameters.remove(last);
			for(Object[] parameters : list){
				task(last);
				TaskResult<?> currentResult = last.execute(parameters);
				done(currentResult);
				result.push(currentResult);
			}
		}
	}
	
	Object[] getDependencies(){
		return this.dependencies;
	}
	
	void addDependencies(Object[] dependencies){
		if(this.dependencies == null){
			this.dependencies = dependencies;
			return;
		}
		Object[] copy = new Object[this.dependencies.length+dependencies.length];
		System.arraycopy(this.dependencies, 0, copy, 0, this.dependencies.length);
		System.arraycopy(dependencies, 0, copy, this.dependencies.length, dependencies.length);
		this.dependencies = copy;
	}
	
	void removeDependencies(Object[] dependencies){
		if(this.dependencies == null || this.dependencies.length == 0) return;
		int position = -1;
		for(int i = 0; i < (this.dependencies.length-dependencies.length)+1; i++){
			boolean find = true;
			for(int index = 0; index < dependencies.length; index++){
				if(this.dependencies[i+index] != dependencies[index]){
					find = false;
					break;
				}
			}
			if(find){
				position = i;
				break;
			}
		}
		if(position < 0) return;
		Object[] copy = new Object[this.dependencies.length-dependencies.length];
		System.arraycopy(this.dependencies, 0, copy, 0, position);
		System.arraycopy(this.dependencies, position+dependencies.length, copy, position, this.dependencies.length-(position+1));
		this.dependencies = copy;
	}
	
	void duplicate(String name, Class<?>[] types, Object[] parameters){
		Task target = null;
		for(Task task : history){
			if(task.is(name) && task.isAssignable(types)){
				target = task;
				break;
			}
		}
		List<Object[]> list = this.parameters.get(target);
		if(list == null) this.parameters.put(target, list = new LinkedList<>());
		list.add(parameters);
	}
}
