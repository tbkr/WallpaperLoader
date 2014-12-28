JFLAGS = -g
JC = javac
JSOURCE = -sourcepath src/
JAR= -cp src/lib/argparse4j-0.4.4.jar:src/lib/jsoup-1.8.1.jar
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $(JAR) $(JSOURCE) -d bin/ $<

CLASSES =  \
		   src/Image.java \
		   src/Loader.java \
		   src/Main.java \


default:$(CLASSES:.java=.class)

clean:
	$(RM) bin/*.class
