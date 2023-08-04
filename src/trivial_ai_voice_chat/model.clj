(ns trivial-ai-voice-chat.model
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]

            [trivial-openai.core :as ai])
  (:import [java.io PushbackReader]))

(defn read-all
  [file]
  (let [rdr (-> file io/file io/reader PushbackReader.)]
    (loop [forms []]
      (let [form (try (edn/read rdr) (catch Exception e nil))]
        (if form
          (recur (conj forms form))
          forms)))))

(defn mk-chat [& {:keys [dir] :or {dir "."}}]
  (let [log (str dir "/chat.edn")]
    (io/make-parents log)
    {:history (atom (if (.exists (io/file log)) (read-all log) []))
     :dir dir}))

(defn chat! [chat msg]
  (let [f (str (:dir chat) "/chat.edn")]
    (swap! (:history chat) #(conj % msg))
    (spit f msg :append true)
    (spit f "\n" :append true)
    (let [resp (ai/chat (map (fn [m] (select-keys m [:role :content])) @(:history chat)))
          txt (get-in resp ["choices" 0 "message" "content"])]
      (when txt
        (let [resp-msg {:role :assistant :content txt}]
          (spit f resp-msg :append true)
          (spit f "\n" :append true)
          resp-msg)))))
