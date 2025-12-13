package dev.peerat.tools.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import dev.peerat.parser.java.JavaElement;

public abstract class Observer<C extends Collector>{
	
	private static final JavaPlaceHolderParser placeHolderParser = new JavaPlaceHolderParser();
	
	protected C collector;
	private List<Action> actions;

	public Observer(C collector){
		this.collector = collector;
		this.actions = new ArrayList<>();
	}
	
	public C getCollector(){
		return this.collector;
	}
	
	public abstract void observe(JavaElement element);
	
	public <T> T element(JavaElement element){
		for(Action action : actions){
			if(action.canApply(element)) return (T) action.apply(element, collector);
		}
		return null;
	}
	
	public <J extends JavaElement> void when(Class<J> clazz, Predicate<J> filter, BiFunction<J, C, Object> function){
		Predicate<JavaElement> elementFilter = (Predicate<JavaElement>)filter;
		this.actions.add(new Action(element -> element.getClass().equals(clazz) && elementFilter.test(element), (BiFunction<JavaElement, Collector, Object>) function));
	}
	
	public <J extends JavaElement> void whenConsume(Class<J> clazz, Predicate<J> filter, BiConsumer<J, C> consumer){
		when(clazz, filter, (J j, C c) -> { consumer.accept(j, c); return null;});
	}
	
	public <J extends JavaElement> void when(String placeHolderCode, BiFunction<J, C, Object> function) throws Exception{
		this.actions.add(new Action(placeHolderParser.parse(placeHolderCode), (BiFunction<JavaElement, Collector, Object>) function));
	}
	
	public <J extends JavaElement> void whenConsume(String placeHolderCode, BiConsumer<J, C> consumer) throws Exception{
		when(placeHolderCode, (J j, C c) -> { consumer.accept(j, c); return null;});
	}
	
	public static class Action{
		
		private Predicate<JavaElement> predicate;
		private BiFunction<JavaElement, Collector, Object> function;
		
		public Action(Predicate<JavaElement> predicate, BiFunction<JavaElement, Collector, Object> function){
			this.predicate = predicate;
			this.function = function;
		}
		
		public boolean canApply(JavaElement element){
			return this.predicate.test(element);
		}
		
		public Object apply(JavaElement element, Collector collector){
			return function.apply(element, collector);
		}
		
	}
}