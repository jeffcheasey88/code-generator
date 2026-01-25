package dev.peerat.tools.codegen.engine;

import java.util.Map;
import java.util.WeakHashMap;

import dev.peerat.tools.codegen.engine.Executor.BiExecutor;
import dev.peerat.tools.codegen.engine.Executor.SingleExecutor;
import dev.peerat.tools.codegen.engine.Executor.TriExecutor;

public class ElementEngine{
	
	private TaskResolver taskResolver;
	private Map<Thread, ExecutionContext> contexts;
	
	public ElementEngine(){
		this.taskResolver = new TaskResolver();
		this.contexts = new WeakHashMap<>();
	}
	
	public ExecutionContext getContext(){
		Thread thread = Thread.currentThread();
		ExecutionContext context = this.contexts.get(thread);
		if(context == null) this.contexts.put(thread, context = new ExecutionContext());
		return context;
	}
	
	public <T> TaskResult<T> task(String name, Object... parameters){
		Task task = this.taskResolver.resolveTask(name, parameters);
		ExecutionContext context = getContext();
		context.task(task);
		TaskResult<T> result = task.execute(parameters);
		context.done(result);
		return result;
	}
	
	public <A, B> void duplicate(String name, Class<A> type, Class<B> secondType, A arg0, B arg1){
		getContext().duplicate(name, new Class<?>[]{type, secondType}, new Object[] {arg0, arg1});
	}
	
	public <A> void rule(String name, Class<A> type, SingleExecutor<A> executor){
		this.taskResolver.rule(name, new Class<?>[]{type}, executor);
	}
	
	public <A, B> void rule(String name, Class<A> type, Class<B> secondType, BiExecutor<A, B> executor){
		this.taskResolver.rule(name, new Class<?>[]{type, secondType}, executor);
	}

	public <A, B, C> void rule(String name, Class<A> type, Class<B> secondType, Class<C> thirdType, TriExecutor<A, B, C> executor){
		this.taskResolver.rule(name, new Class<?>[]{type, secondType, thirdType}, executor);
	}
	
}
