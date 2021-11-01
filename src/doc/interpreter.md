# The inner interpreter loop #
The inner interpreter loop is the pumping heart of the jemForth engine.
It has only 18 lines of java code and it's simplicity has a special beauty.

The only state variable it uses is the interpreter pointer {@code ip}.
It always must contain a valid address locator that ponts to an executable word cell.
If the ip contains zero, the interpreter ends. 

The <tt>**execute**</tt> method is usually called by the outer interpreter loop.
It might also be called to execute a specific word. It executes the word and calls <tt>_next</tt>
until the ip contains zero.

    public void execute(Word word) {
        word.execute(this);
        while (ip != 0) {
            _next();
        }
    }

The <tt>**_next**</tt> method increments the ip to point to the next cell,
looks up the word at the current position in the dictionary and exeutes it.
The jemForth calls the debug hook here to enable the debugger to stop and resume 
the execution. 

    public void _next() {
        int currentPosition = ip;
        ip = ip + CELL_SIZE;
        dictionary.fetchWord(currentPosition).execute(this);
    }

The <tt>**decol**</tt> method is used to execute a list of words. It's used to execute the
word list of a colon definition or part of a cell list words parameter list. It pushes the
current ip value in the return stack and sets the ip to the address provided as parameter.
This is the equivalent of a gosub command in BASIC or machine language. 

    public void docol(int pfa) {
        rStack.push(ip);
        ip = pfa;
    }
    
The <tt>**_exit**</tt> method is the last word in every colon word's parameter list.
It simply pops the top element from the return stack and sets the ip to it.   
This is the equivalent of a return command in BASIC, machine language or java.

    protected void _exit() {
        ip = rStack.pop();
    }
    
