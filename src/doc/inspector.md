# Inspection -- Introspection #
The <tt>Inspectable</tt> interfaces defines how the the forth engine can be accessed
from a development environment like the IDE. Its main function is to provide a means
to control the execution of the engine from the outside. It enables a debug tool
to stop and resume the engine. 

The engine also provides an <tt>Inspector</tt> object. It is used to access the inner
state and implements the words <tt>SEE</tt> and <tt>WORDS</tt>.

This again is an ad hoc implementation and might profif from some refactoring.