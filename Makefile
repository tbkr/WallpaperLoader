JFLAGS = -g
JAVA = java
JAVAC = javac
CP = lib/argparse4j-0.4.4.jar:lib/jsoup-1.8.1.jar:.

.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JFLAGS) -classpath $(CP) $*.java

CLASSES = \
        Image.java \
        Loader.java \
        Main.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

run: classes
	$(JAVA) -classpath $(CP) Main
