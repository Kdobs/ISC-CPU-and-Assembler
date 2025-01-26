//Author: Kyle Dobson
//idea of the problem
//Take the word loaded in Memory[0] and count how many bits in the word are one
//The main steps of the problem are testing the last bit in the word, if it is one increment Memory[1] or the result
// this test is completed by using a mask to test the correct bit, to test the next bit subtract the word by the mask
// and double the mask to do what is equivalent to a right shift. When the word is zero the program is finished

//set d to the word
@0
D=M
// memory[1] to 0 for the count variable
@1
M=0
//make a mask for isolating the last bit
@2
M=1
//start of the loop
(LOOP)
//set D back to the word
@0
D=M
// go to the halt when the word is zero
@DONE
D;JEQ
//isolate the last bit
@2
D=D&M
//Increment if the last bit is not zero D is the last bit
@INCREMENT
D;JNE
//spot to continue after incrementing
(CONTINUE)
//shifting the bit
//load the word into D for manipulation
@0
D=M
// clear the last bit with Memory[2] or the Mask
@2
D=D-M
//store the new word value
@0
M=D
//Update the mask
//load the mask to D
@2
D=M
//double the mask to move the bit up
@2
M=D+M
//load the word the check if loop needs to run again
@0
D=M
//restart the loop if the
@LOOP
0;JMP

//Increment the result or Memory[1]
(INCREMENT)
@1
M=M+1
@CONTINUE
0;JMP

(DONE)
HALT