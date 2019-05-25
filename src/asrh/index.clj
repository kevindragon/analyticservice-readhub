(ns asrh.index
  (:require [clojure.java.io :as io]
            [clojure.set :refer [intersection union]]
            [cheshire.core :refer [generate-string parse-string]]
            [asrh.analyzer :refer [analyze]]))

(defonce ^:private index (ref {}))

(defonce ^:private docs (ref {}))

(def ^:private index-file "index.idx")
(def ^:private docs-file "docs.idx")

(defn commit []
  (spit index-file (pr-str @index))
  (spit docs-file (pr-str @docs)))

(defn load-index []
  (when (.exists (io/file index-file))
    (dosync (ref-set index (read-string (slurp index-file)))))
  (when (.exists (io/file docs-file))
    (dosync (ref-set docs (read-string (slurp docs-file))))))

(defn- update-index [token id]
  (dosync (alter index update token #(conj (or % #{}) id))))

(defn- update-doc [id doc]
  (dosync (alter docs assoc id doc)))

(defn add [{:keys [id content] :as doc}]
  (let [tokens (filter (comp not empty?) (analyze content))]
    (doseq [token tokens]
      (update-index token id))
    (update-doc id (dissoc doc :id))))

(defn- get-doc [ids]
  (map (fn [id] (merge (get @docs id "") {:id id})) ids))

(defn query [q start row]
  (let [tokens (analyze q)
        hits (some->> (select-keys @index tokens)
                      (vals)
                      (apply intersection))]
    {:total (count hits)
     :docs (some->>
            hits
            (sort-by (fn [id] (-> (get @docs id) :updated-at (.getTime))) >)
            (drop start)
            (take row)
            (get-doc))}))
