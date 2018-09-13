build:
	./gradlew build install

install: build
	ln -sf ${PWD}/build/install/ktinit/bin/ktinit /usr/local/bin/ktinit

run:
	ktinit

.PHONY: build
