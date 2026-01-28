package dev.peerat.tools.codegen.engine;

import java.util.function.Function;

public class Redirection{
	
	private Class<?> originType;
	private Class<?> targetType;
	private Function<Object, Object> mapper;
	
	public Redirection(Class<?> originType, Class<?> targetType, Function<Object, Object> mapper){
		this.originType = originType;
		this.targetType = targetType;
		this.mapper = mapper;
	}
	
	public Class<?> getOriginType(){
		return this.originType;
	}
	
	public Class<?> getTargetType(){
		return this.targetType;
	}
	
	public Function<Object, Object> getMapper(){
		return this.mapper;
	}
}
