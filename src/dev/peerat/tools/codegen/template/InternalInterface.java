package dev.peerat.tools.codegen.template;

import static dev.peerat.parser.java.visitor.JavaVisitor.collect;
import static dev.peerat.parser.java.visitor.JavaVisitor.file;
import static dev.peerat.parser.java.visitor.JavaVisitor.interfaceClass;

import java.util.function.Consumer;

import dev.peerat.parser.java.Interface;
import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.builder.JavaBuilder;
import dev.peerat.parser.java.builder.JavaInterfaceBuilder;
import dev.peerat.parser.java.visitor.JavaInterfaceVisitor;
import dev.peerat.parser.visitor.Visitor;

public class InternalInterface{

	private String pack;
	private String name;
	
	public InternalInterface(String pack, String name){
		this.pack = pack;
		this.name = name;
	}

	public boolean exist(JavaProject project){
		return get(project) != null;
	}
	
	public Interface createIfAbsent(JavaProject project) throws Exception{
		return createIfAbsent(project, null);
	}
	
	public Interface createIfAbsent(JavaProject project, Consumer<JavaInterfaceBuilder> builder) throws Exception{
		Interface clazz = get(project);
		return clazz != null ? clazz : create(project, builder);
	}
	
	public Interface create(JavaProject project) throws Exception{
		return create(project, null);
	}
	
	public Interface create(JavaProject project, Consumer<JavaInterfaceBuilder> builder) throws Exception{
		JavaFile file = JavaBuilder.ofFile(pack).build();
		JavaInterfaceBuilder classBuilder = JavaBuilder.ofInterface(name);
		if(builder != null) builder.accept(classBuilder);
		Interface clazz;
		file.addClass(clazz = classBuilder.build());
		project.addFile(file, true);
		return clazz;
	}

	public Visitor<JavaElement> visitor(Consumer<JavaInterfaceVisitor> visitorConsumer){
		JavaInterfaceVisitor visitor = interfaceClass().name((s) -> s.equals(name));
		visitorConsumer.accept(visitor);
		return file().packaged((s) -> s.equals(pack)).hasInterface(visitor);
	}
	
	public Interface get(JavaProject project){
		return project.visit(file().packaged((s) -> s.equals(pack)).hasInterface(collect(interfaceClass().name((s) -> s.equals(name))))).toElement();
	}
	
	public boolean remove(JavaProject project){
		Interface clazz = get(project);
		if(clazz == null) return false;
		return project.getFiles().remove(clazz.getParent());
	}
}
