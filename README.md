# clj-proximity-search

Implementation of [proximity search (text)](https://en.wikipedia.org/wiki/Proximity_search_(text)) algorithm with operators "N(distance)" and "W(distance)" in clojure.


## Query

The data structure for a query consists of two types: Operators and Keywords.

Sample query:

```clojure
(def query ;; (solar W1 panel) N1 roof"
  {:Query    :Op
   :operator :near
   :distance 1
   :operands [{:Query    :Op
               :operator :within
               :distance 1
               :operands [{:Query :Keyword
                           :word  "solar"}
                          {:Query :Keyword
                           :word  "panel"}]}
              {:Query :Keyword
               :word  "roof"}]})
```

## Match

The data structure for a match consists of similar types: Operators and Keywords.
Additionally, it has populated indexes for matched keywords.

```clojure
{:Match    :Op
 :distance 3
 :operands [{:Match :Keyword
             :word  "discloses"
             :index 2}
            {:Match :Keyword
             :word  "fiber"
             :index 5}]}
```

## Algorithm

1. Iterate over words' indexes. Try to match the query on every index.
2. If the query is a keyword and it's matching, return the Match struct containing the matched index.
3. If the query is an operator:
   1. If the left operand matches, try to match the right operand at indexes where the right operand could be found.
   2. If the right operand matched too, finish the computation and return the Match struct.

## Further Exploration

* Implement a parser for the queries.

## License

Copyright © 2023 Anton Žyluk

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
