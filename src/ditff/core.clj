(ns ditff.core
  (:gen-class)
  (:use
    [seesaw core border make-widget mig])
  (:require
    [clj-http.client])
  (:import
    [twitter4j TwitterFactory Twitter Paging]
    (twitter4j.conf ConfigurationBuilder)))

(defn make-config
  []
  (let [config (new ConfigurationBuilder)]
    (. config build)))

(defn make-twitter
  []
  (let [config (make-config)
        factory (TwitterFactory. config)]
    (. factory getInstance)))

(def tl (.getHomeTimeline (make-twitter)))

(defn get-pictwit-url [body]
  (let [[twiturl filetype] (re-find
                             #"data-resolved-url-large.*\.(jpg|png|gif)"
                             body)]
    (re-find (re-pattern (str "http.*." filetype)) twiturl))
  )

(defn get-urls [txt]
  (re-seq #"http[/:.\w]*" txt))

(defn get-twitpic-url [body]
  (first (re-find #"http[.:/\w]*large.*\.(jpg|png|gif)" body))
  )

(defn get-image-url [url]
  (try
    (let [response (clj-http.client/get url)
          mimetype (:content-type (:headers response))]
      (cond
        (re-matches #"image.*" mimetype) url
        (re-matches #".*twitter.*" (last (:trace-redirects response))) (get-pictwit-url (:body response))
        (re-matches #".*twitpic.*" (last (:trace-redirects response))) (get-twitpic-url (:body response))
        :default nil))
    (catch Exception _ nil)))

(defn get-image-url-from-status [status]
  (let [urls (get-urls (.getText status))
        imageurls (filter #(not (nil? %)) (map get-image-url urls))]
    (if (not-empty imageurls)
      (first imageurls)
      nil)
    )
  )

(defrecord ImageStatus [status url])

(extend-type ImageStatus
  MakeWidget
  (make-widget* [imagestatus]
    (let [status (:status imagestatus)
          url (:url imagestatus)]
      (mig-panel
        :border [(line-border :thickness 1) 5]
        :items [
                 [(label :icon (.getMiniProfileImageURL (.getUser status)))]
                 [(.getName (.getUser status))]
                 [(str ":@" (.getScreenName (.getUser status)))]
                 [(.getText status) "w 320!, span, wrap"]
                 [(label :icon url) "c, span, w 320"]
                 ]))))

(defn get-image-status [status]
  (let [imageurl (get-image-url-from-status status)]
    (if (nil? imageurl)
      nil
      (ImageStatus. status imageurl))))

(defn tweet [tweet]
  (.updateStatus (make-twitter) tweet))

(def listview (scrollable (vertical-panel :items (filter #(not (nil? %)) (map get-image-status tl)))))
(def f (frame
         :title "はじめの森の奥深く"
         :content listview))

(defn -main
  [& args]
  (invoke-later
    (-> f pack! show!)))
