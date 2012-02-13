rmaze = (function() {
    var rmaze = {};
    var solve_button = null;
    var clear_button = null;
    var timer = null;

    rmaze.container = null;

    rmaze.fetchMaze = function() {
        $.ajax({
            url: '/maze?nc=' + (new Date()).getTime(),
            type: 'GET',
            dataType: 'html',
            success: function(data) {
                $("#content").html(data);
                rmaze.container = $(".container");
                rmaze.animateSolution();
            }
        });
    };

    rmaze.animateSolution = function() {
        if (timer) {
            clearTimeout(timer);
        }

        rmaze.clearSolution();
        var pos = 0;
        var step = function() {
            if (rmaze.solution) {
                if (pos < rmaze.solution.length) {
                    var elPos = rmaze.solution[pos];
                    var el = $(rmaze.container[0].children[elPos]);
                    pos++;
                    
                    el.addClass("path-node");

                    timer = setTimeout(step, 50);
                } else {
                    rmaze.animateSolutionFinished();
                }
            }
        };
        step();
    };

    rmaze.animateSolutionFinished = function() {
        rmaze.fetchMaze();
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
    };

    $(document).ready(rmaze.init);

    return rmaze;
})();
