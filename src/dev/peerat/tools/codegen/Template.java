package dev.peerat.tools.codegen;

import java.io.BufferedReader;
import java.io.FileReader;

public abstract class Template{
	
	public CodeParser loadCode(String code) throws Exception{
		return new CodeParser(code);
	}
	
	public CodeParser loadTemplate(String template) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader("template/"+template+".txt"));
		String code = "";
		String line;
		while((line = reader.readLine()) != null) code+=line+"\n";
		reader.close();
		
		return new CodeParser(code);
	}
	
}
