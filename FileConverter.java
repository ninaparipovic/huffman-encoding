import java.io.*;
import java.util.*;
/**
 * File converter implementing the Huffman coding method which allows files to be compressed and decompressed
 * @author ninaparipovic, PS3 Winter19 CS10
 * 
 */
public class FileConverter {
	public String pathName= new String();
	public String compressedPathName = new String();
	public String decompressedPathName = new String();

	/**
	 * Constructor
	 * @param pathName the path name of the file to open
	 */
	public FileConverter(String pathName) {
		this.pathName = pathName;
		compressedPathName = "outputs/"+pathName.substring(7, pathName.length() -4)+"_compressed.txt";
		decompressedPathName = "outputs/"+pathName.substring(7, pathName.length() -4)+"_decompressed.txt";
	}
	/**
	 * Class for the data value in the binary tree, allowing the tree to hold two values; frequency and character 
	 */
	private class TreeData {
		private int frequency;
		private char character;
		
		public TreeData(char character, int frequency) { 
			this.character = character;
			this.frequency = frequency;
		}
		public TreeData(int frequency) {
			this.frequency = frequency;
		}
		public int getF() {
			return frequency;
		}

		public char getC() {
			return character;
		}
		public String toString() {
			return "(" + character + ":" + frequency + ")";
		}

	}
	/**
	 * @Override
	 * Overrides the comparator method
	 * @return -1 if the second value is greater than the first 
	 * @return 1 if the first value is greater than the second
	 * @return 0 if the values are equal
	 */
	public class TreeComparator implements Comparator<BinaryTree<TreeData>>{
		public int compare(BinaryTree<TreeData> e1, BinaryTree<TreeData> e2) {
		if (e1.getData().getF()< e2.getData().getF()) return -1;  
		else if (e1.getData().getF() > e2.getData().getF()) return 1; 
		else return 0;
		}
	}
	/**
	 * Creates a frequency table with the character as the key and the frequency as the value 
	 * @return frequencyTable
	 * @throws IOException
	 */
	public Map<Character, Integer> frequencyTable() throws IOException {
		Map<Character, Integer> frequencyTable = new TreeMap<Character, Integer>();
		BufferedReader input = new BufferedReader(new FileReader(pathName));
		int c;
		while ((c=input.read()) != -1) {
			if (frequencyTable.containsKey((char) c)) {
				// if the character is within the table, add to its frequency
				frequencyTable.put((char) c, frequencyTable.get((char) c)+1);
			}
			// else, add it to the table as a new entry 
			else {
				frequencyTable.put((char) c, 1);
			}
		}
		input.close();
		return frequencyTable;
	}
	/**
	 * Creates a priority queue of binary trees, prioritized by the frequency contained in each binary tree 
	 * @param freqTable frequency table containing all the different characters and their corresponding frequencies
	 * @return pq priority queue 
	 */
	public PriorityQueue<BinaryTree<TreeData>> createPriorityQueue(Map<Character, Integer> freqTable){
		Comparator<BinaryTree<TreeData>> treeComparator = new TreeComparator(); //  initialize the comparator 
		PriorityQueue<BinaryTree<TreeData>> pq = new PriorityQueue<BinaryTree<TreeData>>(treeComparator); 
		for (char c: freqTable.keySet()) {
			// for each character in the frequency table, create a new binary tree holding the character and frequency 
			TreeData data = new TreeData(c, freqTable.get(c));
			BinaryTree<TreeData> tree = new BinaryTree<TreeData>(data);
			pq.add(tree);
		}
		// return the priority queue
		return pq;
	}
	/**
	 * Creates BinaryTree sorted with lower frequencies at the bottom and higher frequencies at the top 
	 * @return codeTree
	 */
	public BinaryTree<TreeData> codeTree(Map<Character, Integer> freqTable) {
		PriorityQueue<BinaryTree<TreeData>> pq = createPriorityQueue(freqTable);
		while (pq.size() > 1) {
			// extract the two single trees with the lowest frequency 
			BinaryTree<TreeData> T1 = pq.remove();
			BinaryTree<TreeData> T2 = pq.remove();
			TreeData freqSum = new TreeData(T1.getData().getF() + T2.getData().getF());
			// create the new tree
			BinaryTree<TreeData> T = new BinaryTree<TreeData>(freqSum, T1, T2);
			pq.add(T);
		}
		BinaryTree<TreeData> codeTree = null;
		if (pq.size()>0) {
			 codeTree = pq.remove();	
		}
		return codeTree;
	}
	/**
	 * Creates a map with the key as the character and the value as the string of 0's and 1's that is the code word 
	 * @param binaryTree 
	 * @returns codeMap  
	 */
	public Map<Character, String> codeMap(BinaryTree<TreeData> binaryTree){
		Map<Character, String> codeMap = new TreeMap<Character, String>();
		if (binaryTree != null) {
			if(binaryTree.isLeaf()) {
				codeMap.put(binaryTree.getData().getC(), "1");
			}
			codeMapHelper(binaryTree, codeMap, "");
			if (binaryTree.size() == 1) {
				codeMap.put(binaryTree.getData().getC(), "1");
			}
		}
		return codeMap;	
	}
	/**
	 * Helper for the codeMap method 
	 * @param BinaryTree  
	 * @param codeMap
	 * @param string 
	 * 
	 */
	public void codeMapHelper(BinaryTree<TreeData> binaryTree, Map<Character, String> codeMap, String string) {
		if(binaryTree.isLeaf()) { 
			codeMap.put(binaryTree.getData().getC(), string);
			return;
		}
		codeMapHelper(binaryTree.getLeft(), codeMap, string + "0");
		codeMapHelper(binaryTree.getRight(), codeMap, string + "1");
	}
	/**
	 * Reads the input file and creates an output file with the compressed code
	 * @param pathName path name of the file  
	 * @param codeMap  
	 * @throws IOException
	 */
	public void encodeMessage(Map<Character, String> codeMap, String pathName) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(pathName));
		BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);
		int c;
		while ((c=input.read()) != -1) {
			String str = codeMap.get((char) c);
			int i=0;
			while (i<str.length()) {
				if (str.charAt(i) == '0') {
					bitOutput.writeBit(false);
				}
				else {
					bitOutput.writeBit(true); 
				} 
				i++;
			}	
		}
		input.close();
		bitOutput.close();
	}
	/**
	 * Reads the compressed file and creates an output file with the decompressed code
	 * @param pathName path name of the file  
	 * @param codeMap  
	 * @param tree
	 * @throws IOException
	 */
	public void decodeMessage(Map<Character, String> codeMap, BinaryTree<TreeData> tree, String pathName) throws IOException {
		BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
		BufferedWriter output = new BufferedWriter(new FileWriter(decompressedPathName));
		// blank file
		if (tree == null) {
			output.close();
			bitInput.close();
		}
		// single node tree
		else if (tree.isLeaf()) {
			int i = 0;
			while (i < tree.getData().getF()) {
				output.write(tree.getData().getC());
				i ++;
			}
		}
		else {
			// while there is something to read, iterate down the tree until a leaf is reached
			while(bitInput.hasNext()) {
				BinaryTree<TreeData> currentTree = tree;
				while(! currentTree.isLeaf()) {
				boolean bit = bitInput.readBit();
					if (bit) {
						currentTree = currentTree.getRight();	
					}
					else {
						currentTree = currentTree.getLeft();
					}
				}
				// drops out of the while loop therefore it is a leaf
				char c = currentTree.getData().getC();
				// write out the character at the current leaf
				output.write(c); 
			}
		}
		output.close();
		bitInput.close();
	}
	

	
	public static void main(String[] args) throws IOException {
		// test for file containing "a" repeated 3 times 
		String pathName = "inputs/text.txt";
		FileConverter huffman = new FileConverter(pathName);
		
		Map<Character, Integer> map = huffman.frequencyTable();
		System.out.println("frequency table:");
		System.out.println(map);
		BinaryTree<TreeData> tree = huffman.codeTree(map);
		System.out.println("code tree:");
		System.out.println(tree);
		Map<Character, String> codeMap = huffman.codeMap(tree);
		System.out.println("code map:");
		System.out.println(codeMap);
		huffman.encodeMessage(codeMap, pathName);
		huffman.decodeMessage(codeMap, tree, pathName);
		
		// test for file containing single character "H"
		String pathName1 = "inputs/SingleCharacter.txt";
		FileConverter huffman1 = new FileConverter(pathName1);
		
		Map<Character, Integer> map1 = huffman1.frequencyTable();
		System.out.println("frequency table:");
		System.out.println(map1);
		BinaryTree<TreeData> tree1 = huffman1.codeTree(map1);
		System.out.println("code tree:");
		System.out.println(tree1);
		Map<Character, String> codeMap1 = huffman1.codeMap(tree1);
		System.out.println("code map:");
		System.out.println(codeMap1);
		huffman1.encodeMessage(codeMap1, pathName1);
		huffman1.decodeMessage(codeMap1, tree1, pathName1);
		
		// test for blank file
		String pathName2 = "inputs/blankFile.txt";
		FileConverter huffman2 = new FileConverter(pathName2);
		
		Map<Character, Integer> map2 = huffman2.frequencyTable();
		System.out.println("frequency table:");
		System.out.println(map2);
		BinaryTree<TreeData> tree2 = huffman2.codeTree(map2);
		System.out.println("code tree:");
		System.out.println(tree2);
		Map<Character, String> codeMap2 = huffman2.codeMap(tree2);
		System.out.println("code map:");
		System.out.println(codeMap2);
		huffman2.encodeMessage(codeMap2, pathName2);
		huffman2.decodeMessage(codeMap2, tree2, pathName2);

		String pathName3 = "inputs/USConstitution.txt";
		FileConverter huffman3 = new FileConverter(pathName3);
		
		Map<Character, Integer> map3 = huffman3.frequencyTable();
		System.out.println("frequency table:");
		System.out.println(map3);
		BinaryTree<TreeData> tree3 = huffman3.codeTree(map3);
		System.out.println("code tree:");
		System.out.println(tree3);
		Map<Character, String> codeMap3 = huffman3.codeMap(tree3);
		System.out.println("code map:");
		System.out.println(codeMap3);
		huffman3.encodeMessage(codeMap3, pathName3);
		huffman3.decodeMessage(codeMap3, tree3, pathName3);
		
		String pathName4 = "inputs/WarAndPeace.txt";
		FileConverter huffman4 = new FileConverter(pathName4);
		
		Map<Character, Integer> map4 = huffman4.frequencyTable();
		System.out.println("frequency table:");
		System.out.println(map4);
		BinaryTree<TreeData> tree4 = huffman4.codeTree(map4);
		System.out.println("code tree:");
		System.out.println(tree2);
		System.out.println(tree4);
		Map<Character, String> codeMap4 = huffman4.codeMap(tree4);
		System.out.println("code map:");
		System.out.println(codeMap4);
		huffman4.encodeMessage(codeMap4, pathName4);
		huffman4.decodeMessage(codeMap4, tree4, pathName4);
		
	}
}


