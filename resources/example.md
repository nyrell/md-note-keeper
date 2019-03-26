# Markdown examples

The Markdown library used in this application is markdown-clj. Please visit the github page for that project for more complete information about the supported markdown syntax.

<https://github.com/yogthos/markdown-clj>

Some examples follow below.

## Text formats:
* Emphasis: *Emphasis*
* Italics: _Italics_
* Strong: **Strong**
* Bold: __Bold__
* Bold-Italic: ***Bold-Italic***

## Lists

###Unordered list

* Item 1
  * Item 1a
  * Item 1b
* Item 2
* Item 3

### Ordered list

1. Item
2. Item
3. Item

## Block quote

> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Or with linebreaks:
> Lorem ipsum dolor 
sit amet, consectetur 
adipisicing elit, sed do 
eiusmod tempor incididunt 

## Links

Simple link:
<http://github.com>

Named link:
[github](http://github.com)

## Tables

| First Header  | Second Header |
| ------------- | ------------- |
| Content Cell  | Content Cell  |
| Content Cell  | Content Cell  |

## Code
```clojure

(defn hello-world []
  (println \"Hello world!\"))

```

# Heading H1
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

## Heading H2
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

### Heading H3
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

#### Heading H4
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

##### Heading H5
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

###### Heading H6
Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
