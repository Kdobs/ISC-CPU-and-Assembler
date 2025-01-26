My tests will include the provided test.asm, sum100.asm, DuplicateLabel.asm, InvalidComp.asm, InvalidDestionation.asm, InvalidJump.asm and, InvalidLabel.asm.
The Test.asm will test that valid computations will run and the assembler will output Binary and Hex values for the file.
The sum100.asm will test that the assembler will output the correct binary and Hex values for the output files.
The DuplicateLabel.asm will test for duplicate labels and will throw an exception if duplicate labels are detected
The InvalidComp.asm will test for an invalid computation field and will throw an exception if an invalid Comp field is detected
The InvalidDestionation.asm will test for an invalid destination field and will throw an exception if an invalid Destination is detected
The InvalidJump.asm will test for an invalid Jump field and throw an exception if an invalid jump is detected 
The InvalidLabel.asm will test for a invalid label and throw an exception if an invalid label is detected

Explanation for the bitCountRom.asm:
The main loop of the problem is to isolate the last bit of the word, test if the last bit is one, if it is increment the result, then shift the word to the right and repeat the loop 
A more detailed description of the loop parts
Isolating the last bit:
	To isolate the last bit in the word we start with a value of zero and store it in Memory[2]. By using an & we can test if the ones place is 1 or 0. After testing the last bit we double the mask each iteration to move up the string
Shifting
	The program completes a shift by using the mask from isolating the last bit by subtracting the mask from the word to clear the last bit. This will complete a right shift.
Breaking out of the loop
	After enough iterations the word will become zero because of the shifting, when the word is zero it will jump to the halt instruction
	I chose this design because it allowed the Loop to have an easy break condition that would take me straight to the end. 	