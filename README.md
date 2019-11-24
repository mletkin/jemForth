# Java Emulated Forth

This is a complete Forth-83 implementation of the Forth programming language in Java.
The project is a play ground to experiment with a threaded code language.
It's not a production ready Forth implementation.

The project is work in progress and so is this documentation.

## The Forth engine
'''JemEngine'''.is the central class of the forth engine.
It implements the threaded code compiler/interpreter with some core word definitions.
It also provides the hooks for basic I/O and for the debugger.

Specific Forth variants may be implemented by extending this class.
The '''Forth83Engine''' class is an example of a Forth-83 implementation.
Other variants (like Forth 2012) might follow.

## The GUI
After compiling the project, start the GUI by executing the main method
of the class '''ForthIde''' in the package '''io.github.mletkin.jemforth.frontend'''.
It starts an eclipse like IDE with editor and debugger.

## issues

### Strings
Java uses UTF-16 character encoding. That isn't really compatible with ASCII enoding
because UTF-characters in a string might use a different number of bytes.
Especially the use of block buffer implementation provided by the Forth 83 engine may lead to
unexpected results.

### text console
A retro style text console is already implemented but not yet commited. 
