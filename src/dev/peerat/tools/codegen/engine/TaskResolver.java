package dev.peerat.tools.codegen.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskResolver{
	
	private Map<String, List<Task>> tasks;
	
	public TaskResolver(){
		this.tasks = new HashMap<>();
	}
	
	public Task resolveTask(String name, Object[] parameters){
		List<Task> list = this.tasks.get(name);
		if(list == null) return null;
		for(Task task : list){
			Class<?>[] parameterTypes = task.getParameters();
			//TODO CONTEXT
			if(parameterTypes.length != parameters.length) continue;
			boolean find = true;
			for(int i = 0; i < parameterTypes.length; i++){
				if(parameters[i] == null) continue;
				if(!parameterTypes[i].isAssignableFrom(parameters[i].getClass())){
					find = false;
					break;
				}
			}
			if(find) return task;
		}
		return null;
	}
	
	public void rule(String name, Class<?>[] types, Executor executor){
		List<Task> list = this.tasks.get(name);
		if(list == null) this.tasks.put(name, list = new ArrayList<>());
		list.add(new Task(name, executor, types));
	}
	

}
