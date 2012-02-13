rmaze = (function() {
    var rmaze = {};
    var container = null;
    var solve_button = null;
    var clear_button = null;
    var timer = null;

    rmaze.animateSolution = function() {
        if (timer) {
            clearTimeout(timer);
        }

        rmaze.clearSolution();
        var pos = 0;
        var step = function() {
            if (rmaze.solution && pos < rmaze.solution.length) {
                var elPos = rmaze.solution[pos];
                var el = container.children[elPos];
                pos++;
                    
                el.className += "path-node ";

                timer = setTimeout(step, 50);
            }
        };
        step();
    };

    rmaze.clearSolution = function() {
        if (timer) {
            clearTimeout(timer);
        }

        var cells = container.children;
        for (var i=0, l=cells.length; i<l; i++) {
            var cell = cells[i];
            cell.className = cell.className.replace("path-node ", "");
        }
    };

    rmaze.init = function() {
        container = document.getElementsByClassName("container")[0];
        solve_button = document.getElementById("solve-button");
        clear_button = document.getElementById("clear-button");

        rmaze.initEvents();
    };

    rmaze.initEvents = function() {
        solve_button.onclick = function(e) {
            rmaze.animateSolution();

            e.preventDefault();
            e.stopPropagation();
        };

        clear_button.onclick = function(e) {
            rmaze.clearSolution();

            e.preventDefault();
            e.stopPropagation();
        };
    };

    document.addEventListener("DOMContentLoaded", rmaze.init, false);

    return rmaze;
})();
