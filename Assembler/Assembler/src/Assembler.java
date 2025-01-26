import edu.princeton.cs.algs4.ST;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class Assembler {
    public static void main(String[] args) throws Exception {
        File asmFile = new File("src/" + args[0] + ".asm");


        File workfile = removeWhiteSpace(asmFile); //removes all whitespace and comments
        File Bin = new File(args[0] + ".bin");
        File Hex = new File(args[0] + ".txt");
        ST <String, Integer> symbolTable = firstPass(workfile);// puts things in the symbol table
        secondPass(workfile,symbolTable, Bin, Hex);//creates the output files


    }

    public static File removeWhiteSpace(File asmFile) throws IOException {
        // create a temp file that will have all the white space and comments removed from the assembly
        String tempName = "temp.txt";
        File tempFile = new File(tempName);
        FileWriter tempwriter = new FileWriter(tempName);
        Scanner fileScanner = new Scanner(asmFile);
        // this will assume that code and comments are not on the same line
        while (fileScanner.hasNext()){
            String line = fileScanner.nextLine();
            String newLine = line.strip();
            if(!newLine.isEmpty()){
                if(newLine.startsWith("#")){ // test for a # comment

                } else if (newLine.startsWith("//")) { //check for a // comment

                }else{//line is not a comment
                    tempwriter.write(newLine + System.lineSeparator());
                }
            }
        }
        tempwriter.close();
        fileScanner.close();
        return tempFile;
    }

    public static ST firstPass(File asmFile) throws Exception {
        //first pass will add all labels to the symbol table
        // file should have no white space or empty lines in it at this point
        ST<String, Integer> symbolTable = new ST<>();
        int lineCounter = 1; //keep track of the line for error codes
        int LC = 0; // location counter
        Scanner fileScanner = new Scanner(asmFile);
        while(fileScanner.hasNext()){
            String line = fileScanner.nextLine();
            if(line.startsWith("(")){
                // get rid of the parentheses
                line = line.replace("(", ""); line = line.replace(")", "");
                if(symbolTable.contains(line)){
                    duplicateLabelError(lineCounter, line);
                }else if(testLabel(line)){ //test the character in the label
                    invalidLabelError(lineCounter, line);
                }else{
                    symbolTable.put(line, LC); // add label to symbol table
                }
            }else{
                LC++; //increase the location counter for instructions
            }
            lineCounter++;
        }
        fileScanner.close();
        return symbolTable;
    }

    public static void secondPass(File asmFile, ST<String, Integer> symbolTable, File Bin, File Hex) throws Exception {
        //maps for binary encoding
         final Map<String, String> CompMap = Map.ofEntries(Map.entry("0", "0101010"),
                Map.entry("1", "0111111"), Map.entry("-1", "0111010"), Map.entry("D", "0001100"),
                Map.entry("A", "0110000"), Map.entry("M", "1110000"), Map.entry("!D", "0001101"),
                Map.entry("!A", "0110001"), Map.entry("!M", "1110001"), Map.entry("-D", "0001111"),
                Map.entry("-A", "0110011"), Map.entry("-M", "1110011"), Map.entry("D+1", "0011111"),
                Map.entry("A+1", "0110111"), Map.entry("M+1", "1110111"), Map.entry("D-1", "0001110"),
                Map.entry("A-1", "0110010"), Map.entry("M-1", "1110010"), Map.entry("D+A", "0000010"),
                Map.entry("D+M", "1000010"), Map.entry("D-A", "0010011"), Map.entry("D-M", "1010011"),
                Map.entry("A-D", "0000111"), Map.entry("M-D", "1000111"), Map.entry("D&A", "0000000"),
                Map.entry("D&M", "1000000"), Map.entry("D|A", "0010101"), Map.entry("D|M", "1010101"));
         final Map<String, String> DestMap = Map.of("null", "000", "M", "001", "D", "010", "MD",
                "011", "A", "100", "AM", "101", "AD", "110", "AMD", "111");
         final Map<String, String> JumpMap = Map.of("null", "000", "JGT", "001", "JEQ", "010",
                "JGE", "011", "JLT", "100", "JNE", "101", "JLE", "110", "JMP",
                "111");
        //this creates the binary and hex files(should only do one file then convert to the other version at the end
        int lineLocation = 1; // keep track of active line number for errors
        FileWriter BinWriter = new FileWriter(Bin);
        FileWriter HexWriter = new FileWriter(Hex);
        HexWriter.write("v2.0 raw" + System.lineSeparator()); // need for the hex file
        Scanner fileScanner = new Scanner(asmFile);
        String binValue = "";
        int memoryAdress = 28672;
        while (fileScanner.hasNext()){
            String line = fileScanner.nextLine();
            if(!line.startsWith("(")){ //prevents instructions from being written twice
                //test if the line is an address instruction(starts with a @)
                if(line.startsWith("@")){
                    line = line.replace("@", "");
                    if(line.matches(".*[a-zA-Z].*")){//has letters
                        if(testLabel(line)){
                            invalidLabelError(lineLocation, line);
                        }
                        if(symbolTable.contains(line)){ //already in symbol table
                            binValue = '0' + binary16bit(Integer.toBinaryString(symbolTable.get(line)));
                        }else{
                            symbolTable.put(line, memoryAdress);
                            binValue = "0" + binary16bit(Integer.toBinaryString(memoryAdress));
                            memoryAdress++;
                        }
                    }else{
                        int value = Integer.parseInt(line);
                        binValue = "0" + binary16bit(Integer.toBinaryString(value));
                    }
                }
                // test if the line is a halt command(will be HALT encodes to 0x8000)
                else if(line.equals("HALT")){
                    binValue = "1000000000000000";
                }
                //test if the line is a compute instruction(test for the operators)
                else if(line.matches(".*[\\+\\-!&|=;].*")){
                    String dest = "null", jump = "null", comp = "";//setting default values
                    if(line.contains(";")){ //has a jump
                        String[] jumpfield = line.split(";");
                        jump = jumpfield[1];
                        comp = jumpfield[0];
                    }
                    if(line.contains("=")){ // has a destination
                        String[] destfield = line.split("=");
                        dest = destfield[0]; // isolate the destination field
                        comp = destfield[1];
                    }
                    if(line.contains(";") && line.contains("=")){
                        String[] destfield = line.split("=");
                        line = destfield[1];
                        String[] jumpfield = line.split(";");
                        comp = jumpfield[0];
                    }else if(!line.contains(";") && !line.contains("=")){
                        comp = line;
                    }
                    // validate the instruction
                    if(!CompMap.containsKey(comp)){
                        invaildCompError(lineLocation, line);
                    }
                    if(!DestMap.containsKey(dest)){
                        invalidDestionationError(lineLocation, line);
                    }
                    if(!JumpMap.containsKey(jump)){
                        invalidJumpError(lineLocation, line);
                    }
                    binValue = "111" + CompMap.get(comp) + DestMap.get(dest) + JumpMap.get(jump);
                }
                BinWriter.write(binValue + System.lineSeparator());
                HexWriter.write(binarytoHex(binValue).toUpperCase() + System.lineSeparator());
            }
            lineLocation++;
        }
        BinWriter.close();
        HexWriter.close();
        fileScanner.close();
    }

    //Error outputs
    private static void duplicateLabelError(int lineNumber, String line) throws Exception {
        throw new Exception("Duplicate label detected near line: " + lineNumber + " Line: " + line);
    }
    private static void invalidLabelError(int lineNumber, String line) throws Exception {
        throw new Exception("Invalid label detected near line: " + lineNumber + " Line: " + line);
    }
    private static void invalidDestionationError(int lineNumber, String line) throws Exception{
        throw new Exception("Invalid destination near line: " + lineNumber + " Line: " + line);
    }
    private static void invalidJumpError(int lineNumber, String line) throws Exception{
        throw new Exception("Invalid jump location near line: " + lineNumber + " Line: " + line);
    }
    private static void invaildCompError(int lineNumber, String line) throws Exception{
        throw new Exception("Invalid Computation near line: " + lineNumber + " Line: " + line);
    }

    //helper functions/validity testing
    private static boolean testLabel(String s){
        // returns true if the label has valid characters in it
        char[] characterArray = s.toCharArray();
        if(Character.isDigit(characterArray[0])){
            return true;
        }
        return s.matches("^[a-zA-Z0-9_]+$");
    }
    private static String binarytoHex(String binary){
        int decimal = Integer.parseInt(binary, 2);
        String hex = Integer.toHexString(decimal);
        while(hex.length()<4){
            hex = "0" + hex;
        }
        return hex;
    }
    private static String binary16bit(String binaryString){
        // zero extend the binary value to 15 digits(need to leave room for the address bit)
        while(binaryString.length()< 15){
            binaryString = "0" + binaryString;
        }
        return binaryString;
    }
}
