YUI.add("messages_loader", function (Y) {
    //-------------------------------------------------------------------------
    // Public interface
    //-------------------------------------------------------------------------    
    /* MessageLoader.cs */

    MessageLoader = function () {
        this._messageSystem = new MessageSystem();
    };

    MessageLoader.prototype.getMessageSystem = function () { return this._messageSystem; };

    MessageLoader.prototype.load = function (json) {
        // loop through each message context
        Y.Array.each(json.c_a, this.loadContext, this);
    };

    MessageLoader.prototype.loadContext = function (jsonContext) {
        if (jsonContext == null) return;

        var messageContext = this._messageSystem.getContext(jsonContext.c);

        if (messageContext == null) {
            messageContext = this._messageSystem.addContext(jsonContext.c);
        }

        Y.Array.each(jsonContext.m_a, function (jsonMessage) {
            var message = messageContext.getMessage(jsonMessage.m);

            if (message == null) {
                message = messageContext.addMessage(jsonMessage.id, jsonMessage.m);
            }

            Y.Array.each(jsonMessage.t_a, function (jsonTranslation) {
                // clientMessage, languageCode, subject, gradeCode
                message.addTranslation(jsonTranslation.t, jsonTranslation.l, jsonTranslation.s, jsonTranslation.g);
            });

        });
    };

    MessageLoader.prototype.buildIndex = function () {
        var messageIndexer = this._messageSystem.getIndexer();

        // build main index lookup
        Y.Array.each(this._messageSystem.getContexts(), function (messageContext) {
            Y.Array.each(messageContext.getMessages(), function (message) {
                Y.Array.each(message.getTranslations(), function (translation) {
                    messageIndexer.addLanguage(translation.getLanguage());
                    messageIndexer.addSubject(translation.getSubject());
                    messageIndexer.addGrade(translation.getGrade());
                });
            });
        });

        // build lookup for each message
        Y.Array.each(this._messageSystem.getContexts(), function (messageContext) {
            Y.Array.each(messageContext.getMessages(), function (message) {
                message.initTranslationIndex(messageIndexer.createIndexer3D());

                var translationIndex = 0;
                Y.Array.each(message.getTranslations(), function (translation) {
                    var messageIndex = messageIndexer.get(translation.getLanguage(), translation.getSubject(), translation.getGrade());
                    message.setTranslationIndex(messageIndex, translationIndex);
                    translationIndex++;
                });
            });
        });

    };



}, "0.1", { requires: ["messages_system", "array-extras", "io", "json-parse"] });

