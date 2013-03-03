JFILES=board.java chess.java eval.java gui.java move.java search.java
CFILES=board.class chess.class eval.class gui.class move.class search.class

chess: ${JFILES} ${CFILES}
	

board.class:	$*.java
	javac -O $*.java

.class:	$*.java
	javac -O $*.java

clean:
	rm *.class
