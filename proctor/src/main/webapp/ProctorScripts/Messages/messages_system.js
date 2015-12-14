YUI.add("messages_system", function (Y) {
    //-------------------------------------------------------------------------
    // Private variables
    //-------------------------------------------------------------------------
    var _test = null;
    //-------------------------------------------------------------------------
    // Private functions
    //-------------------------------------------------------------------------    
    function _now() {
        return (new Date()).getTime();
    }

    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------    

    /* MessageTranslation.cs */
    MessageTranslation = function (message /*Message*/, clientMessage, languageCode, subject, gradeCode) {
        this._message = message;
        this._clientMessage = clientMessage;
        this._languageCode = languageCode || null;
        this._subject = subject || null;
        this._gradeCode = gradeCode || null;
    };
    MessageTranslation.prototype.getParentMessage = function () { return this._message; };
    MessageTranslation.prototype.getClientMessage = function () { return this._clientMessage; };
    MessageTranslation.prototype.getLanguage = function () { return this._languageCode; };
    MessageTranslation.prototype.getSubject = function () { return this._subject; };
    MessageTranslation.prototype.getGrade = function () { return this._gradeCode; };
    MessageTranslation.prototype.toString = function () {
        return '[' + this._message.getMessageId() + '][' + this._message.getDefaultMessage() + '][' + this.getClientMessage() + '][' + this.getLanguage() + ']'
    };

    /* Message.cs */
    Message = function (context /*MessageContext*/, messageId /*int*/, defaultMessage /*string*/) {
        this._context = context;
        this._messageId = messageId;
        this._defaultMessage = defaultMessage;
        this._translationList = []; // <MessageTranslation>
        this._translation3D = null; // [,,]
    };

    Message.prototype.getParentContext = function () { return this._context; };
    Message.prototype.getMessageId = function () { return this._messageId; };
    Message.prototype.getDefaultMessage = function () { return this._defaultMessage; };
    Message.prototype.getTranslations = function () { return this._translationList; };

    Message.prototype.addTranslation = function (clientMessage, languageCode, gradeCode, subject) {
        var translation = new MessageTranslation(this, clientMessage, languageCode, gradeCode, subject);
        this._translationList.push(translation);
        return translation;
    };

    Message.prototype.initTranslationIndex = function (translation3D) {
        this._translation3D = translation3D;
    };

    Message.prototype.setTranslationIndex = function (messageIndex /*MessageIndex*/, translationIndex /*int*/) {
        var langIdx = messageIndex.getLanguageIndex();
        var subjectIdx = messageIndex.getSubjectIndex();
        var gradeIdx = messageIndex.getGradeIndex();
        this._translation3D[langIdx][subjectIdx][gradeIdx] = translationIndex;
    };

    //message translation selection
    Message.prototype.getTranslationByIndex = function (messageIndex /*MessageIndex*/) {
        var langIdx = messageIndex.getLanguageIndex();
        var subjectIdx = messageIndex.getSubjectIndex();
        var gradeIdx = messageIndex.getGradeIndex();

        var index = this._translation3D[langIdx, subjectIdx, gradeIdx];
        if (!Y.Lang.isNumber(index)) index = 0;
        return this._translationList[index];
    };

    Message.prototype.getTranslationByLang = function (languageCode) {
        var translations = this.getTranslations();

        // look for specific language
        var translation = Y.Array.find(translations, function (translation) {
            return (translation.getLanguage() == languageCode);
        });

        // check if language was found
        if (translation != null) return translation;

        // if language was not found return default if available
        if (translation == null && translations.length > 0) return translations[0];
        return null;
    };

    Message.prototype.toString = function () { return this.getDefaultMessage(); };

    /* MessageContext.cs */
    MessageContext = function (contextName) {
        this._name = contextName;
        this._messagesLookup = new P.Util.Structs.Map(); // <string, Message>
    };

    MessageContext.prototype.getName = function () { return this._name; };

    MessageContext.prototype.addMessage = function (messageId /*int*/, defaultMessage /*string*/) {
        var message = new Message(this, messageId, defaultMessage);
        this._messagesLookup.set(defaultMessage, message);
        return message;
    };

    MessageContext.prototype.getMessages = function () {
        return this._messagesLookup.getValues();
    };

    MessageContext.prototype.getMessage = function (defaultMessage /*string*/) {
        return this._messagesLookup.get(defaultMessage);
    };

    MessageContext.prototype.toString = function () { return this.getName(); };

    /* MessageSystem.cs */

    MessageSystem = function () {
        this._messageIndexer = new MessageIndexer();
        this._messageContexts = new P.Util.Structs.Map(); // <string, MessageContext>
        //this._currentLanguage = "ENU";
    };

    // get the current language 
    // NOTE: override this function to supply your own way to get current language
    MessageSystem.prototype._getLanguage = function () { return P.TDS.getCurrentLanguage(); };
    //MessageSystem.prototype._setLanguage = function (lang) { this._currentLanguage = lang; };

    MessageSystem.prototype.getIndexer = function () { return this._messageIndexer; };

    MessageSystem.prototype.addContext = function (context /*string*/) {
        var messageContext = new MessageContext(context);
        this._messageContexts.set(context, messageContext);
        return messageContext;
    };

    MessageSystem.prototype.getContexts = function () { return this._messageContexts.getValues(); };
    MessageSystem.prototype.getContextKeys = function () { return this._messageContexts.getKeys(); };

    MessageSystem.prototype.getContext = function (context /*string*/) {
        return this._messageContexts.get(context);
    };

    MessageSystem.prototype.getTranslation = function (context, defaultMessage, languageCode, subject, gradeCode) {
        var messageContext = this.getContext(context);

        if (messageContext != null) {
            var message = messageContext.getMessage(defaultMessage);

            if (message != null) {
                /* TODO: Fix multi dim array lookup
                var messageIndex = this._messageIndexer.get(languageCode, subject, gradeCode);
                return message.getTranslationByIndex(messageIndex);
                */

                return message.getTranslationByLang(languageCode);
            }
        }

        return null;
    };

    // find a translation just by its defaultmessage
    MessageSystem.prototype.findTranslation = function (defaultMessage) {
        var translation = null;
        var language = this._getLanguage();
        var contexts = this.getContexts();

        for (var i = 0; i < contexts.length; i++) {
            var context = contexts[i];
            translation = this.getTranslation(context.getName(), defaultMessage, language, null, null);
            if (translation != null) break;
        }

        return translation;
    };


    // translation text by context 
    MessageSystem.prototype.getTextByContext = function (context, defaultMessage) {
        var language = this._getLanguage();
        var translation = this.getTranslation(context, defaultMessage, language, null, null);

        if (translation) return translation.getClientMessage();
        return defaultMessage;
    };

    MessageSystem.prototype._format = function (text, params) {
        if (params == null) return text;       
        for (var i = 0, l = params.length; i < l; ++i) {
            var reg = new RegExp("\\{" + i + "\\}", "g");
            text = text.replace(reg, params[i]);
        }

        return text;
    };

    //private function
    MessageSystem.prototype._getDisplayMessage = function (translation, params) {
        if (translation == null) return '';

        var clientMessage = translation.getClientMessage();
        var messageID = translation.getParentMessage().getMessageId();

        if (clientMessage != null && clientMessage.length > 0) {
            if (params) clientMessage = this._format(clientMessage, params);
            return clientMessage + ' [Message Code: ' + messageID + ']';
        }

        var defaultMessage = translation.getParentMessage().getDefaultMessage();
        return defaultMessage + ' [Message Code: ' + messageID + ']';
    };

    MessageSystem.prototype.getMessage = function (defaultMessage, params) {
        var translation = null;

        if (P.TDS.messages != null) {
            translation = P.TDS.messages.findTranslation(defaultMessage);
        }

        if (translation == null) return defaultMessage;
        return this._getDisplayMessage(translation, params);
    };

    //private function
    MessageSystem.prototype._getRaw = function (translation, params) {
        if (translation == null) return '';

        var clientMessage = translation.getClientMessage();
        var messageID = translation.getParentMessage().getMessageId();

        if (clientMessage != null && clientMessage.length > 0) {
            if (params) clientMessage = this._format(clientMessage, params);
            return clientMessage;
        }

        var defaultMessage = translation.getParentMessage().getDefaultMessage();
        return defaultMessage;
    };

    MessageSystem.prototype.getRaw = function (defaultMessage, params) {
        var translation = null;

        if (P.TDS.messages != null) {
            translation = P.TDS.messages.findTranslation(defaultMessage);
        }

        if (translation == null) return defaultMessage;
        return this._getRaw(translation, params);
    };


    // get simple json
    MessageSystem.prototype.getTemplateData = function () {
        var language = this._getLanguage();
        var templateData = {};

        // build lookup for each message
        Y.Array.each(this.getContexts(), function (messageContext) {
            Y.Array.each(messageContext.getMessages(), function (message) {
                var translation = message.getTranslationByLang(language);
                templateData[message.getDefaultMessage()] = translation.getClientMessage();
            });
        });

        return templateData;
    }

}, "0.1", { requires: ["tds", "messages_indexer", "util_structs", "yui-base", "array-extras", "io", "json-parse"] });

