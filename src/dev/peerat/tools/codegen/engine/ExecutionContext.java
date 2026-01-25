package dev.peerat.tools.codegen.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExecutionContext{
	
	private List<Task> history;
	private Map<Task, List<Object[]>> parameters;
	
	public ExecutionContext(){
		this.history = new LinkedList<>();
		this.parameters = new HashMap<>();
	}

	public void task(Task task){
		this.history.add(task);
	}
	
	public void done(TaskResult<?> result){
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
	
	public void duplicate(String name, Class<?>[] types, Object[] parameters){
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
