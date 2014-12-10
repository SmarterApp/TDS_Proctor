YUI.add("messages_template", function (Y) {
    /**
    * @fileoverview This is a simple template engine inspired by JsTemplates
    * optimized for i18n.
    *
    * It currently supports two handlers:
    *
    *   * i18n-content which sets the textContent of the element
    *
    *     <span i18n-content="myContent"></span>
    *     i18nTemplate.process(element, {'myContent': 'Content'});
    *
    *   * i18n-values is a list of attribute-value or property-value pairs.
    *     Properties are prefixed with a '.' and can contain nested properties.
    *
    *     <span i18n-values="title:myTitle;.style.fontSize:fontSize"></span>
    *     i18nTemplate.process(element, {
    *       'myTitle': 'Title',
    *       'fontSize': '13px'
    *     });
    */

    MessageTemplate = (function () {
        var hasText = function (element) {
            var text = P.Util.Dom.getTextContent(element);
            if (text == null) return false;
            text = Y.Lang.trim(text);
            return (text.length > 0);
        };

        /**
        * This provides the handlers for the templating engine. The key is used as
        * the attribute name and the value is the function that gets called for every
        * single node that has this attribute.
        * @type {Object}
        */
        var handlers = {
            /**
            * This handler sets the inner html of the element.
            */
            'i18n-content': function (element, attributeValue, obj) {
                var replacement = obj[attributeValue];

                if (replacement) {
                    P.Util.Dom.setContent(element, replacement);
                }
                else if (!hasText(element)) {
                    P.Util.Dom.setContent(element, attributeValue);
                }
            },

            /**
            * This handler sets the textContent of the element.
            */
            'i18n-text': function (element, attributeValue, obj) {
                var replacement = obj[attributeValue];

                if (replacement) {
                    P.Util.Dom.setTextContent(element, replacement);
                }
                else if (!hasText(element)) {
                    P.Util.Dom.setTextContent(element, attributeValue);
                }
            },

            // this handler is for TDS buttons
            'i18n-button': function (element, attributeValue, obj) {
                var replacement = obj[attributeValue];

                if (replacement) {
                    replacement = replacement.replace('<span>', '');
                    replacement = replacement.replace('</span>', '');
                    P.Util.Dom.setTextContent(element, replacement);
                }
                else if (!hasText(element)) {
                    P.Util.Dom.setTextContent(element, attributeValue);
                }
            },

//            /**
//            * This handler adds options to a select element.
//            */
//            'i18n-options': function (element, attributeValue, obj) {
//                var options = obj[attributeValue];

//                options.forEach(function (values) {
//                    var option = typeof values == 'string' ? new Option(values) : new Option(values[1], values[0]);
//                    element.appendChild(option);
//                });
//            },

            /**
            * This is used to set HTML attributes and DOM properties,. The syntax is:
            *   attributename:key;
            *   .domProperty:key;
            *   .nested.dom.property:key
            */
            'i18n-values': function (element, attributeValue, obj) {
                var parts = attributeValue.replace(/\s/g, '').split(/;/);

                for (var j = 0; j < parts.length; j++) {
                    var a = parts[j].match(/^([^:]+):(.+)$/);

                    if (a) {
                        var propName = a[1];
                        var propExpr = a[2];

                        // Ignore missing properties
                        if (propExpr in obj) {
                            var value = obj[propExpr];
                            if (propName.charAt(0) == '.') {
                                var path = propName.slice(1).split('.');
                                var object = element;

                                while (object && path.length > 1) {
                                    object = object[path.shift()];
                                }

                                if (object) {
                                    object[path] = value;

                                    // In case we set innerHTML (ignoring others) we need to
                                    // recursively check the content
                                    if (path == 'innerHTML') {
                                        process(element, obj);
                                    }
                                }
                            }
                            else {
                                P.Util.Dom.setAttribute(element, propName, value);
                            }
                        }
                        else {
                            Y.log('i18n-values: Missing value for "' + propExpr + '"');
                        }
                    }
                }
            }
        };

        // add attribute handler functions
        var attributeNames = [];
        for (var key in handlers) {
            attributeNames.push(key);
        }

        // query selector format
        var selector = '[' + attributeNames.join('],[') + ']';

        function process(node, obj) {
            var elements = node.all(selector);
            if (elements == null) return;
            var len = elements.size();

            for (var i = 0; i < len; i++) {
                var element = elements.item(i);
                for (var j = 0; j < attributeNames.length; j++) {
                    var name = attributeNames[j];
                    if (element.hasAttribute(name)) {
                        var att = element.getAttribute(name);
                        handlers[name](element, att, obj);
                    }
                }
            }
        }

        return {
            process: process
        };
    })();

    // helper function for passing in a language and replacing the documents translations
    MessageTemplate.processLanguage = function (node) {
        // render replacements
        var templateData = P.TDS.messages.getTemplateData();
        MessageTemplate.process(node || Y.Node(document), templateData);
    };



}, "0.1", { requires: ["messages_system", "array-extras", "node"] });
