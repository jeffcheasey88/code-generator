package dev.peerat.tools.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import dev.peerat.parser.java.JavaFile;
import dev.peerat.parser.java.JavaProject;
import dev.peerat.parser.java.printer.JavaPrinter;
import dev.peerat.parser.java.printer.JavaPrinter.JavaPrintProvider;
import dev.peerat.parser.java.printer.JavaPrinter.Writer;
import dev.peerat.tools.codegen.example.Controller;
import dev.peerat.tools.codegen.example.Route;

public class Main{
	
	public static void main(String[] args) throws Exception{
		JavaProject project = new JavaProject();
		
		Route route = new Route(new Controller("be.jeffcheasey88.test", "CarController"), "/cars", "get", "getCars");
		route.create(project);
		
		System.out.println("writing...");
		
		JavaPrintProvider printer = JavaPrinter.getProvider();
		for(JavaFile jfile : project.getFiles()){
			File file = new File("output", jfile.getPackage().getValue().getValue().replace(".", "/")+"/"+jfile.getMainClass().getName().getName().getValue()+".java");
			System.out.println("write in "+file.getAbsolutePath());
			File parent = file.getParentFile();
			if(!parent.exists()) parent.mkdirs();
			if(!file.exists()) file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			printer.getPrinter(jfile).print(jfile, new Writer(writer), "", printer);
			writer.flush();
			writer.close();
		}
		
		System.out.println("done.");
	}

}
