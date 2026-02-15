package dev.peerat.tools.codegen.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ElementRuler{
	
	private List<Rule> rules;
	
	public ElementRuler(){
		this.rules = new LinkedList<>();
	}
	
	public void apply(Object element, ExecutionContext context){
		for(Rule rule : this.rules){
			if(rule.canApply(element, context)){
				rule.apply(element, context);
			}
		}
	}
	
	public void rule(Predicate<?> predicate, Consumer<?> consumer){
		this.rules.add(new Rule((Predicate<Object>)predicate, (Consumer<Object>)consumer));
	}
	
	public void rule(Predicate<?> predicate, String context, Consumer<?> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(taskPredicate(context)),
				(Consumer<Object>)consumer));
	}
	
	public void rule(Predicate<?> predicate, BiConsumer<?, ExecutionContext> consumer){
		this.rules.add(new Rule(
				(Predicate<Object>)predicate,
				(BiConsumer<Object, ExecutionContext>)consumer));
	}
	
	public void rule(Predicate<?> predicate, String context, BiConsumer<?, ExecutionContext> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(taskPredicate(context)),
				(BiConsumer<Object, ExecutionContext>)consumer));
	}
	
	public void rule(Predicate<?> predicate, Predicate<Class<?>> dependencyPredicate, Consumer<?> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(dependencyPredicate(dependencyPredicate)),
				(Consumer<Object>)consumer));
	}
	
	public void rule(Predicate<?> predicate, String context, Predicate<Class<?>> dependencyPredicate, Consumer<?> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(taskPredicate(context))
				.and(dependencyPredicate(dependencyPredicate)),
				(Consumer<Object>)consumer));
	}
	
	public void rule(Predicate<?> predicate, Predicate<Class<?>> dependencyPredicate, BiConsumer<?, ExecutionContext> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(dependencyPredicate(dependencyPredicate)),
				(BiConsumer<Object, ExecutionContext>)consumer));
	}
	
	public void rule(Predicate<?> predicate, String context, Predicate<Class<?>> dependencyPredicate, BiConsumer<?, ExecutionContext> consumer){
		this.rules.add(new Rule(
				biPredicate((Predicate<Object>)predicate)
				.and(taskPredicate(context))
				.and(dependencyPredicate(dependencyPredicate)),
				(BiConsumer<Object, ExecutionContext>)consumer));
	}
	
	private BiPredicate<Object, ExecutionContext> biPredicate(Predicate<Object> predicate){
		return (element, executionContext) -> predicate.test(element);
	}
	
	private BiPredicate<Object, ExecutionContext> taskPredicate(String taskName){
		return (element, executionContext) -> {
			for(Task task : executionContext.getHistory()){
				if(task.getName().equals(taskName)) return true;
			}
			return false;
		};
	}
	
	private BiPredicate<Object, ExecutionContext> dependencyPredicate(Predicate<Class<?>> dependencyPredicate){
		return (element, executionContext) -> {
			for(Object obj : executionContext.getDependencies()){
				if(obj != null && dependencyPredicate.test(obj.getClass())) return true;
			}
			return false;
		};
	}
	
	class Rule{
		
		private Predicate<Object> predicate;
		private BiPredicate<Object, ExecutionContext> contextPredicate;
		private Consumer<Object> consumer;
		private BiConsumer<Object, ExecutionContext> contextConsumer;
		
		public Rule(Predicate<Object> predicate, Consumer<Object> consumer){
			this.predicate = predicate;
			this.consumer = consumer;
		}
		
		public Rule(BiPredicate<Object, ExecutionContext> contextPredicate, Consumer<Object> consumer){
			this.contextPredicate = contextPredicate;
			this.consumer = consumer;
		}
		
		public Rule(Predicate<Object> predicate, BiConsumer<Object, ExecutionContext> contextConsumer){
			this.predicate = predicate;
			this.contextConsumer = contextConsumer;
		}
		
		public Rule(BiPredicate<Object, ExecutionContext> contextPredicate, BiConsumer<Object, ExecutionContext> contextConsumer){
			this.contextPredicate = contextPredicate;
			this.contextConsumer = contextConsumer;
		}
		
		public boolean canApply(Object element, ExecutionContext context){
			return contextPredicate != null ? contextPredicate.test(element, context) : predicate.test(element);
		}
		
		public void apply(Object element, ExecutionContext context){
			if(contextConsumer != null) contextConsumer.accept(element, context);
			else consumer.accept(element);
		}
		
	}
}
