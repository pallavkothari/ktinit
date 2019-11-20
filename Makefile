build:
	./gradlew build spotlessCheck installDist

install: build
	mkdir -p ${HOME}/bin
	ln -sf ${PWD}/build/install/ktinit/bin/ktinit ${HOME}/bin/ktinit
	@echo "******************************************************"
	@echo "please add ${HOME}/bin/ktinit to your PATH"
	@echo "******************************************************"

run:
	ktinit

spotless:
	./gradlew spotlessApply

.PHONY: build
