JFLAGS = -g
JC = javac
JSOURCE = -sourcepath src/
JAR = -cp lib/argparse4j-0.4.4.jar:lib/jsoup-1.8.1.jar
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $(JAR) $(JSOURCE) -d ./bin/ $<

.PHONY: binaries

CLASSES =  \
		   src/Image.java \
		   src/Loader.java \
		   src/Main.java \

all: binaries $(CLASSES:.java=.class)

binaries:
	mkdir -p ./bin/

jar: binaries $(CLASSES:.java=.class); \
	cd bin; \
	jar cmf ../Manifest.mf ../WallpaperLoader.jar *.class; \
	cd ..

clean:
	$(RM) -r ./bin/*.class
