package dev.peerat.tools.codegen.engine;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.peerat.tools.codegen.engine.Executor.BiExecutor;
import dev.peerat.tools.codegen.engine.Executor.SingleExecutor;
import dev.peerat.tools.codegen.engine.Executor.TriExecutor;

public class ElementEngine{
	
	private TaskResolver taskResolver;
	private ElementRuler ruler;
	private Map<Thread, ExecutionContext> contexts;
	
	public ElementEngine(){
		this.taskResolver = new TaskResolver();
		this.ruler = new ElementRuler();
		this.contexts = new WeakHashMap<>();
	}
	
	private ExecutionContext getContext(){
		Thread thread = Thread.currentThread();
		ExecutionContext context = this.contexts.get(thread);
		if(context == null) this.contexts.put(thread, context = new ExecutionContext());
		return context;
	}
	
	public <T> TaskResult<T> task(String name, Object... parameters){
		ExecutionContext context = getContext();
		
		Object[] dependencies = context.getDependencies();
		Object[] env;
		if(dependencies == null) env = parameters;
		else if(parameters.length == 0) env = dependencies;
		else {
			env = new Object[dependencies.length+parameters.length];
			System.arraycopy(dependencies, 0, env, 0, dependencies.length);
			System.arraycopy(parameters, 0, env, dependencies.length, parameters.length);
		}
		
		Executable executable = this.taskResolver.resolveTask(name, env);
		context.task(executable.getTask());
		TaskResult<T> result = executable.getTask().execute(executable.getParameters());
		context.done(result);
		return result;
	}
	
	public <A> void duplicate(String name, Class<A> type, A arg0){
		getContext().duplicate(name, new Class<?>[]{type}, new Object[] {arg0});
	}
	
	public <A, B> void duplicate(String name, Class<A> type, Class<B> secondType, A arg0, B arg1){
		getContext().duplicate(name, new Class<?>[]{type, secondType}, new Object[] {arg0, arg1});
	}
	
	public <A, B, C> void duplicate(String name, Class<A> type, Class<B> secondType, Class<C> thirdType, A arg0, B arg1, C arg2){
		getContext().duplicate(name, new Class<?>[]{type, secondType, thirdType}, new Object[] {arg0, arg1, arg2});
	}
	
	public void context(Object... objects){
		getContext().addDependencies(objects);
	}
	
	public void context(Runnable runnable, Object... objects){
		ExecutionContext context = getContext();
		context.addDependencies(objects);
		runnable.run();
		context.removeDependencies(objects);
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
	
	public <A, B> void redirect(Class<A> originType, Class<B> targetType, Function<B, A> mapper){
		this.taskResolver.redirect(originType, targetType, mapper);
	}
	
	
	//TODO SUPER METHOD
	public <A> void overrideRule(String name, Class<A> type, SingleExecutor<A> executor){
		this.taskResolver.overrideRule(name, new Class<?>[]{type}, executor);
	}
	
	public <A, B> void overrideRule(String name, Class<A> type, Class<B> secondType, BiExecutor<A, B> executor){
		this.taskResolver.overrideRule(name, new Class<?>[]{type, secondType}, executor);
	}

	public <A, B, C> void overrideRule(String name, Class<A> type, Class<B> secondType, Class<C> thirdType, TriExecutor<A, B, C> executor){
		this.taskResolver.overrideRule(name, new Class<?>[]{type, secondType, thirdType}, executor);
	}
	
	public <T> void rule(Predicate<T> predicate, Consumer<T> consumer){
		this.ruler.rule(predicate, consumer);
	}
	
	public <T> void rule(Predicate<T> predicate, String context, Consumer<T> consumer){
		this.ruler.rule(predicate, context, consumer);
	}
	
	public void element(Object element){
		this.ruler.apply(element, getContext());
	}
	
}
