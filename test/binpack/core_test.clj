(ns binpack.core-test
  (:use [midje.sweet])
  (:require [binpack.core :as k]))

(fact "verification on size of containers"
      (k/exceeded? [[3 2 3] [3 7]] [10 10]) => falsey
      (k/exceeded? [[3 2 3] [3 5]] [10 1]) => truthy)

(facts "conditional allocation"
       (fact "room to allocate"
             (k/add-first 3 [[1 2 3] [1 1]] [7 6]) => [[1 2 3] [1 1 3]])
       (fact "should not allocate twice"
             (k/add-first 3 [[1 3] [1 1]] [7 6]) => [[1 3 3] [1 1]]))

(facts "allocation strategies"
       (fact "unconstrained allocation"
             (k/free-alloc [1 2 3] [[] [] []] nil) => [[1] [2] [3]]
             (k/free-alloc [1 2] [[] [] []] nil) => [[1] [2] []]
             (k/free-alloc [1 2] [[4] [6]] nil) => [[4 1] [6 2]]
             (k/free-alloc [1 2 3] [[] []] nil) => [[1] [2]])
       (fact "container size restricted allocation"
             (fact "all full, nothing to do"
                   (k/size-alloc [1 2 3] [[5] [5] [5]] [5 5 5]) => [[5] [5] [5]])
             (fact "there is room somewhere"
                   (k/size-alloc [5 5 1] [[1] [5] [5]] [5 5 5]) => [[1 1] [5] [5]])))

(facts "unconstrained packing of items in containers"
       (fact "even distribution"
             (k/pack [2 2 1 1] [0 0] k/free-alloc) => [[1 2] [1 2]])
       (fact "single container"
             (k/pack (reverse (range 10)) [0] k/free-alloc) => [(range 10)])
       (fact "asymmetric packing"
             (k/pack (reverse (sort [2 3 1 1 4 2 1])) [0 0 0] k/free-alloc) => [[1 2 4] [1 3] [1 2]])
       (fact "asymmetric packing"
             (k/pack (reverse (range 10)) [0 0 0] k/free-alloc) => [[0 3 6 9] [2 5 8] [1 4 7]]))

(facts "pack items without exceeding container sizes"
       (fact "no space whatsoever"
             (k/pack [2 2 1 1] [0 0] k/size-alloc) => [[] []])
       (fact "single container"
             (k/pack (range 1 10) [10] k/size-alloc) => [[4 3 2 1]])
       (fact "asymmetric packing"
             (k/pack [10 20 30 40 50 60 70] [100 20 80] k/size-alloc) => [[50 40 10] [20] [30]])
       (fact "sessions and time slots"
             (k/pack 
               (reverse (sort [60 45 30 45 45 5 60 45 30 30 45 60 60 45 30 30 60 30 30])) 
               [180 180 240 240] k/size-alloc) => 
             [[5 45 60 60] [30 45 45 60] [30 30 30 45 45 60] [30 30 30 45 60]]))