package dev.peerat.tools.codegen.template;

import static dev.peerat.parser.java.visitor.JavaVisitor.clazz;
import static dev.peerat.parser.java.visitor.JavaVisitor.collect;
import static dev.peerat.parser.java.visitor.JavaVisitor.file;

import java.util.function.Consumer;

import dev.peerat.parser.java.Class;
import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.builder.JavaBuilder;
import dev.peerat.parser.java.builder.JavaClassBuilder;
import dev.peerat.parser.java.visitor.JavaClassVisitor;
import dev.peerat.parser.visitor.Visitor;
import dev.peerat.tools.codegen.Internal;
import dev.peerat.tools.codegen.Tracker;

public class InternalClass extends Internal{

	private String pack;
	private String name;
	
	public InternalClass(String pack, String name){
		this(null, pack, name);
	}
	
	public InternalClass(Tracker tracker, String pack, String name){
		super(tracker);
		this.pack = pack;
		this.name = name;
	}

	public boolean exist(JavaProject project){
		return get(project) != null;
	}
	
	public Class createIfAbsent(JavaProject project) throws Exception{
		return createIfAbsent(project, null);
	}
	
	public Class createIfAbsent(JavaProject project, Consumer<JavaClassBuilder> builder) throws Exception{
		Class clazz = get(project);
		return clazz != null ? clazz : create(project, builder);
	}
	
	public Class create(JavaProject project) throws Exception{
		return create(project, null);
	}
	
	public Class create(JavaProject project, Consumer<JavaClassBuilder> builder) throws Exception{
		JavaFile file = JavaBuilder.ofFile(pack).build();
		JavaClassBuilder classBuilder = JavaBuilder.ofClass(name);
		if(builder != null) builder.accept(classBuilder);
		Class clazz;
		file.addClass(clazz = classBuilder.build());
		project.addFile(file, true);
		track(file);
		return clazz;
	}

	public Visitor<JavaElement> visitor(Consumer<JavaClassVisitor> visitorConsumer){
		JavaClassVisitor visitor = clazz().name((s) -> s.equals(name));
		visitorConsumer.accept(visitor);
		return file().packaged((s) -> s.equals(pack)).hasClass(visitor);
	}
	
	public Class get(JavaProject project){
		return project.visit(file().packaged((s) -> s.equals(pack)).hasClass(collect(clazz().name((s) -> s.equals(name))))).toElement();
	}
	
	public boolean remove(JavaProject project){
		Class clazz = get(project);
		if(clazz == null) return false;
		return project.getFiles().remove(clazz.getParent());
	}
}
