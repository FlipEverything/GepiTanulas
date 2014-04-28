import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents one raw instance (a change record)
 * Stores the header and the body (inserts, deletes)
 */
public class ChangeLog {
	private int docId;
	private String type;
	private String userId;
	private String date;
	private String comment;
	private String title;
	
	private int insertWordCount;
	private int deleteWordCount;
	
	private ArrayList<Edit> edits;

	public ChangeLog(int docId, String type, String userId, String date,
			String comment, String title, ArrayList<Edit> edits) {
		super();
		this.docId = docId;
		this.type = type;
		this.userId = userId;
		this.date = date;
		this.comment = comment;
		this.title = title;
		this.edits = edits;
		calculateEditCount();
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ArrayList<Edit> getEdits() {
		return edits;
	}

	public void setEdits(ArrayList<Edit> edits) {
		this.edits = edits;
	}
	
	
	public void calculateEditCount(){
		Iterator<Edit> it = edits.iterator();
		while (it.hasNext()){
			Edit e = it.next();
			String[] textSplit = e.getText().split(" ");
			if (Character.toString(e.getType()).equals("I")){
				incInsertWordCount(textSplit.length);
			} else if (Character.toString(e.getType()).equals("D")){
				incDeleteWordCount(textSplit.length);
			}
		}
	}
	
	public void incInsertWordCount(int count){
		this.insertWordCount += count;
	}
	
	public void incDeleteWordCount(int count){
		this.deleteWordCount += count;
	}

	public int getInsertCount() {
		return insertWordCount;
	}

	public void setInsertCount(int insertCount) {
		this.insertWordCount = insertCount;
	}

	public int getDeleteCount() {
		return deleteWordCount;
	}

	public void setDeleteCount(int deleteCount) {
		this.deleteWordCount = deleteCount;
	}
	

	public int getInsertWordCount() {
		return insertWordCount;
	}

	public void setInsertWordCount(int insertWordCount) {
		this.insertWordCount = insertWordCount;
	}

	public int getDeleteWordCount() {
		return deleteWordCount;
	}

	public void setDeleteWordCount(int deleteWordCount) {
		this.deleteWordCount = deleteWordCount;
	}

	@Override
	public String toString() {
		return "ChangeLog [docId=" + docId + ", type=" + type + ", userId="
				+ userId + ", date=" + date + ", comment=" + comment
				+ ", title=" + title + ", edits=" + edits + "]";
	}
	
	
}
