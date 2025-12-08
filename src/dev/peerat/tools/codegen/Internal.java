package dev.peerat.tools.codegen;

import dev.peerat.parser.java.JavaElement;
import dev.peerat.parser.java.JavaFile;

public class Internal{
	
	private Tracker tracker;
	
	public Internal(){}
	
	public Internal(Tracker tracker){
		this.tracker = tracker;
	}
	
	public void track(JavaFile file){
		if(this.tracker != null) this.tracker.track(file);
	}
	
	public void trackElement(JavaElement element){
		JavaElement parent = element.getParent();
		while(!(parent instanceof JavaFile)) parent = parent.getParent();
		track((JavaFile)parent);
	}
}
