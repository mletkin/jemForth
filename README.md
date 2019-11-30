# Java Emulated Forth
This is an implementation of the Forth programming language in Java.
The standard is implements is mainly that of Forth-83 but contains elements of
figForth from 1979 and the Forth-2012 standard.

The project is a play ground to experiment with a threaded code language and
tries to stick to the original concepts (as the linear memory structure).

It's not a production ready Forth implementation.

The project is work in progress and so is this documentation.

## Getting started
Just run the maven script and start either the GUI or the text console (see the paragraphs below).
You will need Java 9 as the project uses jig saw and reactive streams for the text console.

## The Forth engine
```JemEngine``` is the central class of the forth engine.
It implements the threaded code compiler/interpreter with some core word definitions.
It also provides the hooks for basic I/O and for the debugger.

Specific Forth variants may be implemented by extending this class.
The ```Forth83Engine``` class is an example of a Forth-83 implementation.
Other variants (like Forth 2012) might follow.


## the text console
Running the ```main``` method of class ```F83ConsoleReactive``` will start a retro style monochrome text console in a window.
The default color is amber (the colour of my first monitor) and two fonts are available.
The console adds a vocabulary ```CRT``` with some non-standard Words to control the console
- *font* sets the font type
- *size* sets the font size 
- *color* sets teh font color
- *fontlist* presents a list of installed fonts
- *AMBER* is a constant for amber font colour
- *GREEN* is a constant for green font colour

### setting the font colour
The word ```color``` are implemented as an integer variable.
The value is interpreted as a 24 Bit RGB-colour code.
To set the colour to "green" use
```
GREEN color !
```
or
```
HEX 33FF33 color !
```
### setting the font size
The word ```size``` are implemented as an integer variable.
The value is interpreted as the font size.
To set the font size to 15 use
```
15 size !
```
### setting the font
Actually only consolas and Monospaced are fonts provided by Windows (jemForth hasn't been tested on Linux yet).
If you want tu use other fonts, put the .ttf file in the ```src/main/resources/fonts``` folder and adjust the name
in the ```fonts``` array in the class ```F83ReactiveConsoleWindow``` class.

The word ```fontlist``` prints the name and a number for each font.
The number an be stored using the word ```font```in an integer variable.
To set the default font use
```
0 font !
```
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

### block buffering of mass storage
jemForth features the Forth 83 block buffering system for access to a (virtual) mass storage.
The original Forth implementation devides the mass storage (hard or floppy disk) in blocks of 1024 bytes.
Each block can be loadad into a block buffer changed and saved. Forth programs may be loadad by loading
the 1024 byte blocks.

jemForth uses a binary file stored anywhere in the file system.
The location is hard wired in the ```Forth83Engine``` class when the block buffer is created.
Set path and name of a file anywhere in the file system. If the file does not exist it is created when the
block buffer is created. Make sure you have write access to the containing directory.

Currently the block buffer area is limited to 64MB. This does not limit the file size it only means
that not more than 64MB of the file may be loaded at a time. This limits the number of buffers to 64.000.

Reading and writing of files is not supported. 

