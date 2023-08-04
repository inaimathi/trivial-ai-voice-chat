(ns trivial-ai-voice-chat.sound
  (:require [clojure.string :as str]
            [cheshire.core :as json]
            [trivial-openai.core :as ai]
            [clojure.java.shell :as shell]))

(defn -command-exists? [command]
  (= 0 (:exit (shell/sh "which" command))))

(defn -voice-library []
  (cond (-command-exists? "say") :say
        (-command-exists? "espeak") :espeak
        :else (throw (Exception. "You must install one of `say` or `espeak` and expose it on your PATH for this library to work"))))

(def DEFAULT-SPEAK-LIB (-voice-library))

(def SEARCH-PATH
  (sort-by
   count
   (conj
    (map
     #(str % "/")
     (-> (System/getenv "PATH")
         (str/split #":")
         set
         (conj "/opt/homebrew/bin"
               ".")
         )) "")))

(defn -find-command [name]
  (->> SEARCH-PATH
       (map #(str % name))
       (filter -command-exists?)
       first))

(when (not (-find-command "rec"))
  (throw (Exception. "You must install the `rec` command. You can do this by installing the `sox` package, available in `guix`, `brew` or `apt`")))

(defn => [command args]
  (apply shell/sh (-find-command command) args))

(defn list-voices [& {:keys [lib] :or {lib DEFAULT-SPEAK-LIB}}]
  (let [spk (-find-command (name lib))]
    (vec
     (case lib
       :say (->> (shell/sh spk "--voice=?")
                 :out str/split-lines
                 (map #(first (str/split % #"\W+"))))
       :espeak (->> (shell/sh spk "--voice")
                    :out str/split-lines rest
                    (map #(str/split % #"\W+"))
                    (map #(if (= (get % 4) "M") (get % 5) (get % 4)))
                    set (sort-by str/lower-case) (remove #(>= 2 (count %))))))))

(defn speak [text & {:keys [file voice lib] :or {lib DEFAULT-SPEAK-LIB}}]
  (let [spk (-find-command (name lib))
        cmd (if voice [spk "-v" voice] [spk])]
    (apply shell/sh (conj cmd text))))

(defn speak-to-mp3 [text & {:keys [out dir voice lib] :or {lib DEFAULT-SPEAK-LIB}}]
  (let [spk (-find-command (name lib))
        cmd (atom [spk])
        dir (new java.io.File (or dir "."))]
    (when voice
      (swap! cmd #(vec (concat % ["-v" voice]))))
    (let [tmp (java.io.File/createTempFile
               "spoken"
               (case lib
                 :say ".aiff"
                 ".wav")
               dir)
          out (or out (.getPath (java.io.File/createTempFile "spoken" ".mp3" dir)))]
      (case lib
        :say (swap! cmd #(vec (concat % ["-o" (.getPath tmp)])))
        :espeak (swap! cmd #(vec (concat % ["-w" (.getPath tmp)]))))

      (=> "lame" ["-m" "m" (.getPath tmp) out])
      (.delete tmp)
      out)))

(defn play [file & {:keys [speed] :or {speed 1.5}}]
  (=> "mplayer" [file "-speed" (str speed)])
  nil)

(defn record-until-silence [& {:keys [out dir silence-size] :or {silence-size 1.0}}]
  (let [file (or out (.getPath (java.io.File/createTempFile "recorded" ".wav" (new java.io.File (or dir ".")))))
        res (=>
             "rec" [file
                  "vad" ;; trim silence from the beginning of voice detection
                  "silence" "1" ".05" "1.3%" ;; wait until we hear activity above the threshold for more than 1/20th of a second
                  "1" (str silence-size) "3.0%" ;; stop recording when audible activity falls to zero for silence-size seconds
                  "gain" "-n"])] ;; normalize the gain
    (if (= 0 (:exit res)) file)))
