package dev.peerat.tools.codegen.engine;

import dev.peerat.tools.codegen.engine.Executor.BiExecutor;
import dev.peerat.tools.codegen.engine.Executor.SingleExecutor;
import dev.peerat.tools.codegen.engine.Executor.TriExecutor;

public class ElementEngine{
	
	private TaskResolver taskResolver;
	
	public ElementEngine(){
		this.taskResolver = new TaskResolver();
	}
	
	public <T> TaskResult<T> task(String name, Object... parameters){
		return this.taskResolver.resolveTask(name, parameters).execute(parameters);
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
