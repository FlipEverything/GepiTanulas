import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Used for batch filtering and labeling the test data set
 */
public class Classifier {
	BayesNet classifier;
	Evaluation evaluation;
	ARFF train;
	ARFF test;
	String testFileName;
	String trainFileName;
	String output = "";
	
	public static final String FILTER_FILENAME = "filtered";
	public static final String ATTRIBUTE_SELECTION_FILENAME = "selected";
	
	/**
	 * Bayes Net classifier
	 * @param train Train instances
	 * @param test Test Instances
	 * @param trainFileName Train file name
	 * @param testFileName Test file name
	 */
	public Classifier(ARFF train, ARFF test, String trainFileName, String testFileName){
		classifier = new BayesNet();
		this.train = train;
		this.test = test;
		this.trainFileName = trainFileName;
		this.testFileName = testFileName;
	}
	
	/**
	 * Batch filtering
	 * Preprocessing
	 * With this method the train and test data sets will be compatible
	 * @throws Exception
	 */
	public void batchStringToWordVector() throws Exception{
		System.out.println(" - Batch filtering with StringToWordVector....");
		
		StringToWordVector filter = new StringToWordVector();
		// set input format to the train's format
		filter.setInputFormat(train.getData());
		
		train.setData(Filter.useFilter(train.getData(),filter));
		train.save(trainFileName, FILTER_FILENAME);
		// filter the test data set as the train
		test.setData(Filter.useFilter(test.getData(),filter));
		test.save(testFileName, FILTER_FILENAME);
	}
	
	/**
	 * Batch filtering
	 * Attribute selection with Ranker
	 * With this method the train and test data sets will be compatible
	 * @param numberOfAttributes the number of attributes will be selected
	 * @throws Exception
	 */
	public void batchAttributeSelection(int numberOfAttributes) throws Exception{
		System.out.println(" - Batch attribute selection with Ranker...");
		train.setClassIndex();
		
		AttributeSelection attributeSelection = new AttributeSelection();
		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		
		Ranker search = new Ranker();
		search.setNumToSelect(numberOfAttributes);
	
		attributeSelection.setSearch(search);
		attributeSelection.setEvaluator(eval);
		// set input format to the train's format
		attributeSelection.setInputFormat(train.getData());
			
		train.setData(Filter.useFilter(train.getData(), attributeSelection));
		train.save(trainFileName, ATTRIBUTE_SELECTION_FILENAME);
		// filter the test data set as the train
		test.setData(Filter.useFilter(test.getData(), attributeSelection));
		test.save(testFileName, ATTRIBUTE_SELECTION_FILENAME);
	}
	
	/**
	 * Training: Bayes Net
	 * Cross-validation on the train data set
	 * @throws Exception
	 */
	public void train() throws Exception{
		System.out.println(" - Training...");
		
		classifier.buildClassifier(train.getData());
		evaluation = new Evaluation(train.getData());
		// cross validation
		evaluation.crossValidateModel(classifier, train.getData(), 10, new Random(1));
		
		//System.out.println(evaluation.toSummaryString());
		System.out.println(evaluation.toClassDetailsString());
		//System.out.println(evaluation.toMatrixString());
	}
	
	/**
	 * Labeling the unlabeled test instances 
	 * @throws Exception
	 */
	public void classify() throws Exception{
		System.out.println(" - Classifying and labeling test instances...");
		// set the class index for the classifier
		test.setClassIndex();
		Instances labeled = new Instances(test.getData());
		
		for (int i=0; i< test.getData().numInstances(); i++){
			double clsLabel = classifier.classifyInstance(test.getData().instance(i));
			labeled.instance(i).setClassValue(clsLabel);
			// create the output file
			output += test.getChanges().get(i).getDocId()+"\t"+labeled.classAttribute().value((int) clsLabel)+"\n";
		}
		test.setData(labeled);

	}
	
	/**
	 * Save the output file
	 * @param fileName
	 * @throws IOException
	 */
	public void saveOutput(String fileName) throws IOException{
		System.out.println(" - Saving output to "+fileName+"...");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		out.write(output);
		out.close();
	}
}
