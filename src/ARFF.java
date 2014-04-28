import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Stores the instances from one input file (test or train)
 *
 */
public class ARFF {
	private String fileName = null;
	private ArrayList<ChangeLog> changes = null;
	private String[] classes = null;
	private FastVector attributes = null;
	private FastVector classVals = null;
	private FastVector booleanVals = null;	
	private Instances data = null;
	private String dataSetType = null;
	private String delimiter = "\t";

	public static final String WORD_DELIMITER = " ";
	public static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	/**
	 * Create an object, that represents the current data set
	 * @param fileName The input file name (read the input from)
	 * @param classes The available classes
	 * @param delimiter Override the default delimiter (\t) 
	 * @param dataSetType Train or test data set
	 */
	public ARFF(String fileName, String[] classes, String delimiter, String dataSetType){
		super();
		this.fileName = fileName;
		this.changes = new ArrayList<>();
		this.attributes = new FastVector();
		this.classVals = new FastVector();
		this.booleanVals = new FastVector();
		this.classes = classes;
		if (delimiter != null){
			this.delimiter = delimiter;
		}
		this.dataSetType = dataSetType;
	}
	
	/**
	 * Creating the RAW arff file from the input DataSet
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void create() throws NumberFormatException, IOException{
		read();
		setUpAttributes();
		setUpInstances();
		setClassIndex();
	}
	
	/**
	 * Read and parse the input DataSet
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void read() throws NumberFormatException, IOException{
		System.out.println(dataSetType+": Reading and parsing file: "+fileName+"...");
		BufferedReader br = null;
		String line = "";
		
		br = new BufferedReader(new FileReader(fileName));
		
		// read the file line to line
		while ((line = br.readLine()) != null){
			// first line after the empty line is the header
			String[] header = line.split(delimiter);
			ArrayList<Edit> edits = new ArrayList<>();
			// read while the line is not empty (edits)
			while (!(line = br.readLine()).trim().isEmpty()){
					String[] change = line.split(delimiter);
					edits.add(new Edit(change[0].charAt(0), change[1]));	
			}
			// add the data to the object
			int i=0;
			changes.add(new ChangeLog(Integer.parseInt(header[i++]), header[i++], header[i++], header[i++], header[i++], header[i++], edits));
		}
}
	
	/**
	 * Generate the attributes and add them to the header
	 */
	public void setUpAttributes(){
		System.out.println(dataSetType+": Setting up attributes...");
		
		// set up the boolean vector
		booleanVals.addElement("true");
		booleanVals.addElement("false");
		
		// setting up the class vector
		for (int i=0; i<classes.length; i++){
			classVals.addElement(classes[i]);
		}
		
		// setting up the attributes
		attributes.addElement(new Attribute("isLoggedIn", booleanVals));
		attributes.addElement(new Attribute("comment", booleanVals));
		attributes.addElement(new Attribute("reverted", booleanVals));
		attributes.addElement(new Attribute("insertWordCount"));
		attributes.addElement(new Attribute("deleteWordCount"));
		attributes.addElement(new Attribute("differenceCount"));
		attributes.addElement(new Attribute("commentLength"));
		attributes.addElement(new Attribute("edits", (FastVector)null));
		attributes.addElement(new Attribute("class", classVals));
	}
	
	/**
	 * Iterate through the parsed data and create instances from them
	 * Fill up the attributes
	 */
	public void setUpInstances(){
		System.out.println(dataSetType+": Setting up instances...");
		data = new Instances("Wiki", attributes, 0);
		
		// Iterate the change log object (represents the parsed input data)
		Iterator<ChangeLog> it = changes.iterator();
		while (it.hasNext()){
			ChangeLog ch = it.next();
						
			double[] vals = new double[data.numAttributes()];
			String editsString = "";
			int i;
			
			// concatenate the edit into a single long string
			// copy the edit type (insert, delete) before every word
			Iterator<Edit> editIterator = ch.getEdits().iterator();
			while (editIterator.hasNext()){
				Edit e = editIterator.next();
				String[] words = e.getText().split(WORD_DELIMITER);
				for (int j=0; j<words.length; j++){
					editsString += e.getType()+words[j]+WORD_DELIMITER;	
				}
			}
			
			// set up the attribute values for the current instance
			i = 0;
			vals[i++] = booleanVals.indexOf(isUserName(ch.getUserId())); 
			vals[i++] = booleanVals.indexOf(ch.getComment().equals("null") ? "false" : "true");
			vals[i++] = booleanVals.indexOf(ch.getComment().toLowerCase().contains("reverted") ? "true" : "false");
			vals[i++] = ch.getInsertWordCount();
			vals[i++] = ch.getDeleteWordCount();
			vals[i++] = ch.getDeleteWordCount()-ch.getInsertWordCount();
			vals[i++] = ch.getComment().equals("null") ? 0 : ch.getComment().length();
			vals[i] = data.attribute(i++).addStringValue(editsString); 
			if (!ch.getType().equals("?")){
				vals[i++] = classVals.indexOf(ch.getType());	
			}
			
			// insert the new instance to the container
			Instance ins = new Instance(1.0, vals);
			data.add(ins);	
		}
	}
	
	/**
	 * Validate the text string: IP address or not 
	 * @param text Input text
	 * @return true / false
	 */
	public String isUserName(String text){
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(text);
		return matcher.matches() ? "false" : "true";
	}
	
	/**
	 * Export the currently stored data set 
	 * @param dbFileName the input file name
	 * @param fileName the type of the current output
	 * @throws IOException 
	 */
	public void save(String dbFileName, String fileName) throws IOException{
		String arffFileName = dbFileName+"-"+fileName+".arff";
		System.out.println(dataSetType+": Saving file: "+arffFileName+"...");
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(arffFileName));
		saver.writeBatch();
		
	}
	
	/**
	 * Filter the currently stored data set with StringToWordVector
	 * @throws Exception
	 */
	public void filter() throws Exception{
		System.out.println(dataSetType+": Filtering with StringToWordVector...");

		StringToWordVector filter = new StringToWordVector();
		WordTokenizer tokenizer = new WordTokenizer();
		
		filter.setInputFormat(data);
		filter.setTokenizer(tokenizer);
		data = Filter.useFilter(data, filter);
	}
	
	/**
	 * Set the class index
	 */
	public void setClassIndex(){
		int classIndex = data.attribute("class").index();
		System.out.println(dataSetType+": Setting class index to "+classIndex+"...");
		data.setClassIndex(classIndex);
	}
	
	/**
	 * Selecting attributes with Ranker
	 * @param numberOfAttributes
	 * @throws Exception
	 */
	public void selectAttributes(int numberOfAttributes) throws Exception{
		System.out.println(dataSetType+": Selecting attributes with Ranker...");
		AttributeSelection filter = new AttributeSelection();
		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		Ranker search = new Ranker();
		setClassIndex();
		search.setNumToSelect(numberOfAttributes);
		filter.setSearch(search);
		filter.setEvaluator(eval);
		filter.setInputFormat(data);
		data = Filter.useFilter(data, filter);
	}

	public Instances getData() {
		return data;
	}

	public void setData(Instances data) {
		this.data = data;
	}
	
	public Instance getInstance(int index){
		return this.data.instance(index);
	}

	public ArrayList<ChangeLog> getChanges() {
		return changes;
	}

	public void setChanges(ArrayList<ChangeLog> changes) {
		this.changes = changes;
	}
	
	
	
	
	
	
}
