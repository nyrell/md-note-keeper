# MD Note Keeper

*MD Note Keeper* is an notebook application that supports formatting using markdown syntax. All notes are automatically stored in a database and there is no need to manually save the notes.

Github page: <https://github.com/nyrell/md-note-keeper/>


## Some features from the user perspective

* Formatting using Markdown: Easy to read and easy to copy & paste into other applications.
* The notes are automatically saved to a database.
* Cross platform due to Clojure/Java.


## Some features from the developer perspective

*MD Note Keeper* is written in [Clojure](https://clojure.org/), as an exercise to learn Clojure better. The aim was to create a simple application that were still useful and reasonably "complete". It is released under the MIT License, so hopefully it might be useful for others who want to learn clojure too.

Some features/techniques used:
* Java Swing GUI created using [Seesaw](https://github.com/daveray/seesaw).
* Sqlite3 access using [clojure.java.jdbc](https://github.com/clojure/java.jdbc).
* Markdown support by use of [markdown-clj](https://github.com/yogthos/markdown-clj).
* Customized logging using [Timbre](https://github.com/ptaoussanis/timbre).
* Setting environment variables through project.clj using [Environ](https://github.com/weavejester/environ).
* Use of static code analysis tool (code style) [Kibit](https://github.com/jonase/kibit)
* Use of code analysis tool (LINT) [Eastwood](https://github.com/jonase/eastwood)

Some Java/Swing specific features explored:
* Java preferences (java.util.prefs.Preferences)
* Undo/Redo using undomanager (javax.swing.undo.UndoManager)
* HTML/CSS display using JEditorPane
* Font handling


## TODO: 

### Possible future improvements
* Allow change of db via File->Open
* Allow hiding of raw-html view
* Allow reordering of the notes in the list view using drag-and-drop.
* Testing


## Installation from source

Clone the repository or download the source. Then run with:

    $ lein run
    
Or build a jar file and run that:

    $ lein uberjar
    java -jar md-note-keeper-x.x.x-standalone.jar

## Installation from release

Download the zip file from the [release](https://github.com/nyrell/md-note-keeper/releases) and unpack in a directory of your choice. Move to the directory and start the program with:

    $ java -jar md-note-keeper-x.x.x.jar

## Options

It is possible to change the appearance of the rendered note by adding a css file. *MD Note Keeper* will search for a css file with the same name as the database file but with the extension ".css". If it is found it will be used and otherwise a default css file will be loaded.

Note that the css support of JEditorPane (that is used to display the HTML) is very rudimentary. See resources/default.css (also visible in the "raw HTML" view) for some examples of what is possible.

## License

Copyright © 2019 Mattias Nyrell

MIT License
 
