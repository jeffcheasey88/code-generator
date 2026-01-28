package dev.peerat.tools.codegen.unnamedtasks;

import static dev.peerat.parser.java.builder.JavaBuilder.*;
import static dev.peerat.parser.java.visitor.JavaVisitor.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import dev.peerat.parser.java.Class;
import dev.peerat.parser.java.Function;
import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.JavaParser;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.JavaTokenType;
import dev.peerat.parser.java.Type;
import dev.peerat.parser.java.Variable;
import dev.peerat.parser.java.builder.JavaClassBuilder;
import dev.peerat.parser.java.builder.JavaFunctionBuilder;
import dev.peerat.parser.java.operation.ForeachOperation;
import dev.peerat.parser.java.operation.IfOperation;
import dev.peerat.parser.java.operation.ReturnOperation;
import dev.peerat.parser.java.printer.JavaPrinter;
import dev.peerat.parser.java.printer.JavaPrinter.JavaPrintProvider;
import dev.peerat.parser.java.printer.JavaPrinter.Writer;
import dev.peerat.parser.java.value.Value;
import dev.peerat.parser.tokens.Token;
import dev.peerat.tools.codegen.engine.ElementEngine;

public class TmpEdit{
	
	public static void main(String[] args) throws Exception{
		File fileProject = new File("../peer-at-code-parser-java/src");
		JavaProject project = new JavaProject();
		loadProject(project, fileProject);
		
		ElementEngine engine = new ElementEngine();
		
		engine.rule("className", String.class, s -> "Java"+s+"Builder");
		engine.rule("javabuilder", String.class, s -> "JavaBuilder<"+s+">");
		
		engine.redirect(String.class, Type.class, t -> t.getName().getValue());
		
		engine.rule("setter", dev.peerat.parser.java.Class.class, JavaFunctionBuilder.class, (clazz, builder) -> {
			builder.setType(engine.<String>task("className", clazz.getName()).get()).setPublic();
			
			
			return builder;
		});
		
		//TODO REDIRECT RULE WHEN RUNNING
		
		
		List<Class> operations = project.visit(allFile().packaged(seq("dev.peerat.parser.java.operation")).someClass(collect(allClass()))).toList();

		JavaProject builded = new JavaProject();
		
		JavaElement returnThis = new ReturnOperation(ofStaticValue("this").build());
		
		for(Class clazz : operations){
			JavaClassBuilder clazzBuilder = ofClass(engine.<String>task("className", clazz.getName()).get())
				.extend(engine.<String>task("javabuilder", clazz.getName()).get())
				.setPublic();
			List<Variable> parameters = clazz.visit(classBase().someChild(collect(variable()))).toList();
			for(Variable param : parameters){
				if(param.getType().toString().equals("Token")) clazzBuilder.element(ofVariable("String", param.getName().getValue()).setPrivate());
				else clazzBuilder.element(param.copyOf());
			}
			
			JavaFunctionBuilder normalizer =
					ofFunction(engine.<String>task("className", clazz.getName()).get(), "normalize")
					.setPublic()
					.parameter(ofVariable(clazz.getName(), "value"));
			for(Variable variable : parameters){
				String name = variable.getName().getValue();
				Function getter = clazz.visit(collect(allFunction().hasNoParameter().name(seq("get"+Character.toUpperCase(name.charAt(0))+name.substring(1))))).toElement();
				if(getter != null){
					IfOperation op = new IfOperation(ofBiValue(ofMethodCallValue(ofStaticValue("value"),getter.getName().getValue()), "!=", ofStaticValue("null")).build());
					if(variable.getType().toString().startsWith("List")){
						ForeachOperation forOp = new ForeachOperation(false, (Type) variable.getType().getGeneric().getTypes().get(0), new Token(0,0, "element", JavaTokenType.NAME), ofStaticValue("value."+getter.getName().getValue()+"()").build());
						forOp.getElements().add(ofMethodCallValue(ofStaticValue("this."+variable.getName().getValue()),"add", ofStaticValue("JavaBuilder.normalizeElement(element)").build()).build());
						op.getElements().add(forOp);
					}else{
						op.getElements().add(ofBiValue(ofStaticValue("this."+variable.getName().getValue()), "=", ofStaticValue("JavaBuilder.normalizeElement(value."+getter.getName().getValue()+"())")).build());
					}
					normalizer.element(op);
				}
			}
			
			Value modelInstance = ofInstanceValue(clazz.getName())
					.addParameters(
							Arrays.asList(
									parameters.stream().map(v ->{
										if(v.getType().toString().equals("Token")){
											return ofStaticValue("new Token(0,0, "+v.getName().getValue()+", JavaTokenType.NAME)").build();
										}
										return ofStaticValue(v.getName().getValue()).build();
									}).toArray(i->new Value[i])
							)
					).build();
			
			JavaFunctionBuilder build = ofFunction(clazz.getName(),"build").setPublic().annotate(ofAnnotation("Override"));
			if(clazz.getExtension() != null && clazz.getExtension().toString().equals("OperationBag")){
				clazzBuilder.element(ofVariable("List<JavaElement>", "elements").setPrivate());
				normalizer.element(ofStaticValue("if(value.getElements() != null) for(JavaElement element : value.getElements()) this.elements.add(JavaBuilder.normalizeElement(element))").build());
				normalizer.element(returnThis.copyOf());
				clazzBuilder.element(normalizer);
				
				build.element(ofVariable(clazz.getName(), "result", modelInstance));
				build.element(ofMethodCallValue(ofStaticValue("result"), "setElements", ofStaticValue("elements").build()));
				build.element(new ReturnOperation(ofStaticValue("result").build()));
			}else{
				normalizer.element(returnThis.copyOf());
				clazzBuilder.element(normalizer);
				build.element(new ReturnOperation(modelInstance));
			}
			
			for(Variable param : parameters){
				String name = param.getName().getValue();
				String setter = "set"+Character.toUpperCase(name.charAt(0))+name.substring(1);
				Variable copy = (Variable) param.copyOf();
				copy.setModifier(0);
				String type = param.getType().toString();
				if(type.equals("Token")){
					clazzBuilder.element(
							ofFunction("Java"+clazz.getName()+"Builder",setter)
							.setPublic()
							.parameter(ofVariable("String", name))
							.element(ofBiValue(ofStaticValue("this."+name), "=", ofStaticValue(name)))
							.element(returnThis.copyOf())
							);
					clazzBuilder.element(
							ofFunction("Java"+clazz.getName()+"Builder", setter)
							.setPublic()
							.parameter(ofVariable(copy).setType("Token"))
							.element(new ReturnOperation(ofMethodCallValue(setter, ofMethodCallValue(ofStaticValue(name), "getValue").build()).build()))
							);
					continue;
				}else if((!type.startsWith("List")) && (!type.equals("Type"))){
					clazzBuilder.element(
							ofFunction("Java"+clazz.getName()+"Builder",setter)
							.setPublic()
							.parameter(copy)
							.element(ofBiValue(ofStaticValue("this."+name), "=", ofStaticValue(name)))
							.element(returnThis.copyOf())
							);
				}
				if(type.startsWith("List")){
					clazzBuilder.element(
							ofFunction("Java"+clazz.getName()+"Builder", name)
							.setPublic()
							.parameter(ofVariable(copy).setType(ofType("Collection"+copy.getType().getGeneric())))
							.element(ofMethodCallValue(ofVariableAccessValue(ofStaticValue("this"), name), "addAll", ofStaticValue(name).build()))
							.element(returnThis.copyOf())
							);
				}else{
					if(type.contains("Value") || type.contains("Operation")){
						clazzBuilder.element(
								ofFunction("Java"+clazz.getName()+"Builder", setter)
								.setPublic()
								.parameter(ofVariable(copy).setType("JavaBuilder<? extends "+type+">"))
								.element(new ReturnOperation(ofMethodCallValue(setter, ofMethodCallValue(ofStaticValue(name), "build").build()).build()))
								);
					}else if(type.equals("Type")){
						clazzBuilder.element(
								ofFunction("Java"+clazz.getName()+"Builder",setter)
								.setPublic()
								.parameter(ofVariable("String", name))
								.element(ofBiValue(ofStaticValue("this."+name), "=", ofStaticValue(name)))
								.element(returnThis.copyOf())
								);
						clazzBuilder.element(
								ofFunction("Java"+clazz.getName()+"Builder", setter)
								.setPublic()
								.parameter(ofVariable(copy).setType("Type"))
								.element(new ReturnOperation(ofMethodCallValue(setter, ofMethodCallValue("ofType").addParameter(ofMethodCallValue(name, ofStaticValue("toString").build())).build()).build()))
								);
					}
				}
			}
			
			if(clazz.getExtension() != null && clazz.getExtension().toString().equals("OperationBag")){
				clazzBuilder.element(
						ofFunction("Java"+clazz.getName()+"Builder", "element")
						.setPublic()
						.parameter(ofVariable("JavaElement","value"))
						.element(ofMethodCallValue(ofVariableAccessValue(ofStaticValue("this"), "elements"), "add", ofStaticValue("element").build()))
						.element(returnThis.copyOf())
						);
				clazzBuilder.element(
						ofFunction("Java"+clazz.getName()+"Builder", "element")
						.setPublic()
						.parameter(ofVariable("JavaBuilder<? extends JavaElement>","value"))
						.element(new ReturnOperation(ofMethodCallValue("element", ofMethodCallValue(ofStaticValue("value"), "build").build()).build()))
						);
				clazzBuilder.element(
						ofFunction("Java"+clazz.getName()+"Builder", "elements")
						.setPublic()
						.parameter(ofVariable("Collection<JavaElement>","elements"))
						.element(ofMethodCallValue(ofVariableAccessValue(ofStaticValue("this"), "elements"), "addAll", ofStaticValue("elements").build()))
						.element(returnThis.copyOf())
						);
			}
			
			clazzBuilder.element(build);
			
			builded.addFile(ofFile("dev").clazz(clazzBuilder).build(), true);
		}
		
		JavaPrintProvider provider = JavaPrinter.getProvider();
		BufferedWriter sysout = new BufferedWriter(new java.io.Writer(){
			
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException{
				for(int i = 0; i < len; i++){
					System.out.print(cbuf[off+i]);
				}
			}
			
			@Override
			public void flush() throws IOException{
				
			}
			
			@Override
			public void close() throws IOException{
				
			}
		});
		for(JavaFile jfile : builded.getFiles()){
			provider.print(jfile, new Writer(sysout), "");
		}
		sysout.flush();
		sysout.close();
	}
	
	private static final JavaParser PARSER = new JavaParser();

	private static void loadProject(JavaProject project, File file) throws Exception{
		if(file.isDirectory()){
			for(File child : file.listFiles()) loadProject(project, child);
			return;
		}
		if(!file.getName().endsWith(".java")) return;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		JavaFile jfile = new JavaFile();
		PARSER.parse(reader, jfile);
		reader.close();
		project.addFile(jfile, true);
	}
}
