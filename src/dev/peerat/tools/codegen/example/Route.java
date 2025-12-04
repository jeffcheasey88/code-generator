package dev.peerat.tools.codegen.example;

import static dev.peerat.parser.java.visitor.JavaVisitor.*;

import java.util.HashMap;
import java.util.Map;

import dev.peerat.parser.java.Annotation;
import dev.peerat.parser.java.Class;
import dev.peerat.parser.java.Function;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.JavaTokenType;
import dev.peerat.parser.java.builder.JavaBuilder;
import dev.peerat.parser.java.value.Value;
import dev.peerat.parser.tokens.Token;
import dev.peerat.tools.codegen.CodeParser;
import dev.peerat.tools.codegen.Template;
import dev.peerat.tools.codegen.template.InternalClass;

public class Route extends Template{

	private InternalClass controller;
	private String url;
	private String type;
	private String methodName;
	private boolean needLogin;
	private boolean websocket;
	
	public Route(Controller controller, String url, String type, String methodName){
		this(controller, url, type, methodName, false, false);
	}
	
	public Route(Controller controller, String url, String type, String methodName, boolean needLogin, boolean websocket){
		this.controller = controller.getClazz();
		this.url = url;
		this.type = type;
		this.methodName = methodName;
		this.needLogin = needLogin;
		this.websocket = websocket;
	}
	
	public boolean exist(JavaProject project){
		return get(project) != null;
	}
	
	public Function create(JavaProject project) throws Exception{
		Function function = get(project);
		if(function != null) return function;
		
		Class controller = this.controller.create(project);
		
		CodeParser code = loadTemplate("route");
		function = (Function) code.getElement();
		
		controller.getElements().add(function);
		
		Annotation annotation = function.visit(function().oneAnnotation(collect(annotation().name((s) -> s.equals("Route"))))).toElement();
		annotation.setParameters(mapAnnotation());
		
		function.setName(methodName);
		
		return function;
	}
	
	private Map<Token, Value> mapAnnotation(){
		Map<Token, Value> map = new HashMap<>();
		map.put(new Token(0,0,"path", JavaTokenType.STRING), JavaBuilder.ofValue("\""+this.url+"\""));
		map.put(new Token(0,0,"type", JavaTokenType.NAME), JavaBuilder.ofValue("RequestType."+type.toUpperCase()));
		if(needLogin) map.put(new Token(0,0,"needLogin", JavaTokenType.NAME), JavaBuilder.ofValue("true"));
		if(websocket) map.put(new Token(0,0,"websocket", JavaTokenType.NAME), JavaBuilder.ofValue("true"));
		return map;
	}
	
	public Function get(JavaProject project){
		return project.visit(controller.visitor((v) -> v.oneChild(collect(function().name((s) -> s.equals(methodName)))))).toElement();
	}
	
}
