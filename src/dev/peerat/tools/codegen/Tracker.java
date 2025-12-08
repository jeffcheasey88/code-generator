package dev.peerat.tools.codegen;

import java.util.function.Consumer;

import dev.peerat.parser.java.JavaFile;

public class Tracker{
	
	private Consumer<JavaFile> tracker;
	
	public Tracker(Consumer<JavaFile> tracker){
		this.tracker = tracker;
	}
	
	public void track(JavaFile file){
		this.tracker.accept(file);
	}

}
