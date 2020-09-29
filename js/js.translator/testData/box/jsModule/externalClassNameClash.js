define("a", [], function() {

    function A() {}

    A.prototype.foo = function () {
        return "O";
    };

    return { A: A };
});

define("b", [], function() {

    function A() {}

    A.prototype.foo = function () {
        return "K";
    };

    return { A: A };
});