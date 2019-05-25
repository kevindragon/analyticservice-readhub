(ns asrh.core
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(defonce state (r/atom {:index-text "Index"}))

(defn update-query [e]
  (swap! state assoc :query (-> e .-target .-value)))

(defn get-result []
  (get @state :data []))

(defn set-index-text [text] (swap! state assoc :index-text text))

(defn get-index-text [] (get @state :index-text))

(defn- perform-get [url opts]
  (GET url (merge {:format :json
                   :response-format :json
                   :keywords? true
                   :timeout 30000}
                  opts)))

(defn perform-index []
  (set-index-text "Index......")
  (perform-get "/index" {:timeout 600000
                         :handler #(set-index-text "Index")
                         :error-handler #(set-index-text "Index")}))

(defn perform-query []
  (let [query (str/trim (get @state :query nil))]
    (if-not (empty? query)
      (perform-get "/query"
                   {:params {:query query}
                    :handler (fn [resp] (swap! state assoc :data resp))})
      (js/alert "请输入查询词"))))

;;;; ---------- UI

(defn main []
  [:div#app
   [:div#search-box
    [:input {:value (get @state :query "")
             :on-change update-query}]
    [:button.search {:on-click perform-query} "Search"]
    [:button.index {:on-click perform-index} (get-index-text)]]
   [:p.tips "单个词分词，默认使用AND布尔搜索，无位置信息，按时倒序排列，默认取前10条，稍显简陋"]
   (let [{:keys [total docs]} (get-result)]
     [:div
      (when total [:div (str "Total: " total)])
      [:ol#result
       (for [{:keys [id content updated-at]} docs]
         ^{:key id} [:li.doc
                     [:div (str "id: " id)]
                     [:div (str "updated at: " updated-at)]
                     [:div "content:"]
                     [:div content]])]])])

(defn ^:export run []
  (r/render [main] (js/document.getElementById "app")))
