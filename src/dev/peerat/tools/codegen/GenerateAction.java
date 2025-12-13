package dev.peerat.tools.codegen;

import java.util.List;

import dev.peerat.parser.java.Import;
import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.Type;
import dev.peerat.parser.java.builder.JavaBuilder;

public class GenerateAction{
	
	public static Type useType(JavaElement element, String source, String type){
		String normalizedType = type;
		if(type.contains("<")) normalizedType = type.substring(0, type.indexOf('<'));
		
		String typeInUse = type;
		if(!imp(element, source+"."+normalizedType)) typeInUse=source+typeInUse;
		return JavaBuilder.ofType(typeInUse);
	}
	
	public static String useAnnotation(JavaElement element, String source, String type){
		String typeInUse = type;
		if(!imp(element, source+"."+type)) typeInUse=source+typeInUse;
		return typeInUse;
	}
	
	private static boolean imp(JavaElement element, String source){
		JavaFile file = null;
		if(element instanceof JavaFile) file = (JavaFile)element;
		else{
			JavaElement parent = element.getParent();
			while(!(parent instanceof JavaFile)) parent = parent.getParent();
			file = (JavaFile) parent;
		}
		
		String typeName = source.substring(source.lastIndexOf('.'));
		
		List<Import> imports = file.getImports();
		for(Import imp : imports){
			if(imp.isStatic()) continue;
			String value = imp.getValue().getValue();
			if(value.equals(source)) return true;
			String valueTypeName = value.substring(value.lastIndexOf('.'));
			if(typeName.equals(valueTypeName)) return false;
		}
		
		imports.add(JavaBuilder.ofImport(false, source).build());
		return true;
	}

}
