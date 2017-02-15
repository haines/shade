# shade

shade is a tool for relocating namespaces within uberjars to avoid dependency clashes.

## Motivation

Test helper libraries' dependencies are liable to clash with those of the code under test.
To avoid this, the helper library can be distributed as an uberjar with its dependencies "shaded" under a different namespace.

## Installation

The easiest way to get started is to add the lein-shade plugin to your Leiningen project map:

```clojure
:plugins [[lein-shade "0.3.0"]]
```

## Usage

As is typical when building an uberjar, you'll need to use AOT compilation:

```clojure
:profiles {...
           :uberjar {:aot :all}
           ...}
```

Unlike when building an application uberjar, you probably don't want to bundle Clojure itself.
You can omit it by moving the dependency to the `:provided` profile:

```clojure
:profiles {...
           :provided {:dependencies [[org.clojure/clojure "1.8.0"]
                                     ...]}
           ...}
```

You'll want to omit dependencies that are bundled in the uberjar from the POM, otherwise consumers of your library will download them as transitive dependencies.
To do so, move your dependencies to the `:shaded` profile, and add `:shaded` to your `:default` profile:

```clojure
:profiles {...
           :shaded {:dependencies [...]}
           :default [:leiningen/default :shaded]
           ...}
```

If you run `lein shade-jar` now, you'll get an uberjar under `target/shaded`.
However, the namespaces within your dependencies won't have been relocated, so version clashes will still be problematic.
To relocate those namespaces, you need to specify them in your project map:

```clojure
:shade {:namespaces [foo.bar
                     ...]}
```

Now when you run `lein shade-jar`, those namespaces will have been hidden under the `your-project.shaded` namespace in the uberjar (so `foo.bar` is now `your-project.shaded.foo.bar`).

You can customize where shaded dependencies are hidden if you want:

```clojure
:shade {:namespaces [...]
        :under my-project.hidden}
```

### Deploying shaded uberjars

You can deploy your shaded uberjar using `lein deploy-shaded-jar [repository]`.
You might want to alias the `deploy` task:

```clojure
:aliases {...
          "deploy" ["deploy-shaded-jar" "clojars"]
          ...}
```

You can also install your shaded uberjar to your local repository using `lein install-shaded-jar`.
Similarly, you might want to alias the `install` task:

```clojure
:aliases {...
          "install" ["install-shaded-jar"]
          ...}
```

## License

Copyright © 2017 Red Badger

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
