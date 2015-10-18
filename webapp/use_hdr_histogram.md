# Using HDR Histogram

Add the HDR Histogram library to your project.clj as a dependency

```clojure
[org.hdrhistogram/HdrHistogram "2.1.7"]
```

Import the histogram class. See [the Javadocs](http://hdrhistogram.github.io/HdrHistogram/JavaDoc/)

```clojure
(import org.HdrHistogram.Histogram)
```

Create a new instance of the histogram class

```clojure

(def hdr
  (Histogram.
   ;; highest trackable value. We're concerned with the ms level only
   ;; Track up to 1 hours
   (* 1 3600 1000)

   ;; Number of significant digits
   3))

```

Record the time of some task

```clojure
(dotimes [n 500]
  (let [start (System/currentTimeMillis)]

    ;; Do the task
    (Thread/sleep (inc (rand-int 50)))

    ;; Record the time taken
    (.recordValue hdr (- (System/currentTimeMillis) start))))
```


Output the percentile distribution

```clojure
(.outputPercentileDistribution hdr System/out 1.0)
```

Write it to a file
```clojure
(.outputPercentileDistribution
  hdr
  (java.io.PrintStream. "example.hgrm")
  1.0)
```

That file can be viewed in the plotFiles.html. This is a copy of from the [HDR Histogram](https://github.com/HdrHistogram/HdrHistogram) library.
