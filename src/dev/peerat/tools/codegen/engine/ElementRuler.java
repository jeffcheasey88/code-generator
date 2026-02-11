package dev.peerat.tools.codegen.engine;

import java.util.LinkedList;
import java.util.List;
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
				rule.consumer.accept(element);
			}
		}
	}
	
	public void rule(Predicate<?> predicate, Consumer<?> consumer){
		this.rules.add(new Rule((Predicate<Object>)predicate, (Consumer<Object>)consumer));
	}
	
	public void rule(Predicate<?> predicate, String context, Consumer<?> consumer){
		final Predicate<Object> elementPredicate = (Predicate<Object>) predicate;
		final String taskName = context;
		
		this.rules.add(new Rule((element, executionContext) -> {
			if(elementPredicate.test(element)) {
				List<Task> tasks = executionContext.getHistory();
				for(Task task : tasks){
					if(task.getName().equals(taskName)) return true;
				}
			}
			return false;
		}, (Consumer<Object>)consumer));
	}
	
	class Rule{
		
		private Predicate<Object> predicate;
		private BiPredicate<Object, ExecutionContext> contextPredicate;
		private Consumer<Object> consumer;
		
		public Rule(Predicate<Object> predicate, Consumer<Object> consumer){
			this.predicate = predicate;
			this.consumer = consumer;
		}
		
		public Rule(BiPredicate<Object, ExecutionContext> contextPredicate, Consumer<Object> consumer){
			this.contextPredicate = contextPredicate;
			this.consumer = consumer;
		}
		
		public boolean canApply(Object element, ExecutionContext context){
			return contextPredicate != null ? contextPredicate.test(element, context) : predicate.test(element);
		}
		
	}
}
