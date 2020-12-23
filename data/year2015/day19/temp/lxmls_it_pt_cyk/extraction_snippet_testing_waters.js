{
    let a = [];
    let b = new Object();
    let ac = [-1,-1];
    let cc = [-1,-1];
    b.start = function(s) {
        a.push({"event":"start","arguments":{"s":s}});
    }
    b.end = function(accepted) {
        a.push({"event":"end","context_cells":cc.slice(),"active_cell":ac.slice(),"arguments":{"accepted":accepted}});
        console.log(JSON.stringify(a));
    }
    b.cell_updated = function(i, j, content) {
        a.push({"event":"cell_updated","arguments":{"i":i,"j":j,"content":content}});
    }
    b.active_cell_changed = function(i, j) {
        a.push({"event":"active_cell_changed","context_cells":cc.slice(),"active_cell":ac.slice(),"arguments":{"i":i,"j":j}});
        ac[0] = i;
        ac[1] = j;
    }
    b.attempt_match = function(i, j, k, l) {
        a.push({"event":"attempt_match","context_cells":cc.slice(),"arguments":{"i":i,"j":j,"k":k,"l":l}});
        cc[0] = i;
        cc[1] = j;
        cc[2] = k;
        cc[3] = l;
    }
    b.found_match = function(i, j, k, l) {
        a.push({"event":"found_match","active_cell":ac.slice(),"arguments":{"i":i,"j":j,"k":k,"l":l}});
    }
    cky_offline(idgram.value, idsen.value, b);
}