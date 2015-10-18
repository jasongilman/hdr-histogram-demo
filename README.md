# HDR Histogram Demo in Clojure

This is an demo project showing use of the [HDR Histogram](https://github.com/HdrHistogram/HdrHistogram) Java library created by Gil Tene. There are two parts to the demo, a simple web application and a load tester. 

## Usage

1. Open the web application and load tester in separate REPL instances.
2. Run `(reset)` in each to load the code and start the web app.
3. Go to the `perf-tester.core` namespace and look at the comment at the bottom.
4. Example tests can be run there. These will output hgrm files for what the server measured and the load balancer.
5. Open `plotFiles.html`. This is a copy of [plotFiles.html](https://github.com/HdrHistogram/HdrHistogram/blob/master/GoogleChartsExample/plotFiles.html) from the HDR Histogram project for convenience.
6. Graph the results by opening the *.hgrm files generated using the "Choose Files" button shown in `plotFiles.html`.


## License

Copyright © 2015 Jason Gilman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
