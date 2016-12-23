/**
 * Created by lee on 11/11/16.
 */
$(document).ready(function() {

    $('.container').mouseenter(function () {         //hover animation home button
        $('#body-img').removeClass('transparent');
        $('#logo2').removeClass('transparent');
    });

    $('.container').mouseleave(function () {
        $('#body-img').addClass('transparent');
        $('#logo2').addClass('transparent');
    });
});