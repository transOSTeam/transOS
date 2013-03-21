package backend;

public class DirEntry{
	private String name;
	private char type;
	
	public DirEntry(String tName, char tType){
		this.name = tName;
		this.type = tType;
	}
	public String getName() {
		return this.name;
	}
	public char getType() {
		return this.type;
	}
}