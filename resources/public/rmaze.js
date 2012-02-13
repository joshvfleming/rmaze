rmaze = (function() {
    var rmaze = {};
    var solve_button = null;
    var clear_button = null;
    var timer = null;

    rmaze.container = null;
    rmaze.paused = false;

    rmaze.fetchMaze = function() {
        $.ajax({
            url: '/maze?nc=' + (new Date()).getTime(),
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                $("#content").html('');
                rmaze.drawMaze(data.maze);
                rmaze.solution = data.solution;
                rmaze.container = $(".container");
                rmaze.animateSolution();
            }
        });
    };

    rmaze.drawMaze = function(data) {
        var container = $("<div class='container'></div");
        $("#content").append(container);

        for (var i=0, l=data.length; i<l; i++) {
            var cellData = data[i];
            var cell = $("<div class='cell'>&nbsp;</div>");
            container.append(cell);

            for (var j=0, p=cellData.length; j<p; j++) {
                var dir = cellData[j];
                cell.addClass("open-" + dir);
            }
        }
    };

    rmaze.animateSolution = function() {
        if (timer) {
            clearTimeout(timer);
        }

        rmaze.clearSolution();
        var pos = 0;
        var step = function() {
            if (!rmaze.solution || rmaze.paused) {
                timer = setTimeout(step, 50);
                return;
            }

            if (pos < rmaze.solution.length) {
                var elPos = rmaze.solution[pos];
                var el = $(rmaze.container[0].children[elPos]);
                pos++;
                
                el.addClass("path-node");

                timer = setTimeout(step, 50);
            } else {
                rmaze.animateSolutionFinished();
            }
        };
        step();
    };

    rmaze.animateSolutionFinished = function() {
        rmaze.fetchMaze();
    };

    rmaze.keydown = function(e) {
        if (e.keyCode === 32 || e.keyCode === 80 || e.keyCode === 83) {
            rmaze.paused = !rmaze.paused;
        }
    };

    rmaze.clearSolution = function() {
        if (timer) {
            clearTimeout(timer);
        }

        rmaze.container.find(".path-node").removeClass(".path-node");
    };

    rmaze.init = function() {
        rmaze.initEvents();
        rmaze.fetchMaze();
    };

    rmaze.initEvents = function() {
        $(document).on("keydown", function(e) { rmaze.keydown(e) });
    };

    $(document).ready(rmaze.init);

    return rmaze;
})();
