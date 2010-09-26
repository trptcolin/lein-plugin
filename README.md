# lein-plugin

Plugin for [Leiningen](http://github.com/technomancy/leiningen) to manage other
Leiningen plugins.

## Installation

    % git clone git://github.com/trptcolin/lein-plugin.git
    % cd lein-plugin
    % lein plugin install lein-plugin "0.1.0"

## Usage

Once you're installed, you can call the plugin task from anywhere.

    % lein plugin help

Use the same arguments you would put in the Leiningen :dev-dependencies if you
were only using the plugin on a single project.

    % lein plugin install lein-clojars/lein-clojars "0.6.0"

You can also use the simplified version where the group and artifact id are the
same, and even leave out the parentheses if you want.

    % lein plugin install lein-clojars 0.6.0

You can also easily uninstall plugins through the task.

    % lein plugin uninstall lein-clojars 0.6.0


## License

Copyright (C) 2010 Colin Jones

Distributed under the Eclipse Public License, the same as Clojure.
