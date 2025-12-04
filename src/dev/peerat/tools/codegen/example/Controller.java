package dev.peerat.tools.codegen.example;

import dev.peerat.tools.codegen.template.InternalClass;

public class Controller{
	
	private String pack;
	private String name;
	
	public Controller(String pack, String name){
		this.pack = pack;
		this.name = name;
	}
	
	public InternalClass getClazz(){
		return new InternalClass(pack, name);
	}

}
