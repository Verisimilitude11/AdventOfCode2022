/**
 * --- Day 7: No Space Left On Device ---
 *
 * Outline:
 *
 * 1. Read in the input file and store it in an ArrayList
 * 2. Parse the ArrayList and store the lines in a TreeMap
 * 3. Iterate over the TreeMap and find the top level directories
 * 4. Iterate over the TreeMap and find the files
 * 5. Iterate over the TreeMap and find the files that are first children
 * 6. Iterate over the TreeMap and find the files that are not first children
 *
 */

package Day7;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Scanner;

public class Part1 {
    private static int totalFileSize = 0;
    private static ArrayList<String> input = new ArrayList<String>();
    private static TreeMap<Integer, String> commandLines = new TreeMap<>();
    private static TreeMap<Integer, Directory> dirLines = new TreeMap<>();
    private static TreeMap<Integer, FileType> files = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        // Create a File object that represents the disk file.
        File file = new File("C:\\Users\\achar\\OneDrive\\Documents\\GitHub\\AdventOfCode2022\\Day7\\input.txt");
        parseLines(file);

        setTopLevelDirectories();

        // Iterate over the files in the TreeMap using the line numbers as the keys
        for (int lineNumber : files.keySet()) {
            int previousLineNum = lineNumber - 1;
            boolean firstFileFound = findParentDirectoryAndAdd(previousLineNum, files.get(lineNumber), dirLines,
                    commandLines,
                    files);
            // validate
            // System.out.println("File: " + files.get(lineNumber).filename + " Size: " + files.get(lineNumber).size);
            if (firstFileFound) {
                // do the same thing as for directories below, but for the files
                boolean fileFound = true;
                int fileLineNumber = lineNumber;
                while (fileFound) {
                    if (files.containsKey(fileLineNumber + 1)) {
                        // set first child to true
                        files.get(fileLineNumber + 1).isFirstChild = true;
                        fileLineNumber++;
                        // validate
                        //System.out.println("File: " + files.get(fileLineNumber).filename + " Size: "
                        //        + files.get(fileLineNumber).size);
                        int filePreviousLineNum = fileLineNumber - 1;
                        boolean check = findParentDirectoryAndAdd(filePreviousLineNum, files.get(fileLineNumber),
                                dirLines,
                                commandLines, files);
                        if (check) {
                            totalFileSize += files.get(fileLineNumber).size;
                            continue;
                        }
                    } else {
                        fileFound = false;
                    }
                }
            } /*
               * else {
               * if (files.containsKey(previousLineNum)) {
               * findDirParentAndAdd(previousLineNum, files.get(lineNumber), dirLines,
               * commandLines, files);
               * System.out.println("recursion");
               * }
               */
        }

        // loop over files again, look for files that are first children, then start
        // while loop, keep on adding to the parent directory
        for (int lineNumber : files.keySet()) {
            if (files.get(lineNumber).isFirstChild) {
                int previousLineNum = lineNumber - 1;
                boolean check = findParentDirectoryAndAdd(previousLineNum, files.get(lineNumber), dirLines,
                        commandLines,
                        files);
                if (check) {
                    totalFileSize += files.get(lineNumber).size;
                    continue;
                }
            }

            // validate
            // System.out.println("File: " + files.get(lineNumber).filename + " Size: " +
            // files.get(lineNumber).size);
        }

    }

    /**
     * This method parses the input and adds the lines to the appropriate TreeMap
     *
     * @param input
     * @param commandLines
     * @param dirLines
     * @param files
     * @throws FileNotFoundException
     */
    public static void parseLines(File file) throws FileNotFoundException {
        // Create a Scanner object for the file.
        Scanner sc = new Scanner(file);

        // Read the file contents into the ArrayList
        while (sc.hasNext()) {
            String thisLine = sc.nextLine();
            input.add(thisLine);
        }
        // Recognize lines that contain a command ($)
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).charAt(0) == '$') {
                // create a new entry in commandLines, with the line number as the key and the
                // command as the value
                commandLines.put(i + 1, input.get(i).substring(input.get(i).indexOf("$") + 1));
            } else if (input.get(i).contains("dir")) {
                // create a new entry in dirLines, with the line number as the key and the
                // directory as the value
                Directory newDir = new Directory();
                newDir.name = input.get(i).substring(input.get(i).indexOf("dir") + 4);
                dirLines.put(i + 1, newDir);
            } else if (input.get(i).matches(".*\\d.*")) {
                // create a new entry in files, with the line number as the key and the file as
                // the value
                FileType newFile = new FileType();
                newFile.size = Integer.parseInt(input.get(i).substring(0, input.get(i).indexOf(" ")));
                newFile.filename = input.get(i).substring(input.get(i).indexOf(" ") + 1);
                files.put(i + 1, newFile);
            }
        }
    }

    /**
     * This method finds the parent directory of the file and adds the file to the
     * directory. It also updates the size of the directory.
     *
     * @param previousLineNum
     * @param file
     * @param dirLines
     * @param commandLines
     * @param files
     * @return false if the file is not added to a directory, true if the file is
     *         added
     */
    private static boolean findParentDirectoryAndAdd(int previousLineNum, FileType file,
            TreeMap<Integer, Directory> dirLines,
            TreeMap<Integer, String> commandLines, TreeMap<Integer, FileType> files) {
        // check if previous line is a dir
        if (dirLines.containsKey(previousLineNum)) {
            // add file to dir
            dirLines.get(previousLineNum).files.add(file);
            // update size of dir
            dirLines.get(previousLineNum).size += file.size;
            // mark file as first child of dir
            file.isFirstChild = true;
            return true;
        } else {
            // if previous line is not a command, keep going up 1 line until its found
            boolean notFound = true;
            while (notFound) {
                if (dirLines.containsKey(previousLineNum)) {
                    // add file to dir
                    dirLines.get(previousLineNum).files.add(file);
                    // update size of dir
                    dirLines.get(previousLineNum).size += file.size;
                    // mark file as first child of dir
                    file.isFirstChild = true;
                    notFound = false;
                    return true;
                } else {
                    previousLineNum--;
                }
            }
        }
        return false;
    }

    private static void setTopLevelDirectories() {
        // loop over dir list, if command is found designate it as a top level directory
        // if dir is found that is top level, designate it also as a top level directory
        for (int lineNumber : dirLines.keySet()) {
            if (commandLines.containsKey(lineNumber - 1)) {
                dirLines.get(lineNumber).topLevel = true;
                // validate
                System.out.println("Top level directory: " + dirLines.get(lineNumber).name);

                // keep looping down until you dont find a directory
                boolean dirFound = true;
                int dirLineNumber = lineNumber;
                while (dirFound) {
                    if (dirLines.containsKey(dirLineNumber + 1)) {
                        dirLines.get(dirLineNumber + 1).topLevel = true;
                        // validate
                        System.out.println("Top level directory: " + dirLines.get(dirLineNumber + 1).name);
                        dirLineNumber++;
                    } else {
                        dirFound = false;
                    }
                }
            } /*
               * else if (dirLines.containsKey(lineNumber - 1)) {
               * if (dirLines.get(lineNumber - 1).topLevel) {
               * dirLines.get(lineNumber).topLevel = true;
               * }
               * }
               */
        }
    }
}
