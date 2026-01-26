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
	
	public Executable resolveTask(String name, Object[] parameters){
		List<Task> list = this.tasks.get(name);
		if(list == null) return null;
		for(Task task : list){
			Object[] result = task.isAssignable(parameters);
			if(result != null) return new Executable(task, result);
		}
		return null;
	}
	
	public void rule(String name, Class<?>[] types, Executor executor){
		List<Task> list = this.tasks.get(name);
		if(list == null) this.tasks.put(name, list = new ArrayList<>());
		
		int index = 0;
		for(int i = 0; i < list.size(); i++){
			if(types.length < list.get(i).getParameterCount()) continue;
			index = i;
			break;
		}
		list.add(index, new Task(name, executor, types));
	}
	

}
