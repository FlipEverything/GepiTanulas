/**
 * Represents one line
 * Edit or Insert
 */
public class Edit {
	private char type;
	private String text;
	
	public Edit(char type, String text) {
		super();
		this.type = type;
		this.text = text;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "Edit [type=" + type + ", text=" + text + "]";
	}
	
	
	
	
}
