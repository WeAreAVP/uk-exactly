Exactly 0.1.6
====

MacOS Build
=====
- install/clone `jar2app` from [https://github.com/Jorl17/jar2app](https://github.com/Jorl17/jar2app)
- Run build [or press F11], it will generate `exactly.jar` in `uk-exactly/target/`
- Then run the following command (remember to updated the version numbers)
>jar2app uk-exactly/target/exactly.jar -n "Exactly" -d "com.avp.exactly" -i "./uk-exactly/exactly-big.icns" -v "0.1.6" -s "0.1" -o ../Exactly.app


Windows
=====

- Install launch4j from here: https://sourceforge.net/projects/launch4j/files/launch4j-3/3.11/ `version 3.11`
- Open uk-exactly in NetBeans
- From `Projects` view right click and select build
- Once the package is complete open launch4j windows application and select 'exactly.jar' from `uk-exactly/target` to convert to exe.
- launch4j configurations file is included [launch4j-exactly-build.xml](https://github.com/WeAreAVP/uk-exactly/) Update the version number if required.
