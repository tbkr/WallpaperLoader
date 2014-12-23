JFLAGS = -g
JAVA = java
JAVAC = javac
CP = lib/argparse4j-0.4.4.jar:lib/jsoup-1.8.1.jar:.

.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JFLAGS) -classpath $(CP) $*.java

SOURCES = \
        Image.java \
        Loader.java \
        Main.java

default: classes

classes: $(SOURCES:.java=.class)

clean:
	$(RM) *.class

jar: classes
	jar cmf Manifest.mf WallpaperLoader.jar *.class

run: classes
	$(JAVA) -classpath $(CP) Main
