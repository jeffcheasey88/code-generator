package dev.peerat.tools.codegen.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TaskResolver{
	
	private Map<String, List<Task>> tasks;
	private List<Redirection> redirections;
	
	public TaskResolver(){
		this.tasks = new HashMap<>();
		this.redirections = new ArrayList<>();
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
		rule(name, types, executor, null);
	}
	
	public void rule(String name, Class<?>[] types, Executor executor, Function<Object, Object>[] mappers){
		List<Task> list = this.tasks.get(name);
		if(list == null) this.tasks.put(name, list = new ArrayList<>());
		
		int index = 0;
		for(int i = 0; i < list.size(); i++){
			if(types.length < list.get(i).getParameterCount()) continue;
			index = i;
			break;
		}
		Task task = new Task(name, executor, types, mappers);
		list.add(index, task);
		for(Redirection redirect : this.redirections) applyRedirect(redirect, task);
	}
	
	public void overrideRule(String name, Class<?>[] types, Executor executor){
		List<Task> list = this.tasks.get(name);
		Task task = new Task(name, executor, types, null);
		if(list == null) {
			 this.tasks.put(name, list = new ArrayList<>());
			 list.add(task);
			 return;
		}
		int index = -1;
		for(int i = 0; i < list.size(); i++){
			Class<?>[] parameters = list.get(i).getParameters();
			if(types.length != parameters.length) continue;
			boolean isThis = true;
			for(int j = 0; j < types.length; j++){
				if(!types[j].equals(parameters[j])){
					isThis = false;
					break;
				}
			}
			if(!isThis) continue;
			index = i;
			break;
		}
		if(index < 0) return;
		list.set(index, task);
	}
	
	public void redirect(Class<?> origin, Class<?> target, Function<?, ?> mapper){
		Redirection redirect = new Redirection(origin, target, (Function<Object, Object>)mapper);
		this.redirections.add(redirect);
		for(List<Task> tasks : this.tasks.values()){
			for(Task task : tasks) applyRedirect(redirect, task);
		}
	}
	
	private void applyRedirect(Redirection redirection, Task task){
		Class<?>[] parameters = task.getParameters();
		Function<Object, Object>[] mappers = task.getMappers();
		for(int i = 0; i < parameters.length; i++){
			if(parameters[i].equals(redirection.getOriginType())){
				if(mappers == null){
					mappers = new Function[i+1];
					mappers[i] = redirection.getMapper();
				}else if(mappers.length <= i){
					Function<Object, Object>[] copy = new Function[i+1];
					System.arraycopy(mappers, 0, copy, 0, mappers.length);
					mappers = copy;
					mappers[i] = redirection.getMapper();
				}else if(mappers[i] == null){
					mappers[i] = redirection.getMapper();
				}else{
					Function<Object, Object> map = mappers[i];
					Function<Object, Object> redirect = redirection.getMapper();
					mappers[i] = (value) -> redirect.apply(map.apply(value));
				}
				Class<?>[] adaptedParameters = new Class[parameters.length];
				System.arraycopy(parameters, 0, adaptedParameters, 0, parameters.length);
				adaptedParameters[i] = redirection.getTargetType();
				rule(task.getName(), adaptedParameters, task.getExecutor(), mappers);
				break;
			}
		}
	}
	

}
