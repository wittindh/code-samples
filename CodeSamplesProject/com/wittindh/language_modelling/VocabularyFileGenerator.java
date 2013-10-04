package com.wittindh.language_modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

// @author David Witting

public class VocabularyFileGenerator {

	//newline character
	private final static String nl = System.getProperty("line.separator");

	/*
	 * Reads in all files in a directory and outputs the unique words to a file. 
	 * 
	 */
	public static void main(String[] args) {
		File indir = new File(args[0]);
		File outputfile  = new File(args[1]);
		generateVocabularyFile(indir, outputfile);

	}
	/*
	 * Reads in all files in a given directory and outputs the unique words to a file
	 * 
	 * @param indir directory containing files to be read
	 * @param outputfile file to create to contain vocabulary information
	 */
	public static void generateVocabularyFile(File indir, File outputfile){
		try {
			FileWriter output = new FileWriter(outputfile);
			Set<String> vocab = new TreeSet<String>();
			for(File f : indir.listFiles()){
				try{
					addVocabularyToSet(f, vocab);
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}
			}
			for(String token : vocab){
				output.write(token+nl);
			}
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
	 * Reads in a file and adds all the unique words to an existing set.
	 * 
	 * @param f file to be read
	 * @param vocab the set to which the vocabulary words are added
	 * @throws FileNotFoundException when file is not found.
	 */
	public static void addVocabularyToSet(File f, Set<String> vocab) throws FileNotFoundException{
		Scanner filescanner = new Scanner(f);
		while(filescanner.hasNextLine()){
			Scanner linescanner = new Scanner(filescanner.nextLine());
			while(linescanner.hasNext()){
				vocab.add(linescanner.next());
			}
			linescanner.close();
		}
		filescanner.close();
	}
}
