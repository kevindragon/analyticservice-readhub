(ns asrh.fetch
  (:require [clojure.instant :refer [read-instant-date]]
            [clj-http.client :as client])
  (:import org.jsoup.Jsoup))

(def ^:const api-topic "https://api.readhub.cn/topic")

(defn- get-page [last-cursor]
  (-> (str api-topic "?pageSize=20&lastCursor=" last-cursor)
      (#(do (println "fetch " %) %))
      (client/get {:as :json})
      (:body)))

(defn- topic-n [n last-cursor]
  (when (> n 0)
    (let [data (-> last-cursor get-page :data)
          last-cursor (-> data last :order)]
      (lazy-cat (take n data)
                (topic-n (- n (count data)) last-cursor)))))

(defn- get-doc-prop [doc]
  (let [id (get doc :id nil)
        updated-at (get doc :updatedAt nil)
        has-instant-view (get doc :hasInstantView false)
        instantview (fn [id]
                      (-> (str api-topic "/instantview?topicId=" id)
                          (client/get {:as :json,
                                       :throw-exceptions false})
                          ((comp :content :body))
                          (or "")
                          (Jsoup/parse)
                          (.text)))]
    {:id id
     :updated-at (read-instant-date updated-at)
     :content (if has-instant-view
                (instantview id)
                (get doc :summary ""))}))

(defn fetch-top-n-topic [n]
  (->> (topic-n n "")
       (map get-doc-prop)))
