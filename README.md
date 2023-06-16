# text-matcher

This challenge is about determining if a text matches a given query.


## Query

The data structure for a query consists of two types of query: Operators and Words.

Sample query:

```clojure
(def query ;; (solar W1 panel) W1 roof"
  {:Query    :Op
   :distance 1
   :operands [{:Query    :Op
               :distance 1
               :operands [{:Query :Word
                           :word  "solar"}
                          {:Query :Word
                           :word  "panel"}]}
              {:Query :Word
               :word  "roof"}]})
```

## Match

The data structure for a match consists of similar types: Operators and Words.
Additionally, it has populated indexes for matched words.

```clojure
{:Match    :Op
 :distance 3
 :operands [{:Match :Word
             :word  "discloses"
             :index 2}
            {:Match :Word
             :word  "fiber"
             :index 5}]}
```

## Algorithm

1. Transform the text into a vector of words.
2. Starting with index 0, attempt to match the top form of the query to the given index.
3. If the query is a word, return its index.
4. If the query is an operator, first match the left operand.
5. If the left operand is matched, use the match's index to compute the list of right indexes to test.
6. If any of the indexes match with the right operand, return match with operands' matches.

## Further Exploration

* Implement a parser for the queries.
* Optimisation: 
  1. Implement an index of words based on proximity.
  2. Match query-tree depth first.
  3. Execute same-depth sub-queries concurrently.



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
