[![Actions Status](https://github.com/pallavkothari/ktinit/workflows/CI/badge.svg)](https://github.com/pallavkothari/ktinit/actions)
# KtInit

CLI to bootstrap kt projects.

### build and install
```
brew install gradle 
make install        ## installs at /usr/local/bin/ktinit
```

### run 
```
ktinit --help
mkdir ~/kotlin && cd ~/kotlin
ktinit -c --group-id demo --artifact-id kotlindemo
```


#### Resources
Inspired by:
- [oh-my-mustache](https://github.com/ekino/oh-my-mustache)
- [slides](http://slides.com/ekino-leomillon/oh-my-mustache/)
- [jwtctl](https://github.com/leomillon/jwtctl)
