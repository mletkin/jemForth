# Java Emulated Forth

This is a complete Forth-83 implementation of the Forth programming language in Java.
The project is a play ground to experiment with a threaded code language.
It's not a production ready Forth implementation.

The project is work in progress and so is this documentation.

## The Forth engine
```JemEngine``` is the central class of the forth engine.
It implements the threaded code compiler/interpreter with some core word definitions.
It also provides the hooks for basic I/O and for the debugger.

Specific Forth variants may be implemented by extending this class.
The ```Forth83Engine``` class is an example of a Forth-83 implementation.
Other variants (like Forth 2012) might follow.

## The GUI
After compiling the project, start the GUI by executing the main method
of the class ```ForthIde``` in the package ```io.github.mletkin.jemforth.frontend```.
It starts an eclipse like IDE with editor and debugger.

## issues

### Strings
Java uses UTF-16 character encoding. That isn't really compatible with ASCII enoding
because UTF-characters in a string might use a different number of bytes.
Especially the use of block buffer implementation provided by the Forth 83 engine may lead to
unexpected results.

### text console
Running the ```main``` method of class ```F83ConsoleReactive``` will start a retro style monochrome text console in a window.
The default color is amber (the colour of my first monitor) and two fonts are available.
The console adds a vocabulary ```CRT``` with some non-standard Words to control the console
- *font* sets the font type
- *size* sets the font size 
- *color* sets teh font color
- *fontlist* presents a list of installed fonts
- *AMBER* is a constant for amber font colour
- *GREEN* is a constant for green font colour

#### colour
The word ```color``` are implemented as an integer variable.
The value is interpreted as a 24 Bit RGB-colour code.
To set the colour to "green" use
 GREEN color !
or
 HEX 33FF33 color !

### font
Actually only consolas and Monospaced are fonts provided by Windows (jemForth hasn't been tested on Linux yet).
If you want tu use other fonts, put the .ttf file in the ```src/main/resources/fonts``` folder and adjust the name
in the ```fonts``` array in the class ```F83ReactiveConsoleWindow``` class.

The word ```fontlist``` prints the name and a number for each font.
The number an be stored using the word ```font```in an integer variable.
To set the default font use
 0 font !


