JFLAGS = -g
JAVA = java
JAVAC = javac
CP = lib/argparse4j-0.4.4.jar:lib/jsoup-1.8.1.jar:src/

.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JFLAGS) -classpath $(CP) -d build/ -sourcepath src $*.java

SOURCES = \
        src/Image.java \
        src/Loader.java \
        src/Main.java

CLASSES = \
	build/Image.class \
	build/Loader.class \
	build/Main.class

default: classes

classes: $(SOURCES:.java=.class)

clean:
	$(RM) build/*.class

jar: classes
	cd build; \
	jar cmf ../Manifest.mf ../WallpaperLoader.jar *.class; \
	cd ..

run: classes
	$(JAVA) -classpath $(CP) Main
