package dev.peerat.tools.codegen.engine;

public interface Executor{
	
	default Object exec(Object[] parameters){ return null; }
	
	public static interface SingleExecutor<A> extends Executor{
		
		Object execute(A arg0);
		
		@Override
		default Object exec(Object[] parameters){
			return execute((A) parameters[0]);
		}
		
	}
	
	public static interface BiExecutor<A, B> extends Executor{
		
		Object execute(A arg0, B arg1);
		
		@Override
		default Object exec(Object[] parameters){
			return execute((A) parameters[0], (B) parameters[1]);
		}
		
	}

	public static interface TriExecutor<A, B, C> extends Executor{
	
		Object execute(A arg0, B arg1, C arg2);
		
		@Override
		default Object exec(Object[] parameters){
			return execute((A) parameters[0], (B) parameters[1], (C) parameters[2]);
		}
	
	}


}
