YUI.add("messages_indexer", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _sample = null;
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _now() {
        return (new Date()).getTime();
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------    
    /* MessageIndex.cs */
    MessageIndex = function (langIdx /*int*/, subjectIdx /*int*/, gradeIdx /*int*/) {
        this._langIdx = langIdx;
        this._subjectIdx = subjectIdx;
        this._gradeIdx = gradeIdx;
    };

    MessageIndex.prototype.getLanguageIndex = function () { return this._langIdx; };
    MessageIndex.prototype.getSubjectIndex = function () { return this._subjectIdx; };
    MessageIndex.prototype.getGradeIndex = function () { return this._gradeIdx; };



    /* MessageIndexer.cs */

    MessageIndexer = function () {
        this._languageDictionary = new P.Util.Structs.Map(); // <string, int>
        this._subjectDictionary = new P.Util.Structs.Map(); // <string, int>
        this._gradeDictionary = new P.Util.Structs.Map(); // <string, int>

        this.init();
    };

    MessageIndexer.prototype.init = function () {
        this._languageDictionary.clear();
        this._subjectDictionary.clear();
        this._gradeDictionary.clear();

        this._languageDictionary.set('ENU', 0);
        this._subjectDictionary.set('DefaultSubject', 0);
        this._gradeDictionary.set('DefaultGrade', 0);
    };

    MessageIndexer.prototype.addLanguage = function (language) {
        if (!Y.Lang.isString(language)) return 0;

        // check if language
        if (this._languageDictionary.containsKey(language)) {
            return this._languageDictionary.get(language);
        }

        this._languageDictionary.set(language, this._languageDictionary.getCount());
        return this._languageDictionary.getCount() - 1;
    };

    MessageIndexer.prototype.addSubject = function (subject) {
        if (!Y.Lang.isString(subject)) return 0;

        // check if subject
        if (this._subjectDictionary.containsKey(subject)) {
            return this._subjectDictionary.get(subject);
        }

        this._subjectDictionary.set(subject, this._subjectDictionary.getCount());
        return this._subjectDictionary.getCount() - 1;
    };

    MessageIndexer.prototype.addGrade = function (grade) {
        if (!Y.Lang.isString(grade)) return 0;

        // check if grade
        if (this._gradeDictionary.containsKey(grade)) {
            return this._gradeDictionary.get(grade);
        }

        this._gradeDictionary.set(grade, this._gradeDictionary.getCount());
        return this._gradeDictionary.getCount() - 1;
    };

    MessageIndexer.prototype.createIndexer3D = function () {
        var i1 = (this._languageDictionary.getCount() > 0) ? this._languageDictionary.getCount() : 1;
        var i2 = (this._subjectDictionary.getCount() > 0) ? this._subjectDictionary.getCount() : 1;
        var i3 = (this._gradeDictionary.getCount() > 0) ? this._gradeDictionary.getCount() : 1;
        return P.Util.Array.createMultiDimArray(i1, i2, i3);
    };

    MessageIndexer.prototype.getLanguageIndex = function (language) {
        if (!Y.Lang.isString(language) || this._languageDictionary.getCount() == 0) return 0;

        if (this._languageDictionary.containsKey(language)) {
            return this._languageDictionary.get(language);
        }

        return 0;
    };

    MessageIndexer.prototype.getSubjectIndex = function (subject) {
        if (!Y.Lang.isString(subject) || this._subjectDictionary.getCount() == 0) return 0;

        if (this._subjectDictionary.containsKey(subject)) {
            return this._subjectDictionary.get(subject);
        }

        return 0;
    };

    MessageIndexer.prototype.getGradeIndex = function (grade) {
        if (!Y.Lang.isString(grade) || this._gradeDictionary.getCount() == 0) return 0;

        if (this._gradeDictionary.containsKey(grade)) {
            return this._gradeDictionary.get(grade);
        }

        return 0;
    };

    MessageIndexer.prototype.get = function (language, subject, grade) {
        var langIdx = this.getLanguageIndex(language);
        var subjectIdx = this.getSubjectIndex(subject);
        var gradeIdx = this.getGradeIndex(grade);
        return new MessageIndex(langIdx, subjectIdx, gradeIdx);
    };

}, "0.1", { requires: ["util_structs", "io", "json-parse"] });

