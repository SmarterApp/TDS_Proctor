// collection of accommodations
function Accommodations() {
    this._context = new P.Util.Structs.Map();
    this._globalContextKey = "TAGlobal";
    this._languageKey = "Language";
};

Accommodations.prototype.load = function (json) {
    if (json == undefined) return;
    var contextLen = json.length;
    for (var i = 0; i < contextLen; i++) {
        var types = Accommodations.Types.create(json[i].Value);
        this._context.set(json[i].Key, types);
    }
};

Accommodations.prototype.getGlobalLanguageAccs = function () {
    var accs = this._context.get(this._globalContextKey);
    if (accs == undefined) return null;
    return accs.getType(this._languageKey);
};

Accommodations.Types = function () {
    this._typeLookup = new P.Util.Structs.Map();
};

Accommodations.Types.prototype.getType = function (type) {
    return this._typeLookup.get(type);
};

Accommodations.Types.create = function (json) {
    var types = new Accommodations.Types();
    types.load(json);
    return types;
};

Accommodations.Types.prototype.load = function (types) {
    if (types == undefined) return;
    var typesLen = types.length;
    for (var i = 0; i < typesLen; i++) {
        this._typeLookup.set(types[i].Key, types[i].Value)
    }
};







