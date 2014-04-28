public class Main {
	private static String trainFileName;
	private static String testFileName;
	
	public static final int NUMBER_OF_ATTRIBUTES = 40;
	public static final String[] classes = {"regular", "vandalism"};
	
	
	/**
	 * @param args Command-line parameters
	 */
	public static void main(String[] args) {
		if (args.length != 2){
			System.out.println("Not enough parameters.\r\nUsage: program [trainFileName] [testFileName]\r\nExample: program data/train data/test");
		} else {
			trainFileName = args[0];
			testFileName = args[1];
			
			try {
				// Parse the train
				ARFF train = new ARFF(trainFileName, classes, null, "train");
				train.create();
				train.save(trainFileName, "raw");
				
				// Parse the test
				ARFF test = new ARFF(testFileName, classes, null, "test");
				test.create();
				test.save(testFileName, "raw");
				
				Classifier c = new Classifier(train, test, trainFileName, testFileName);
				c.batchStringToWordVector();
				c.batchAttributeSelection(NUMBER_OF_ATTRIBUTES);
				c.train();
				c.classify();
				c.saveOutput("predictions.txt");
				
				System.out.println(" - Exiting...");
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			//validateFilter();
			
		}
	}
	
	/**
	 * Only for testing purposes
	 * Create single filter output
	 * (Comparing batch filter output and single filter output)
	 */
	public static void validateFilter() throws Exception{
		ARFF arff = new ARFF(trainFileName, classes, null, "train");
		arff.create();
		arff.filter();
		arff.save(trainFileName, "filter-single");
		arff.selectAttributes(NUMBER_OF_ATTRIBUTES);
		arff.save(trainFileName, "selected-single");
	}
	
	

}
