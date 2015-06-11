/*
	Transit by TEMPLATED
	templated.co @templatedco
	Released for free under the Creative Commons Attribution 3.0 license (templated.co/license)
 */

(function($) {

    skel.init({
        reset : 'full',
        breakpoints : {
            global : {
                href : 'css/style.css',
                containers : 1400,
                grid : {
                    gutters : [ '2em', 0 ]
                }
            },
            xlarge : {
                media : '(max-width: 1680px)',
                href : 'css/style-xlarge.css',
                containers : 1200
            },
            large : {
                media : '(max-width: 1280px)',
                href : 'css/style-large.css',
                containers : 960,
                grid : {
                    gutters : [ '1.5em', 0 ]
                },
                viewport : {
                    scalable : false
                }
            },
            medium : {
                media : '(max-width: 980px)',
                href : 'css/style-medium.css',
                containers : '90%!'
            },
            small : {
                media : '(max-width: 736px)',
                href : 'css/style-small.css',
                containers : '90%!',
                grid : {
                    gutters : [ '1.25em', 0 ]
                }
            },
            xsmall : {
                media : '(max-width: 480px)',
                href : 'css/style-xsmall.css'
            }
        },
        plugins : {
            layers : {
                config : {
                    mode : 'transform'
                },
                navButton : {
                    breakpoints : 'medium',
                    height : '4em',
                    html : '<span class="toggle" data-action="toggleLayer" data-args="navPanel"></span>',
                    position : 'top-left',
                    side : 'top',
                    width : '6em'
                },
                navPanel : {
                    animation : 'overlayX',
                    breakpoints : 'medium',
                    clickToHide : true,
                    height : '100%',
                    hidden : true,
                    html : '<div data-action="moveElement" data-args="nav"></div>',
                    orientation : 'vertical',
                    position : 'top-left',
                    side : 'left',
                    width : 250
                }
            }
        }
    });

    $(function() {

        var $window = $(window), $body = $('body');

        // Disable animations/transitions until the page has loaded.
        $body.addClass('is-loading');

        $window.on('load', function() {
            $body.removeClass('is-loading');
        });

        $("#btnSearch").click(function() {
            $("#btnSearch").hide();
            $("#spinner").show();

            setTimeout(function() {
                var results = $("#results");

                var encodedQuery = encodeURIComponent($("#query").val());

                var baseUrl = location.href.substr(0, location.href.lastIndexOf("/"));

                $.get(baseUrl + "/rest/search/" + encodedQuery, function(response) {

                    if (response == null || response.searchResults == null) {
                        return;
                    }

                    var searchResults = response.searchResults;

                    var index = 0;

                    var currentResults = results.find(".search-result");

                    if (currentResults.size() < 1) {
                        addSearchResult(results, searchResults);
                    }
                    else {
                        results.find(".search-result").slideUp(400, function() {
                            results.find(".search-result").remove();
                            addSearchResult(results, searchResults);
                        });
                    }

                }).fail(function(e) {
                    alert("error");
                })

            }, 500);

        });

    });

})(jQuery);

function addSearchResult(results, searchResults) {
    for (var int = 0; int < searchResults.length; int++) {
        var searchResult = $("#resultTemplate .search-result").clone(true, true);
        updateSearchResult(searchResult, searchResults[int]);
        results.append(searchResult);
    }
    $("#btnSearch").show();
    $("#spinner").hide();
}

function updateSearchResult(searchResult, searchResultObj) {
    searchResult.hide();
    searchResult.find(".search-text").html(searchResultObj.content.substr(0, 500));
    searchResult.find(".search-title").html(searchResultObj.title);
    searchResult.find(".search-html").html(encodeURI(searchResultObj.html));
    searchResult.fadeIn(400);
}

function displayContent(current) {
    var win = window.open("", "Title", "toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=780, height=600, top="
            + (screen.height - 400) + ", left=" + (screen.width - 840));
    var html = $(current).parents(".search-result").find(".search-html").html();

    win.document.body.innerHTML = decodeURI(html);
}