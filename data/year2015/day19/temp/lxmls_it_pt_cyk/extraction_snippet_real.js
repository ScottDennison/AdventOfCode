{
    let a = [];
    let b = new Object();
    let cc = [-1,-1];
    b.start = function(s) {
        a = [];
        cc = [-1,-1];
    }
    b.end = function(accepted) {
        console.log(JSON.stringify(a));
    }
    b.cell_updated = function(i, j, content) {
        a.push({"rule_matched":content,"coordinates":{"changed_cell":{"i":i,"j":j},"context_cells":[{"i":cc[0],"j":cc[1]},{"i":cc[2],"j":cc[3]}]}})
    }
    b.active_cell_changed = function(i, j){}
    b.attempt_match = function(i, j, k, l) {}
    b.found_match = function(i, j, k, l) {
        cc[0] = i;
        cc[1] = j;
        cc[2] = k;
        cc[3] = l;
    }
    cky_offline(idgram.value, idsen.value, b);
}