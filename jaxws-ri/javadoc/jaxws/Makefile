#
# Builds all image files.
#
#   Currently this only runs for Kohsuke
#
images = $(patsubst %.seq,%.png,$(wildcard *.seq))

all : $(images)

clean :
	rm *.png

%.png: %.seq
	rm -f tmp
	ssh kk122374@skinny.sfbay "/home/kk122374/bin/sequence-diagram/generate.sh" < $< > tmp
	cp tmp $@
	rm tmp

diagrams.zip : $(images)
	zip diagrams.zip *.seq *.png
