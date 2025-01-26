//this file will test duplicate labels 
//count down from ten
@0
M=1
(LOOP)
@0
M=M-1
@LOOP
M;JNE

//Second loop
(LOOP)
@0
M=M+1
@Loop
M;JEQ

HALT

